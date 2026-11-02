package com.nh.introstudio.audio

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import java.nio.ByteOrder
import kotlin.math.floor
import kotlin.math.min

/** Decodes any supported audio source into stereo float samples at 44.1 kHz. */
object AudioDecoder {
    private const val SR = SfxSynth.SR

    /** Length of the source in seconds, or 0 when unknown. */
    fun durationSec(context: Context, source: String): Float {
        return try {
            when {
                source.startsWith("builtin:") -> BuiltinMusic.load(context, source.removePrefix("builtin:"))?.durationSec ?: 0f
                source.startsWith("file:") -> {
                    val r = MediaMetadataRetriever()
                    try {
                        r.setDataSource(source.removePrefix("file:"))
                        (r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L) / 1000f
                    } finally {
                        r.release()
                    }
                }
                else -> 0f
            }
        } catch (e: Exception) {
            0f
        }
    }

    /** Returns interleaved stereo floats for the segment [startSec, startSec + maxSec], or null if it cannot be decoded. */
    fun decode(context: Context, source: String, startSec: Float, maxSec: Float): FloatArray? {
        return try {
            when {
                source.startsWith("builtin:") -> {
                    val pcm = BuiltinMusic.load(context, source.removePrefix("builtin:")) ?: return null
                    resample(pcm.samples, pcm.sampleRate, pcm.channels, startSec, maxSec)
                }
                source.startsWith("file:") -> decodeFile(source.removePrefix("file:"), startSec, maxSec)
                else -> null
            }
        } catch (e: Throwable) {
            null
        }
    }

    private fun decodeFile(path: String, startSec: Float, maxSec: Float): FloatArray? {
        val ex = MediaExtractor()
        var codec: MediaCodec? = null
        try {
            ex.setDataSource(path)
            var track = -1
            var fmt: MediaFormat? = null
            for (i in 0 until ex.trackCount) {
                val f = ex.getTrackFormat(i)
                if ((f.getString(MediaFormat.KEY_MIME) ?: "").startsWith("audio/")) { track = i; fmt = f; break }
            }
            if (track < 0 || fmt == null) return null
            ex.selectTrack(track)
            val startUs = (startSec * 1_000_000L).toLong()
            if (startUs > 0) ex.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            codec = MediaCodec.createDecoderByType(fmt.getString(MediaFormat.KEY_MIME)!!)
            codec.configure(fmt, null, null, 0)
            codec.start()

            var rate = fmt.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            var ch = fmt.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            var isFloat = false
            var buf = ShortArray(rate * ch * 4)
            var count = 0
            val info = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            var needed = (maxSec * rate).toInt() * ch + ch
            while (!outputDone && count < needed) {
                if (!inputDone) {
                    val ii = codec.dequeueInputBuffer(10_000)
                    if (ii >= 0) {
                        val ib = codec.getInputBuffer(ii)!!
                        val n = ex.readSampleData(ib, 0)
                        if (n < 0) {
                            codec.queueInputBuffer(ii, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            codec.queueInputBuffer(ii, 0, n, ex.sampleTime, 0)
                            ex.advance()
                        }
                    }
                }
                val oi = codec.dequeueOutputBuffer(info, 10_000)
                if (oi == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val of = codec.outputFormat
                    rate = of.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    ch = of.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    isFloat = of.containsKey(MediaFormat.KEY_PCM_ENCODING) &&
                        of.getInteger(MediaFormat.KEY_PCM_ENCODING) == AudioFormat.ENCODING_PCM_FLOAT
                    needed = (maxSec * rate).toInt() * ch + ch
                } else if (oi >= 0) {
                    if (info.size > 0) {
                        val ob = codec.getOutputBuffer(oi)!!
                        ob.position(info.offset); ob.limit(info.offset + info.size)
                        ob.order(ByteOrder.LITTLE_ENDIAN)
                        val total = if (isFloat) info.size / 4 else info.size / 2
                        var skip = 0
                        if (info.presentationTimeUs < startUs) {
                            skip = (((startUs - info.presentationTimeUs) * rate) / 1_000_000L).toInt() * ch
                            if (skip > total) skip = total
                        }
                        val tmp = ShortArray(total)
                        if (isFloat) {
                            val fb = ob.asFloatBuffer()
                            for (i in 0 until total) tmp[i] = (fb.get(i).coerceIn(-1f, 1f) * 32767f).toInt().toShort()
                        } else {
                            ob.asShortBuffer().get(tmp)
                        }
                        val take = total - skip
                        if (take > 0) {
                            if (count + take > buf.size) buf = buf.copyOf(maxOf(buf.size * 2, count + take))
                            System.arraycopy(tmp, skip, buf, count, take)
                            count += take
                        }
                    }
                    codec.releaseOutputBuffer(oi, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
                }
            }
            if (count == 0) return null
            return resample(buf.copyOf(count), rate, ch, 0f, maxSec)
        } finally {
            try { codec?.stop() } catch (_: Exception) {}
            try { codec?.release() } catch (_: Exception) {}
            ex.release()
        }
    }

    /** Linear resample + channel conversion to stereo float at 44.1 kHz. */
    fun resample(samples: ShortArray, rate: Int, channels: Int, startSec: Float, maxSec: Float): FloatArray? {
        if (channels <= 0 || rate <= 0) return null
        val srcFrames = samples.size / channels
        val startFrame = (startSec * rate).toInt().coerceAtLeast(0)
        if (startFrame >= srcFrames) return null
        val avail = (srcFrames - startFrame).toDouble() / rate
        val outFrames = (min(avail, maxSec.toDouble()) * SR).toInt()
        if (outFrames <= 0) return null
        val out = FloatArray(outFrames * 2)
        val step = rate.toDouble() / SR
        for (o in 0 until outFrames) {
            val pos = startFrame + o * step
            val i0 = floor(pos).toInt()
            val i1 = min(i0 + 1, srcFrames - 1)
            val fr = (pos - i0).toFloat()
            val l = lerpS(samples, i0 * channels, i1 * channels, fr)
            val r = if (channels > 1) lerpS(samples, i0 * channels + 1, i1 * channels + 1, fr) else l
            out[o * 2] = l; out[o * 2 + 1] = r
        }
        return out
    }

    private fun lerpS(s: ShortArray, a: Int, b: Int, f: Float): Float {
        val x = s[a] / 32768f
        val y = s[b] / 32768f
        return x + (y - x) * f
    }
}
