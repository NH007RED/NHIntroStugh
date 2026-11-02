package com.nh.introstudio.audio

import android.content.Context
import com.nh.introstudio.domain.BrandConfig
import com.nh.introstudio.templates.TemplateCatalog
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.tanh

/** Builds the final stereo 16-bit PCM soundtrack (music + synthesized effects) used by both preview and export. */
object AudioMixer {
    const val SR = SfxSynth.SR

    /** Interleaved stereo PCM, or null when the project has no audible content. */
    fun mix(context: Context, cfg: BrandConfig): ShortArray? {
        val d = cfg.durationSec
        val frames = (d * SR).toInt()
        if (frames <= 0) return null
        val mix = FloatArray(frames * 2)
        var any = false

        if (cfg.musicSource != "none" && cfg.musicVolume > 0.001f) {
            val seg = if (cfg.musicEndSec > cfg.musicStartSec) min(cfg.musicEndSec - cfg.musicStartSec, d) else d
            val music = AudioDecoder.decode(context, cfg.musicSource, cfg.musicStartSec, seg)
            if (music != null) {
                any = true
                val n = min(music.size / 2, frames)
                for (i in 0 until n) {
                    val t = i.toFloat() / SR
                    val fadeIn = min(1f, t / 0.15f)
                    val fadeOut = min(1f, ((d - t) / 0.8f).coerceAtLeast(0f))
                    val g = cfg.musicVolume * fadeIn * fadeOut
                    mix[i * 2] += music[i * 2] * g
                    mix[i * 2 + 1] += music[i * 2 + 1] * g
                }
            }
        }

        if (cfg.sfxEnabled && cfg.sfxVolume > 0.001f) {
            val sfx = SfxSynth.render(TemplateCatalog.byId(cfg.templateId).sfx, cfg.speed, d)
            any = true
            for (i in 0 until frames) {
                val t = i.toFloat() / SR
                val fo = min(1f, ((d - t) / 0.4f).coerceAtLeast(0f))
                mix[i * 2] += sfx[i * 2] * cfg.sfxVolume * fo
                mix[i * 2 + 1] += sfx[i * 2 + 1] * cfg.sfxVolume * fo
            }
        }
        if (!any) return null

        val norm = 1f / tanh(1.2f)
        val out = ShortArray(frames * 2)
        var peak = 0f
        for (i in out.indices) {
            val v = tanh(mix[i] * 1.2f) * norm * 0.92f
            peak = maxOf(peak, abs(v))
            out[i] = (v.coerceIn(-1f, 1f) * 32767f).toInt().toShort()
        }
        return if (peak < 0.0005f) null else out
    }
}
