package com.nh.introstudio.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

/** Plays the mixed soundtrack in sync with the preview (static AudioTrack, seekable). */
class PreviewAudio {
    private var track: AudioTrack? = null
    private var frames = 0

    @Synchronized
    fun setPcm(pcm: ShortArray?) {
        release()
        if (pcm == null || pcm.isEmpty()) return
        try {
            val t = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(AudioMixer.SR).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build())
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            t.write(pcm, 0, pcm.size)
            frames = pcm.size / 2
            track = t
        } catch (e: Exception) {
            track = null
        }
    }

    @Synchronized
    fun play(fromSec: Float) {
        val t = track ?: return
        try {
            t.stop()
            val f = (fromSec * AudioMixer.SR).toInt().coerceIn(0, (frames - 1).coerceAtLeast(0))
            t.setPlaybackHeadPosition(f)
            t.play()
        } catch (e: Exception) {
            // preview audio is best effort
        }
    }

    @Synchronized
    fun pause() {
        try { track?.pause() } catch (_: Exception) {}
    }

    @Synchronized
    fun stop() {
        try { track?.stop() } catch (_: Exception) {}
    }

    @Synchronized
    fun release() {
        try { track?.release() } catch (_: Exception) {}
        track = null
        frames = 0
    }
}
