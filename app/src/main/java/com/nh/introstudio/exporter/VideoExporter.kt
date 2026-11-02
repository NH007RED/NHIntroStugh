package com.nh.introstudio.exporter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import com.nh.introstudio.audio.AudioMixer
import com.nh.introstudio.domain.BrandConfig
import com.nh.introstudio.domain.ExportSettings
import com.nh.introstudio.renderer.IntroRenderer
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.min

class ExportException(message: String) : Exception(message)
class ExportCancelled : Exception("Export cancelled")

data class ExportOutput(
    val uri: Uri, val displayName: String, val width: Int, val height: Int,
    val fps: Int, val sizeBytes: Long, val durationSec: Float
)

/**
 * Real offline renderer: every frame is drawn by [IntroRenderer], uploaded to an OpenGL ES texture,
 * encoded with MediaCodec (H.264 / H.265), the soundtrack is mixed and encoded to AAC, and both are muxed into an MP4 with MediaMuxer.
 * Must be called on a single dedicated thread (the EGL context is bound to the calling thread).
 */
class VideoExporter(private val context: Context) {

    private class AudioChunk(val data: ByteArray, val pts: Long, val flags: Int)
    private class AudioData(val format: MediaFormat, val chunks: List<AudioChunk>)

    fun run(
        projectName: String,
        cfg: BrandConfig,
        set: ExportSettings,
        isActive: () -> Boolean,
        onProgress: (Float, String) -> Unit
    ): ExportOutput {
        val compat = ExportCompat.check(set)
        if (!compat.ok || compat.encoderName == null) throw ExportException(compat.message)
        val w = compat.width
        val h = compat.height
        val temp = File(context.cacheDir, "export_${System.currentTimeMillis()}.mp4")

        var encoder: MediaCodec? = null
        var gl: EncoderGl? = null
        var muxer: MediaMuxer? = null
        var bitmap: Bitmap? = null
        val renderer = IntroRenderer()
        try {
            onProgress(0f, "Mixing audio")
            val pcm = AudioMixer.mix(context, cfg)
            val audio = if (pcm != null) encodeAudio(pcm, isActive) else null
            if (!isActive()) throw ExportCancelled()

            val enc = MediaCodec.createByCodecName(compat.encoderName)
            encoder = enc
            val range = ExportCompat.supportedBitrateRange(set)
            var bitrate = set.effectiveBitrate()
            if (range != null) bitrate = bitrate.coerceIn(range.first, range.last)
            val fmt = MediaFormat.createVideoFormat(set.codec.mime, w, h).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, set.fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }
            enc.configure(fmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = enc.createInputSurface()
            enc.start()
            try {
                gl = EncoderGl(inputSurface, w, h)
            } catch (e: TextureLimitException) {
                throw ExportException("This device's GPU supports textures up to ${e.max} px, so $w×$h cannot be rendered. Choose a lower resolution.")
            }
            val frameBitmap = try {
                Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            } catch (e: OutOfMemoryError) {
                throw ExportException("There is not enough memory to render $w×$h. Choose a lower resolution.")
            }
            bitmap = frameBitmap
            val canvas = Canvas(frameBitmap)
            val mux = MediaMuxer(temp.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            muxer = mux

            var vTrack = -1
            var aTrack = -1
            var started = false
            val info = MediaCodec.BufferInfo()

            fun drain(eos: Boolean) {
                var idle = 0
                while (true) {
                    val idx = enc.dequeueOutputBuffer(info, if (eos) 10_000L else 0L)
                    if (idx == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        if (!eos) return
                        if (!isActive()) throw ExportCancelled()
                        if (++idle > 3000) throw ExportException("The encoder stopped responding.")
                    } else if (idx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (started) throw ExportException("Encoder format changed unexpectedly.")
                        vTrack = mux.addTrack(enc.outputFormat)
                        if (audio != null) aTrack = mux.addTrack(audio.format)
                        mux.start()
                        started = true
                        if (audio != null) {
                            for (c in audio.chunks) {
                                val bi = MediaCodec.BufferInfo()
                                bi.set(0, c.data.size, c.pts, c.flags)
                                mux.writeSampleData(aTrack, ByteBuffer.wrap(c.data), bi)
                            }
                        }
                    } else if (idx >= 0) {
                        idle = 0
                        val ob = enc.getOutputBuffer(idx)!!
                        if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) info.size = 0
                        if (info.size > 0 && started) {
                            ob.position(info.offset)
                            ob.limit(info.offset + info.size)
                            mux.writeSampleData(vTrack, ob, info)
                        }
                        enc.releaseOutputBuffer(idx, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) return
                    }
                }
            }

            val total = ceil(cfg.durationSec * set.fps).toInt().coerceAtLeast(1)
            for (i in 0 until total) {
                if (!isActive()) throw ExportCancelled()
                renderer.render(canvas, w, h, i.toFloat() / set.fps, cfg, isPreview = false)
                gl.draw(frameBitmap, i * 1_000_000_000L / set.fps)
                drain(false)
                onProgress(0.05f + 0.90f * (i + 1) / total, "Rendering frame ${i + 1} of $total")
            }
            onProgress(0.96f, "Finalizing video")
            enc.signalEndOfInputStream()
            drain(true)
            if (!started) throw ExportException("The encoder produced no output.")
            mux.stop()

            onProgress(0.98f, "Saving to Movies/NHIntroStudio")
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val safe = projectName.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_').ifEmpty { "Intro" }
            val name = "NHIntro_${safe}_$stamp.mp4"
            val saved = MediaSaver.save(context, temp, name)
            return ExportOutput(saved.uri, name, w, h, set.fps, saved.sizeBytes, cfg.durationSec)
        } finally {
            try { encoder?.stop() } catch (_: Throwable) {}
            try { encoder?.release() } catch (_: Throwable) {}
            gl?.release()
            try { muxer?.release() } catch (_: Throwable) {}
            bitmap?.recycle()
            renderer.release()
            temp.delete()
        }
    }

