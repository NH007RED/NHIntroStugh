package com.nh.introstudio.audio

import android.content.Context
import com.nh.introstudio.domain.BUILTIN_MUSIC
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Pcm(val sampleRate: Int, val channels: Int, val samples: ShortArray) {
    val frames: Int get() = if (channels == 0) 0 else samples.size / channels
    val durationSec: Float get() = if (sampleRate == 0) 0f else frames.toFloat() / sampleRate
}

/** Reads the royalty-free WAV tracks bundled in assets/audio. */
object BuiltinMusic {
    private val cache = HashMap<String, Pcm>()

    fun load(context: Context, id: String): Pcm? {
        cache[id]?.let { return it }
        val track = BUILTIN_MUSIC.firstOrNull { it.id == id } ?: return null
        return try {
            val bytes = context.assets.open(track.file).use { it.readBytes() }
            parseWav(bytes)?.also { cache[id] = it }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseWav(b: ByteArray): Pcm? {
        if (b.size < 44) return null
        val bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN)
        if (String(b, 0, 4) != "RIFF" || String(b, 8, 4) != "WAVE") return null
        var pos = 12
        var rate = 22050; var ch = 1; var bits = 16
        while (pos + 8 <= b.size) {
            val id = String(b, pos, 4)
            val size = bb.getInt(pos + 4)
            val body = pos + 8
            if (id == "fmt ") {
                ch = bb.getShort(body + 2).toInt()
                rate = bb.getInt(body + 4)
                bits = bb.getShort(body + 14).toInt()
            } else if (id == "data") {
                if (bits != 16) return null
                val len = minOf(size, b.size - body) / 2
                val s = ShortArray(len)
                for (i in 0 until len) s[i] = bb.getShort(body + i * 2)
                return Pcm(rate, ch, s)
            }
            pos = body + size + (size and 1)
        }
        return null
    }
}
