package com.nh.introstudio.audio

import com.nh.introstudio.domain.SfxEvent
import com.nh.introstudio.domain.SfxType
import java.util.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

/** Original synthesized sound effects (no samples, no copyrighted material). Output: stereo interleaved floats at 44.1 kHz. */
object SfxSynth {
    const val SR = 44100

    fun render(events: List<SfxEvent>, speed: Float, durationSec: Float): FloatArray {
        val frames = (durationSec * SR).toInt()
        val out = FloatArray(frames * 2)
        val rnd = Random(7)
        for (e in events) {
            val start = ((e.at / speed) * SR).toInt()
            if (start >= frames) continue
            val gain = e.gain
            fun add(i: Int, l: Float, r: Float) {
                val idx = start + i
                if (idx in 0 until frames) {
                    out[idx * 2] += l * gain
                    out[idx * 2 + 1] += r * gain
                }
            }
            when (e.type) {
                SfxType.WHOOSH -> {
                    val n = (1.3f * SR).toInt(); var lp = 0f
                    for (i in 0 until n) {
                        val x = i.toFloat() / n
                        val env = sin(PI.toFloat() * x).pow(2) * 0.55f
                        lp += (0.02f + 0.55f * x) * ((rnd.nextFloat() * 2f - 1f) - lp)
                        val s = lp * env * 2f
                        add(i, s * (1f - x * 0.8f), s * (0.2f + x * 0.8f))
                    }
                }
                SfxType.BOOM -> {
                    val n = (1.6f * SR).toInt(); var ph = 0.0; var lp = 0f
                    for (i in 0 until n) {
                        val t = i.toFloat() / SR
                        val f = 35.0 + 80.0 * exp(-t * 6.0)
                        ph += 2.0 * PI * f / SR
                        lp += 0.08f * ((rnd.nextFloat() * 2f - 1f) - lp)
                        val s = (sin(ph) * exp(-t * 2.6) * 0.9 + lp * exp(-t * 18.0) * 1.2).toFloat()
                        add(i, s, s)
                    }
                }
                SfxType.SHIMMER -> {
                    val freqs = floatArrayOf(1046.5f, 1318.5f, 1568f, 2093f, 2637f)
                    for ((k, f) in freqs.withIndex()) {
                        val off = (k * 0.08f * SR).toInt(); val n = SR
                        for (i in 0 until n) {
                            val t = i.toFloat() / SR
                            val env = (1f - exp(-t * 400f)) * exp(-t * 4.5f)
                            val s = (sin(2.0 * PI * f * t).toFloat() * 0.22f + sin(2.0 * PI * f * 2 * t).toFloat() * 0.08f) * env
                            add(off + i, s * (0.6f + 0.1f * k), s * (1f - 0.1f * k))
                        }
                    }
                }
                SfxType.ZAP -> {
                    val n = (0.3f * SR).toInt(); var prev = 0f
                    for (i in 0 until n) {
                        val t = i.toFloat() / SR
                        val nz = rnd.nextFloat() * 2f - 1f
                        val hp = nz - prev; prev = nz
                        val buzz = if (sin(2.0 * PI * 90.0 * t) > 0) 0.25f else -0.25f
                        val s = (hp * 0.5f + buzz) * exp(-t * 14f)
                        add(i, s, s)
                    }
                }
                SfxType.BLIP -> {
                    val n = (0.08f * SR).toInt()
                    for (i in 0 until n) {
                        val t = i.toFloat() / SR
                        val env = (1f - i.toFloat() / n).pow(2)
                        val s = sin(2.0 * PI * 1320.0 * t).toFloat() * env * 0.35f
                        add(i, s, s)
                    }
                }
                SfxType.CHORD -> {
                    val freqs = floatArrayOf(110f, 220f, 277.18f, 329.63f)
                    val n = (2.8f * SR).toInt()
                    for (i in 0 until n) {
                        val t = i.toFloat() / SR
                        val env = (1f - exp(-t * 14f)) * exp(-t * 1.1f)
                        var s = 0f
                        for (f in freqs) {
                            s += sin(2.0 * PI * f * t).toFloat() + 0.5f * sin(2.0 * PI * f * 2 * t).toFloat() + 0.25f * sin(2.0 * PI * f * 3 * t).toFloat()
                        }
                        s *= 0.085f * env
                        add(i, s, s)
                    }
                }
                SfxType.FIRE -> {
                    val n = (1.8f * SR).toInt(); var lp = 0f
                    for (i in 0 until n) {
                        val x = i.toFloat() / n
                        val env = sin(PI.toFloat() * x).pow(1.2f)
                        lp += 0.3f * ((rnd.nextFloat() * 2f - 1f) - lp)
                        var s = lp * env * 1.1f
                        if (rnd.nextFloat() < 0.0015f) s += (rnd.nextFloat() - 0.5f) * 0.9f * env
                        add(i, s, s)
                    }
                }
                SfxType.SWEEP -> {
                    val n = (1.5f * SR).toInt(); var ph = 0.0
                    for (i in 0 until n) {
                        val x = i.toFloat() / n
                        ph += 2.0 * PI * (200.0 + 1800.0 * x * x) / SR
                        val env = sin(PI.toFloat() * x).pow(1.5f) * 0.35f
                        val s = sin(ph).toFloat() * env
                        add(i, s * (1f - x * 0.6f), s * (0.4f + x * 0.6f))
                    }
                }
                SfxType.RISER -> {
                    val n = (1.5f * SR).toInt(); var ph = 0.0; var prev = 0f
                    for (i in 0 until n) {
                        val x = i.toFloat() / n
                        ph += 2.0 * PI * (300.0 + 1200.0 * x) / SR
                        val nz = rnd.nextFloat() * 2f - 1f
                        val hp = nz - prev; prev = nz
                        val s = (hp * 0.25f + sin(ph).toFloat() * 0.3f) * x * x
                        add(i, s, s)
                    }
                }
                SfxType.SPLASH -> {
                    // Glossy liquid droplet pitch-up plop + airy splash burst
                    val n = (0.95f * SR).toInt(); var ph = 0.0; var lp = 0f
                    for (i in 0 until n) {
                        val t = i.toFloat() / SR
                        val dropEnv = exp(-t * 16.0).toFloat()
                        val fDrop = 280.0 + 920.0 * (1.0 - exp(-t * 28.0))
                        ph += 2.0 * PI * fDrop / SR
                        val dropTone = sin(ph).toFloat() * dropEnv * 0.55f
                        lp += 0.45f * ((rnd.nextFloat() * 2f - 1f) - lp)
                        val splashNoise = lp * exp(-t * 6.5).toFloat() * (1f - exp(-t * 50.0).toFloat()) * 0.75f
                        val s = dropTone + splashNoise
                        add(i, s * 0.95f, s * 1.05f)
                    }
                }
                SfxType.GLITCH_SFX -> {
                    // Digital RGB stutter + high-tech data snap
                    val n = (0.65f * SR).toInt(); var ph = 0.0
                    for (i in 0 until n) {
                        val t = i.toFloat() / SR
                        val step = (t * 36f).toInt()
                        val freq = 220.0 + ((step * 523) % 1900)
                        ph += 2.0 * PI * freq / SR
                        val gate = if (step % 3 != 1) 1f else 0.15f
                        val env = exp(-t * 3.8).toFloat() * gate
                        val bit = if (sin(ph) > 0) 0.38f else -0.38f
                        val nz = (rnd.nextFloat() * 2f - 1f) * 0.25f
                        val s = (bit + nz) * env
                        add(i, s * (if (step % 2 == 0) 1f else 0.4f), s * (if (step % 2 == 1) 1f else 0.4f))
                    }
                }
                SfxType.CINEMATIC_HIT -> {
                    // Clean modern sub-bass punch + crystalline harmonic bell (LogoAnimationAI style)
                    val n = (1.9f * SR).toInt(); var phSub = 0.0
                    for (i in 0 until n) {
                        val t = i.toFloat() / SR
                        val fSub = 48.0 + 95.0 * exp(-t * 14.0)
                        phSub += 2.0 * PI * fSub / SR
                        val sub = sin(phSub).toFloat() * exp(-t * 3.2).toFloat() * 0.75f
                        val bellEnv = (1f - exp(-t * 250f)) * exp(-t * 2.4f)
                        val bell = (sin(2.0 * PI * 523.25 * t).toFloat() * 0.28f +
                            sin(2.0 * PI * 783.99 * t).toFloat() * 0.20f +
                            sin(2.0 * PI * 1567.98 * t).toFloat() * 0.12f) * bellEnv
                        val s = sub + bell
                        add(i, s, s)
                    }
                }
            }
        }
        for (i in out.indices) if (abs(out[i]) > 4f) out[i] = if (out[i] > 0) 4f else -4f
        return out
    }
}