    private fun encodeAudio(pcm: ShortArray, isActive: () -> Boolean): AudioData {
        val sr = AudioMixer.SR
        val fmt = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sr, 2).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_BIT_RATE, 192_000)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
        }
        val enc = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        try {
            enc.configure(fmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            enc.start()
            val info = MediaCodec.BufferInfo()
            val chunks = ArrayList<AudioChunk>()
            var outFormat: MediaFormat? = null
            var pos = 0
            var inputDone = false
            while (true) {
                if (!isActive()) throw ExportCancelled()
                if (!inputDone) {
                    val ii = enc.dequeueInputBuffer(10_000L)
                    if (ii >= 0) {
                        val ib = enc.getInputBuffer(ii)!!
                        ib.clear()
                        var n = min(ib.capacity() / 2, pcm.size - pos)
                        n -= n % 2
                        val pts = (pos / 2).toLong() * 1_000_000L / sr
                        if (n <= 0) {
                            enc.queueInputBuffer(ii, 0, 0, pts, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            ib.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(pcm, pos, n)
                            enc.queueInputBuffer(ii, 0, n * 2, pts, 0)
                            pos += n
                        }
                    }
                }
                val oi = enc.dequeueOutputBuffer(info, 10_000L)
                if (oi == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    outFormat = enc.outputFormat
                } else if (oi >= 0) {
                    val ob = enc.getOutputBuffer(oi)!!
                    if (info.size > 0 && info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) {
                        val data = ByteArray(info.size)
                        ob.position(info.offset)
                        ob.get(data)
                        chunks.add(AudioChunk(data, info.presentationTimeUs, info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM.inv()))
                    }
                    val eos = info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                    enc.releaseOutputBuffer(oi, false)
                    if (eos) break
                }
            }
            return AudioData(outFormat ?: throw ExportException("Audio encoder produced no format."), chunks)
        } finally {
            try { enc.stop() } catch (_: Throwable) {}
            try { enc.release() } catch (_: Throwable) {}
        }
    }
}
