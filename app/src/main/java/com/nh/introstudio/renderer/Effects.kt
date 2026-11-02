package com.nh.introstudio.renderer

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RadialGradient
import android.graphics.Shader
import com.nh.introstudio.domain.EmblemType
import com.nh.introstudio.domain.InOutStyle
import com.nh.introstudio.domain.MascotType
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Broadcast-grade procedural stage, lighting, particle, and emblem engines for NH Intro Studio v4:
 * - Templates 1..20: 20 Distinct AI Logo Animation stages with Deep Unified In/Idle/Out integration
 * - Templates 21..40: 20 Streaming & Cinema Hero Text Intro stages (Netflix, Prime, Hotstar, Marvel,
 *   Apple TV+, HBO, IMAX, Dolby, Universal, Columbia, JioCinema, Zee5, A24, Hulu, Paramount, Warner,
 *   Peacock, YouTube Originals, Cyberpunk, and Epic Trailer Molten Titanium).
 */
internal object Effects {

    fun drawStageEffect(
        id: Int,
        g: Gfx,
        isLightPaper: Boolean,
        logoCx: Float,
        logoCy: Float,
        emblemSize: Float,
        textLeft: Float,
        textWidth: Float,
        by1: Float,
        bs: Float
    ) {
        if (isLightPaper) drawPaperGrain(g)
        when (id) {
            // Section 1: 20 AI Logo Animation Stages (1..20)
            1 -> fx1BirdVortexStage(g, logoCx, logoCy, emblemSize)
            2 -> fx2PaintSplash(g, logoCx, logoCy, emblemSize)
            3 -> fx3GyroSpin3D(g, logoCx, logoCy, emblemSize)
            4 -> fx4OrigamiFold(g, logoCx, logoCy, emblemSize, textLeft, textWidth, by1)
            5 -> fx5CyberGlitch(g, logoCx, logoCy, emblemSize)
            6 -> fx6GalaxyParticles(g, logoCx, logoCy, emblemSize)
            7 -> fx7LaserTrace(g, logoCx, logoCy, emblemSize)
            8 -> fx8FireSweep(g, logoCx, logoCy, emblemSize)
            9 -> fx9MinimalShutter(g, logoCx, logoCy, emblemSize, textLeft, textWidth, by1)
            10 -> fx10NeonTube(g, logoCx, logoCy, emblemSize)
            11 -> fx11BouncingBall(g, logoCx, logoCy, emblemSize, textLeft, textWidth, by1, bs)
            12 -> fx12RoyalRibbon(g, logoCx, logoCy, emblemSize)
            13 -> fx13LiquidMetaball(g, logoCx, logoCy, emblemSize)
            14 -> fx14IsometricCubeMatrix(g, logoCx, logoCy, emblemSize)
            15 -> fx15LightningShockwave(g, logoCx, logoCy, emblemSize)
            16 -> fx16ZenInkBrush(g, logoCx, logoCy, emblemSize)
            17 -> fx17QuantumPortal(g, logoCx, logoCy, emblemSize)
            18 -> fx18AudioEqualizer(g, logoCx, logoCy, emblemSize)
            19 -> fx19BlueprintScan(g, logoCx, logoCy, emblemSize, textLeft, textWidth, by1)
            20 -> fx20AnamorphicFlare(g, logoCx, logoCy, emblemSize)

            // Section 2: 20 Streaming & Cinema Text Intro Stages (21..40)
            21 -> fx21NetflixPrismStage(g)
            22 -> fx22PrimeSmileStage(g, textLeft, textWidth, by1, bs)
            23 -> fx23HotstarArcStage(g, textLeft, textWidth, by1, bs)
            24 -> fx24MarvelFlipStage(g)
            25 -> fx25AppleGlassStage(g)
            26 -> fx26HboChromeStage(g)
            27 -> fx27ImaxLaserStage(g)
            28 -> fx28DolbySphereStage(g)
            29 -> fx29UniversalOrbitStage(g)
            30 -> fx30TorchPillarStage(g)
            31 -> fx31JioStadiumStage(g)
            32 -> fx32AuroraSilkStage(g)
            33 -> fx33A24EditorialStage(g)
            34 -> fx34HuluSonicStage(g, textWidth, bs)
            35 -> fx35ParamountStarHaloStage(g, by1, bs)
            36 -> fx36WarnerMonolithStage(g)
            37 -> fx37PeacockBloomStage(g, by1, bs)
            38 -> fx38RedPlaySplitStage(g, textWidth, by1, bs)
            39 -> fx39CyberCutStage(g)
            40 -> fx40MoltenTitaniumStage(g)
        }
    }

    /**
     * Deep Foreground Unified Light-Wrap & Volumetric Bokeh Pass:
     * Drawn AFTER the logo and text so light, sheen, and particles pass seamlessly *in front* of
     * both the logo and the typography—making it impossible to tell they are separate layers!
     */
    fun drawUnifiedForegroundLightWrap(
        g: Gfx,
        isLightPaper: Boolean,
        logoCx: Float,
        logoCy: Float,
        emblemSize: Float,
        textCenterX: Float,
        by1: Float,
        bs: Float,
        inProg: Float,
        outroProg: Float,
        inOutStyle: InOutStyle
    ) {
        val w = g.w; val h = g.h; val u = g.u; val tau = g.tau

        // 1. Continuous diagonal specular light-sheen sweeping across BOTH logo and text
        val sweepPos = fmod(tau * 0.38f, 1.8f) - 0.4f
        val sweepX = sweepPos * w
        g.c.save()
        g.c.rotate(-22f, w * 0.5f, h * 0.5f)
        g.paint.reset(); g.paint.isAntiAlias = true
        val sheenCol = if (isLightPaper) g.p2 else Color.WHITE
        val sheenAlpha = if (isLightPaper) 0.14f else 0.11f
        g.paint.shader = LinearGradient(
            sweepX - u * 14f, 0f, sweepX + u * 14f, 0f,
            intArrayOf(withAlpha(sheenCol, 0f), withAlpha(sheenCol, sheenAlpha * (1f - outroProg)), withAlpha(sheenCol, 0f)),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP
        )
        g.c.drawRect(sweepX - u * 14f, -h * 0.5f, sweepX + u * 14f, h * 1.5f, g.paint)
        g.c.restore()

        // 2. Ambient light-bleed bridging the space between logo and text
        val bridgeAlpha = (0.14f + 0.05f * sin(tau * 3f)) * inProg * (1f - outroProg)
        if (bridgeAlpha > 0.01f) {
            g.glow((logoCx + textCenterX) * 0.5f, (logoCy + by1) * 0.5f, emblemSize * 1.45f, g.p1, bridgeAlpha)
        }

        // 3. Foreground floating micro-bokeh motes passing over the logo and text
        val moteAlpha = inProg * (1f - outroProg)
        if (moteAlpha > 0.02f) {
            for (i in 0 until 24) {
                val mx = fmod(g.rnd(i, 701) * w + tau * u * (1.2f + g.rnd(i, 702) * 2.5f), w)
                val my = h - fmod(g.rnd(i, 703) * h + tau * u * (1.5f + g.rnd(i, 704) * 3.0f), h)
                val mr = u * (0.18f + 0.45f * g.rnd(i, 705))
                val tw = 0.35f + 0.65f * abs(sin(tau * 2.8f + i))
                g.circle(mx, my, mr, if (i % 2 == 0) g.p2 else Color.WHITE, tw * 0.32f * moteAlpha)
            }
        }

        // 4. Deep Cinematic OUT Transition Overlays (when outroProg > 0)
        if (outroProg > 0.005f) {
            when (inOutStyle) {
                InOutStyle.HYPER_ZOOM_THROUGH -> {
                    // Radial warp streaks + anamorphic flash as camera flies through the lockup
                    val flash = sin(outroProg * 3.14159f)
                    g.glow(w * 0.5f, h * 0.5f, max(w, h) * 0.75f * outroProg, g.p2, 0.55f * flash)
                    for (i in 0 until 40) {
                        val ang = i * 6.2832f / 40f
                        val r1 = min(w, h) * 0.1f * outroProg
                        val r2 = max(w, h) * 0.75f * outroProg
                        g.line(
                            w * 0.5f + cos(ang) * r1, h * 0.5f + sin(ang) * r1,
                            w * 0.5f + cos(ang) * r2, h * 0.5f + sin(ang) * r2,
                            if (i % 2 == 0) Color.WHITE else g.p1, 0.45f * flash, u * 0.28f
                        )
                    }
                }
                InOutStyle.PARTICLE_VORTEX_DISSOLVE -> {
                    // 160 glowing particles disintegrating and swirling away from the logo & text
                    val pAlpha = sin(outroProg * 3.14159f)
                    for (i in 0 until 160) {
                        val originX = if (i % 2 == 0) logoCx else textCenterX + (g.rnd(i, 1) - 0.5f) * w * 0.35f
                        val originY = if (i % 2 == 0) logoCy else by1 - bs * 0.3f
                        val ang = g.rnd(i, 2) * 6.2832f + outroProg * 2.8f
                        val dist = outroProg * outroProg * min(w, h) * (0.15f + 0.55f * g.rnd(i, 3))
                        val px = originX + cos(ang) * dist + outroProg * u * 12f
                        val py = originY + sin(ang) * dist - outroProg * u * 16f
                        val pr = u * (0.25f + 0.65f * g.rnd(i, 4)) * (1f - 0.5f * outroProg)
                        g.circle(px, py, pr, if (i % 3 == 0) g.p1 else g.p2, pAlpha * 0.85f)
                    }
                }
                InOutStyle.PRISM_CHROMATIC_WARP -> {
                    // Horizontal anamorphic photon line implosion
                    val lineW = w * (1f - 0.3f * outroProg)
                    val lineH = u * (4.5f * sin(outroProg * 3.14159f) + 0.3f)
                    val cy = (logoCy + by1) * 0.5f
                    g.glow(w * 0.5f, cy, w * 0.5f * sin(outroProg * 3.14159f), g.p1, 0.55f * sin(outroProg * 3.14159f))
                    g.rectFill((w - lineW) * 0.5f, cy - lineH * 0.5f, (w + lineW) * 0.5f, cy + lineH * 0.5f, Color.WHITE, sin(outroProg * 3.14159f) * 0.9f)
                }
                InOutStyle.LIQUID_ELASTIC_MORPH -> {
                    // Vacuum implosion shockwave rings at center
                    val rip = clamp01((outroProg - 0.2f) / 0.8f)
                    if (rip > 0f) {
                        val cx = (logoCx + textCenterX) * 0.5f
                        val cy = (logoCy + by1) * 0.5f
                        g.ring(cx, cy, min(w, h) * 0.55f * easeOut(rip), g.p1, (1f - rip) * 0.85f, u * 0.65f)
                        g.ring(cx, cy, min(w, h) * 0.35f * easeOut(rip), g.p2, (1f - rip) * 0.75f, u * 0.35f)
                    }
                }
                InOutStyle.LASER_LIGHT_SWEEP -> {
                    // Vertical laser curtain sweeping right-to-left across the canvas to vaporize the lockup
                    val beamX = lerp(w * 0.88f, w * 0.12f, easeInOut(outroProg))
                    val beamAlpha = sin(outroProg * 3.14159f)
                    g.glow(beamX, h * 0.5f, h * 0.45f, g.p2, 0.55f * beamAlpha)
                    g.line(beamX, h * 0.18f, beamX, h * 0.82f, g.p1, 0.65f * beamAlpha, u * 1.6f)
                    g.line(beamX, h * 0.18f, beamX, h * 0.82f, Color.WHITE, 0.95f * beamAlpha, u * 0.38f)
                }
            }
        }
    }

    private fun drawPaperGrain(g: Gfx) {
        val w = g.w; val h = g.h; val u = g.u
        val cBase = 0xFFD8E0DC.toInt()
        val cAcc = mixColor(g.p1, 0xFFE2E8E5.toInt(), 0.75f)
        for (i in 0 until 140) {
            val sx = g.rnd(i, 101) * w
            val sy = g.rnd(i, 202) * h
            val sr = u * (0.14f + 0.34f * g.rnd(i, 303))
            g.circle(sx, sy, sr, if (i % 5 == 0) cAcc else cBase, 0.22f)
        }
    }

    // =========================================================================================================
    // SECTION 1 STAGES: 1..20 (Logo AI Templates)
    // =========================================================================================================

    private fun fx1BirdVortexStage(g: Gfx, cx: Float, cy: Float, s: Float) {
        g.glow(cx, cy, s * 1.4f, g.p2, 0.14f)
    }

    private fun fx2PaintSplash(g: Gfx, cx: Float, cy: Float, s: Float) {
        val tau = g.tau; val u = g.u; val h = g.h
        val dropP = clamp01(tau / 0.82f)
        val cols = intArrayOf(g.p1, g.p2, g.p3)
        if (dropP < 1f) {
            for (k in 0..2) {
                val startX = cx + (k - 1) * s * 0.85f
                val startY = -h * 0.08f - k * u * 5f
                val dx = lerp(startX, cx, dropP)
                val dy = lerp(startY, cy, dropP * dropP)
                val dr = u * (2.6f - k * 0.3f)
                g.circle(dx, dy, dr, cols[k], 0.92f)
                g.circle(dx - dr * 0.28f, dy - dr * 0.28f, dr * 0.32f, Color.WHITE, 0.75f)
            }
        }
        val sp = easeOut(clamp01((tau - 0.80f) / 1.05f))
        if (sp > 0f) {
            g.glow(cx, cy, s * 1.1f * sp, g.p1, 0.22f * (1f - 0.4f * sp))
            for (i in 0 until 54) {
                val ang = i * 6.2832f / 54f + g.rnd(i) * 0.22f
                val maxD = s * (0.48f + 0.82f * g.rnd(i, 1))
                val d = maxD * easeOutBack(clamp01(sp * (0.85f + 0.25f * g.rnd(i, 2))))
                val px = cx + cos(ang) * d
                val py = cy + sin(ang) * d
                val r = u * (0.65f + 2.35f * g.rnd(i, 3)) * (1f - 0.3f * sp)
                g.circle(px, py, r, cols[i % 3], 0.88f)
                if (r > u * 1.5f) g.circle(px - r * 0.28f, py - r * 0.28f, r * 0.28f, Color.WHITE, 0.55f)
            }
            val rip = clamp01((tau - 0.82f) / 0.9f)
            if (rip < 1f) g.ring(cx, cy, s * (0.3f + 0.95f * easeOut(rip)), g.p2, (1f - rip) * 0.75f, u * 0.5f)
        }
    }

    private fun fx3GyroSpin3D(g: Gfx, cx: Float, cy: Float, s: Float) {
        val tau = g.tau; val u = g.u; val w = g.w; val h = g.h
        val p = easeOut(clamp01(tau / 1.0f))
        g.glow(cx, cy, s * 1.4f, g.p1, 0.32f * p)
        g.glow(cx, cy, s * 0.85f, g.p2, 0.25f * p)
        for (ringIdx in 0 until 3) {
            val r = s * (0.62f + ringIdx * 0.17f) * p
            val tilt = 0.34f + ringIdx * 0.20f
            val rotDeg = tau * (85f - ringIdx * 24f) * (if (ringIdx % 2 == 0) 1f else -1f) + ringIdx * 50f
            g.c.save(); g.c.translate(cx, cy); g.c.rotate(rotDeg); g.c.scale(1f, tilt)
            g.rect.set(-r, -r, r, r)
            val col = when (ringIdx) { 0 -> g.p1; 1 -> g.p2; else -> Color.WHITE }
            g.c.drawArc(g.rect, 0f, 250f, false, g.strokeP(col, (0.72f - ringIdx * 0.16f) * p, u * (0.58f - ringIdx * 0.1f)))
            g.c.restore()
        }
        for (i in 0 until 40) {
            val ang = g.rnd(i) * 6.283f + tau * 0.4f
            val dist = min(w, h) * (0.12f + 0.36f * g.rnd(i, 1)) * p
            val tw = 0.35f + 0.65f * abs(sin(tau * 3f + i))
            g.circle(cx + cos(ang) * dist, cy + sin(ang) * dist * 0.65f, u * (0.16f + 0.25f * g.rnd(i, 2)), g.p2, tw * 0.7f * p)
        }
    }

    private fun fx4OrigamiFold(g: Gfx, cx: Float, cy: Float, s: Float, textLeft: Float, textWidth: Float, by1: Float) {
        val tau = g.tau; val u = g.u
        val p = easeOut(clamp01(tau / 1.15f))
        val lineA = 0.25f * p * (1f - 0.5f * clamp01((tau - 1.8f) / 1.0f))
        g.line(cx - s * 0.7f, cy, textLeft + textWidth * p, cy, g.p1, lineA, u * 0.18f)
        g.line(cx, cy - s * 0.75f, cx, cy + s * 0.75f, g.p2, lineA, u * 0.18f)
        val unfold = clamp01(tau / 1.25f)
        if (unfold < 1f) {
            for (k in 0 until 6) {
                val fp = clamp01(unfold * 6f - k * 0.65f)
                if (fp <= 0f) continue
                val a1 = Math.toRadians((k * 60 - 90).toDouble()).toFloat()
                val a2 = Math.toRadians(((k + 1) * 60 - 90).toDouble()).toFloat()
                val rad = s * 0.58f * easeOutBack(fp)
                g.path.reset(); g.path.moveTo(cx, cy); g.path.lineTo(cx + cos(a1) * rad, cy + sin(a1) * rad); g.path.lineTo(cx + cos(a2) * rad, cy + sin(a2) * rad); g.path.close()
                g.c.drawPath(g.path, g.fillP(if (k % 2 == 0) g.p1 else g.p2, (1f - unfold * 0.7f) * 0.55f))
            }
        }
    }

    private fun fx5CyberGlitch(g: Gfx, cx: Float, cy: Float, s: Float) {
        val tau = g.tau; val w = g.w; val h = g.h; val u = g.u
        val slot = floor(tau * 20f).toInt()
        val glitchAmt = if (tau < 1.20f) (1f - clamp01((tau - 0.65f) / 0.55f)) else if (g.rnd(slot, 4) < 0.15f) 0.45f else 0.04f
        var sy = 0f
        while (sy < h) { g.line(0f, sy, w, sy, Color.WHITE, 0.04f, u * 0.1f); sy += u * 1.15f }
        g.glow(cx - u * 4f * glitchAmt, cy, s * 1.1f, g.p1, 0.25f)
        g.glow(cx + u * 4f * glitchAmt, cy, s * 1.1f, g.p2, 0.25f)
        val bars = (20 * glitchAmt).toInt().coerceAtLeast(2)
        for (i in 0 until bars) {
            val by = g.rnd(slot, i + 10) * h
            val bh = u * (0.4f + 2.6f * g.rnd(slot, i + 30))
            val bw = w * (0.10f + 0.50f * g.rnd(slot, i + 50))
            val bx = cx + (g.rnd(slot, i + 70) - 0.5f) * w * 0.75f
            g.rectFill(bx - bw / 2f, by, bx + bw / 2f, by + bh, if (i % 2 == 0) g.p1 else g.p2, 0.55f * glitchAmt + 0.06f)
        }
    }

    private fun fx6GalaxyParticles(g: Gfx, cx: Float, cy: Float, s: Float) {
        val tau = g.tau; val w = g.w; val h = g.h; val u = g.u
        val gather = easeInOut(clamp01(tau / 1.25f))
        val burst = easeOut(clamp01((tau - 1.25f) / 1.1f))
        g.glow(cx, cy, s * (0.6f + 0.8f * gather), g.p1, 0.35f * gather)
        val maxR = max(w, h) * 0.65f
        for (i in 0 until 240) {
            val arm = i % 4
            val baseAng = arm * 1.5708f + g.rnd(i) * 1.2f
            val spiral = (1f - gather) * (3.8f + g.rnd(i, 1) * 2.2f)
            val ang = baseAng + spiral + tau * 0.4f
            val startR = maxR * (0.3f + 0.7f * g.rnd(i, 2))
            val targetR = s * (0.12f + 0.38f * g.rnd(i, 3))
            var r = lerp(startR, targetR, gather)
            if (burst > 0f) r += burst * s * (0.35f + 1.1f * g.rnd(i, 4))
            val px = cx + cos(ang) * r; val py = cy + sin(ang) * r
            val col = when (i % 3) { 0 -> g.p1; 1 -> g.p2; else -> g.p3 }
            val sz = u * (0.20f + 0.48f * g.rnd(i, 5)) * (1f - burst * 0.3f)
            g.circle(px, py, sz, col, (0.35f + 0.65f * gather) * (1f - burst * 0.4f))
        }
        if (burst > 0f && burst < 1f) g.ring(cx, cy, easeOut(burst) * s * 1.45f, Color.WHITE, (1f - burst) * 0.85f, u * 0.7f)
    }

    private fun fx7LaserTrace(g: Gfx, cx: Float, cy: Float, s: Float) {
        val tau = g.tau; val w = g.w; val u = g.u
        val trace = clamp01(tau / 1.15f)
        val r = s * 0.56f
        val hex = Path()
        for (k in 0..6) {
            val a = Math.toRadians((k * 60 - 90).toDouble()).toFloat()
            val px = cx + cos(a) * r; val py = cy + sin(a) * r
            if (k == 0) hex.moveTo(px, py) else hex.lineTo(px, py)
        }
        val pm = PathMeasure(hex, false)
        val seg = Path()
        pm.getSegment(0f, pm.length * easeInOut(trace), seg, true)
        g.c.drawPath(seg, g.strokeP(g.p1, 0.38f, u * 1.8f))
        g.c.drawPath(seg, g.strokeP(Color.WHITE, 0.92f, u * 0.35f))
        if (trace < 1f) {
            val pos = FloatArray(2)
            pm.getPosTan(pm.length * easeInOut(trace), pos, null)
            g.line(0f, 0f, pos[0], pos[1], g.p1, 0.38f, u * 1.1f)
            g.line(0f, 0f, pos[0], pos[1], Color.WHITE, 0.88f, u * 0.25f)
            g.line(w, 0f, pos[0], pos[1], g.p2, 0.38f, u * 1.1f)
            g.line(w, 0f, pos[0], pos[1], Color.WHITE, 0.88f, u * 0.25f)
            g.glow(pos[0], pos[1], u * 5.5f, g.p1, 0.95f)
        } else {
            val flash = 1f - clamp01((tau - 1.15f) / 0.65f)
            g.glow(cx, cy, r * 2.2f, g.p1, 0.5f * flash + 0.22f)
        }
    }

    private fun fx8FireSweep(g: Gfx, cx: Float, cy: Float, s: Float) {
        val tau = g.tau; val w = g.w; val h = g.h; val u = g.u
        val sweep = clamp01((tau - 0.15f) / 1.25f)
        val sweepX = lerp(cx - w * 0.40f, cx + w * 0.50f, easeInOut(sweep))
        g.glow(cx, cy, s * 1.3f, g.p1, 0.35f * clamp01(tau / 0.8f))
        if (sweep > 0f && sweep < 1f) {
            val inten = sin(sweep * 3.14159f)
            g.glow(sweepX, cy, s * 0.95f, g.p2, 0.7f * inten)
            for (k in 0 until 50) {
                val fy = cy + (g.rnd(k) - 0.5f) * h * 0.55f
                val fx = sweepX + (g.rnd(k, 1) - 0.5f) * u * 10f - g.rnd(k, 2) * u * 12f
                val r = u * (1.2f + 3.8f * g.rnd(k, 3)) * inten
                g.circle(fx, fy, r, mixColor(g.p3, g.p1, g.rnd(k, 4)), 0.8f * inten)
            }
        }
        for (i in 0 until 75) {
            val ph = fmod(tau * (0.45f + 0.4f * g.rnd(i, 1)) + g.rnd(i), 1f)
            val ang = g.rnd(i, 2) * 6.2832f
            val baseR = s * (0.35f + 0.45f * g.rnd(i, 3))
            val x = cx + cos(ang) * baseR + sin(tau * 3f + i) * u * 1.5f
            val y = cy + sin(ang) * baseR - ph * h * 0.32f
            g.circle(x, y, u * (0.45f * (1f - ph) + 0.12f), mixColor(g.p2, g.p1, ph), (1f - ph) * 0.85f * clamp01(tau / 0.6f))
        }
    }

    private fun fx9MinimalShutter(g: Gfx, cx: Float, cy: Float, s: Float, textLeft: Float, textWidth: Float, by1: Float) {
        val tau = g.tau; val u = g.u
        val p = easeOut(clamp01(tau / 0.85f))
        val divX = (cx + s * 0.5f + textLeft) * 0.5f
        val divH = s * 0.68f * p
        g.line(divX, cy - divH, divX, cy + divH, g.p2, 0.85f * p, u * 0.38f)
        val left = cx - s * 0.65f; val right = textLeft + textWidth + s * 0.25f
        val top = cy - s * 0.72f; val bot = cy + s * 0.72f; val tl = u * 3.5f * p
        g.line(left, top, left + tl, top, g.p1, 0.65f * p, u * 0.3f)
        g.line(left, top, left, top + tl, g.p1, 0.65f * p, u * 0.3f)
        g.line(right, bot, right - tl, bot, g.p1, 0.65f * p, u * 0.3f)
        g.line(right, bot, right, bot - tl, g.p1, 0.65f * p, u * 0.3f)
    }

    private fun fx10NeonTube(g: Gfx, cx: Float, cy: Float, s: Float) {
        val tau = g.tau; val w = g.w; val h = g.h; val u = g.u
        val trace = easeInOut(clamp01(tau / 0.95f))
        val flicker = if (tau < 0.85f && hash(floor(tau * 25f).toInt(), 3) < 0.32f) 0.22f else 0.92f + 0.08f * sin(tau * 9f)
        g.glow(cx - u * 5f, cy, s * 1.25f, g.p1, 0.30f * flicker * trace)
        g.glow(cx + u * 5f, cy, s * 1.25f, g.p2, 0.30f * flicker * trace)
        for (k in 0 until 2) {
            val half = s * (0.62f - k * 0.10f); val rad = u * (4.5f - k * 1.0f)
            val p = Path(); g.rect.set(cx - half, cy - half, cx + half, cy + half)
            p.addRoundRect(g.rect, rad, rad, Path.Direction.CW)
            val pm = PathMeasure(p, false); val seg = Path()
            pm.getSegment(0f, pm.length * trace, seg, true)
            val col = if (k == 0) g.p1 else g.p2
            g.c.drawPath(seg, g.strokeP(col, 0.28f * flicker, u * 2.2f))
            g.c.drawPath(seg, g.strokeP(col, 0.78f * flicker, u * 0.85f))
            g.c.drawPath(seg, g.strokeP(Color.WHITE, 0.92f * flicker, u * 0.28f))
        }
        val floorY = h * 0.85f
        for (i in 0 until 4) {
            val rw = w * (0.38f - i * 0.07f) * trace; val y = floorY + i * u * 1.8f
            g.line(cx - rw / 2f, y, cx + rw / 2f, y, if (i % 2 == 0) g.p1 else g.p2, (0.35f - i * 0.07f) * flicker, u * 0.35f)
        }
    }

    private fun fx11BouncingBall(g: Gfx, logoCx: Float, logoCy: Float, emblemSize: Float, textLeft: Float, textWidth: Float, by1: Float, bs: Float) {
        val tau = g.tau; val u = g.u; val bounceEnd = 1.65f
        if (tau < bounceEnd) {
            val p = clamp01(tau / bounceEnd)
            val bx = if (p < 0.78f) lerp(textLeft + textWidth * 0.95f, textLeft + bs * 0.2f, p / 0.78f)
            else lerp(textLeft + bs * 0.2f, logoCx, easeInOut((p - 0.78f) / 0.22f))
            val hopPhase = abs(sin(p * 5f * 3.1415927f))
            val baseTopY = if (p < 0.78f) (by1 - bs * 0.82f) else lerp(by1 - bs * 0.82f, logoCy, (p - 0.78f) / 0.22f)
            val by = baseTopY - hopPhase * emblemSize * 0.55f
            val ballR = u * 2.4f
            val squash = if (hopPhase < 0.18f) 1.25f else 0.92f
            g.glow(bx, by, ballR * 2.8f, g.p1, 0.55f)
            g.c.save(); g.c.translate(bx, by); g.c.scale(squash, 1f / squash)
            g.circle(0f, 0f, ballR, g.p1, 1f)
            g.circle(-ballR * 0.28f, -ballR * 0.28f, ballR * 0.32f, Color.WHITE, 0.8f)
            g.c.restore()
        } else {
            val burst = clamp01((tau - bounceEnd) / 0.75f)
            if (burst < 1f) {
                g.ring(logoCx, logoCy, emblemSize * (0.35f + 0.85f * easeOut(burst)), g.p1, (1f - burst) * 0.8f, u * 0.6f)
                g.ring(logoCx, logoCy, emblemSize * (0.25f + 0.60f * easeOut(burst)), g.p2, (1f - burst) * 0.7f, u * 0.35f)
            }
        }
    }

    private fun fx12RoyalRibbon(g: Gfx, cx: Float, cy: Float, s: Float) {
        val tau = g.tau; val w = g.w; val h = g.h; val u = g.u
        val p = easeInOut(clamp01(tau / 1.4f))
        g.glow(cx, cy, s * 1.35f, g.p1, 0.22f * p)
        for (k in 0..1) {
            val r = s * (0.64f - k * 0.08f)
            g.rect.set(cx - r, cy - r, cx + r, cy + r)
            val dir = if (k == 0) 1f else -1f
            g.c.drawArc(g.rect, -90f, 360f * p * dir, false, g.strokeP(if (k == 0) g.p1 else g.p2, 0.85f, u * (0.45f - k * 0.15f)))
        }
        for (i in 0 until 50) {
            val tw = 0.35f + 0.65f * abs(sin(tau * 2.2f + i))
            val x = g.rnd(i) * w
            val y = h - fmod(g.rnd(i, 1) * h + tau * u * (4f + 8f * g.rnd(i, 2)), h)
            g.circle(x, y, u * (0.14f + 0.28f * g.rnd(i, 3)), g.p1, tw * 0.8f * p)
        }
    }

    private fun fx13LiquidMetaball(g: Gfx, cx: Float, cy: Float, s: Float) {
        val tau = g.tau; val u = g.u
        val conv = easeInOut(clamp01(tau / 1.05f))
        val ripple = clamp01((tau - 1.0f) / 1.0f)
        g.glow(cx, cy, s * 1.15f, g.p1, 0.25f * conv)
        if (conv < 0.98f) {
            for (i in 0 until 6) {
                val ang = i * 6.2832f / 6f + (1f - conv) * 1.6f
                val dist = (1f - conv) * s * 1.45f
                val bx = cx + cos(ang) * dist; val by = cy + sin(ang) * dist
                val rad = s * (0.16f + 0.18f * conv)
                g.circle(bx, by, rad, when (i % 3) { 0 -> g.p1; 1 -> g.p2; else -> g.p3 }, 0.82f)
                g.circle(bx - rad * 0.26f, by - rad * 0.26f, rad * 0.28f, Color.WHITE, 0.55f)
            }
        }
        if (ripple > 0f && ripple < 1f) {
            g.ring(cx, cy, s * (0.4f + 0.85f * easeOut(ripple)), g.p1, (1f - ripple) * 0.75f, u * 0.55f)
            g.ring(cx, cy, s * (0.3f + 0.55f * easeOut(ripple)), g.p2, (1f - ripple) * 0.6f, u * 0.3f)
        }
    }

    private fun fx14IsometricCubeMatrix(g: Gfx, cx: Float, cy: Float, s: Float) {
        val tau = g.tau; val u = g.u
        val p = easeOut(clamp01(tau / 1.1f))
        g.glow(cx, cy, s * 1.3f, g.p1, 0.28f * p)
        val cubeR = u * 2.4f
        for (i in 0 until 12) {
            val cp = clamp01((tau - i * 0.05f) / 0.65f)
            if (cp <= 0f) continue
            val ang = Math.toRadians((i * 30).toDouble()).toFloat()
            val dist = s * (0.72f - 0.10f * easeOut(cp))
            val x = cx + cos(ang) * dist
            val y = cy + sin(ang) * dist + (1f - easeOutBack(cp)) * u * 8f
            drawIsoCube(g, x, y, cubeR * easeOut(cp), g.p1, g.p2, 0.65f * cp)
        }
    }

    private fun drawIsoCube(g: Gfx, cx: Float, cy: Float, r: Float, c1: Int, c2: Int, alpha: Float) {
        val dx = r * 0.866f; val dy = r * 0.5f
        g.path.reset(); g.path.moveTo(cx, cy - r); g.path.lineTo(cx + dx, cy - dy); g.path.lineTo(cx, cy); g.path.lineTo(cx - dx, cy - dy); g.path.close()
        g.c.drawPath(g.path, g.fillP(mixColor(c2, Color.WHITE, 0.35f), alpha))
        g.path.reset(); g.path.moveTo(cx - dx, cy - dy); g.path.lineTo(cx, cy); g.path.lineTo(cx, cy + r); g.path.lineTo(cx - dx, cy + dy); g.path.close()
        g.c.drawPath(g.path, g.fillP(c1, alpha))
        g.path.reset(); g.path.moveTo(cx, cy); g.path.lineTo(cx + dx, cy - dy); g.path.lineTo(cx + dx, cy + dy); g.path.lineTo(cx, cy + r); g.path.close()
        g.c.drawPath(g.path, g.fillP(mixColor(c1, Color.BLACK, 0.35f), alpha))
    }

    private fun fx15LightningShockwave(g: Gfx, cx: Float, cy: Float, s: Float) {
        val tau = g.tau; val u = g.u
        g.glow(cx, cy, s * 1.3f, g.p1, 0.30f * clamp01(tau / 0.8f))
        if (tau in 0.20f..1.15f) {
            val slot = floor(tau * 12f).toInt()
            val inten = 1f - clamp01((tau - 0.85f) / 0.30f)
            var px = cx + (g.rnd(slot, 1) - 0.5f) * s * 0.8f; var py = 0f
            for (k in 1..10) {
                val ny = lerp(0f, cy, k / 10f)
                val nx = if (k == 10) cx else cx + (g.rnd(slot, k + 10) - 0.5f) * u * 9f
                g.line(px, py, nx, ny, g.p2, 0.45f * inten, u * 1.8f)
                g.line(px, py, nx, ny, Color.WHITE, 0.95f * inten, u * 0.4f)
                px = nx; py = ny
            }
        }
        val sp = clamp01((tau - 0.85f) / 0.95f)
        if (sp > 0f && sp < 1f) {
            g.ring(cx, cy, easeOut(sp) * s * 1.6f, Color.WHITE, (1f - sp) * 0.9f, u * 0.9f)
            g.ring(cx, cy, easeOut(sp) * s * 1.1f, g.p1, (1f - sp) * 0.8f, u * 0.5f)
        }
    }

    private fun fx16ZenInkBrush(g: Gfx, cx: Float, cy: Float, s: Float) {
        val tau = g.tau; val u = g.u
        val sweep = easeOut(clamp01(tau / 1.15f))
        val r = s * 0.62f
        for (pass in 0..3) {
            val rr = r + (pass - 1.5f) * u * 0.45f
            g.rect.set(cx - rr, cy - rr, cx + rr, cy + rr)
            g.c.drawArc(g.rect, 130f + pass * 4f, (315f - pass * 12f) * sweep, false, g.strokeP(g.p1, 0.75f - pass * 0.12f, u * (1.35f - pass * 0.22f)))
        }
        for (i in 0 until 32) {
            if (i / 32f > sweep) break
            val ang = Math.toRadians((130.0 + i * 9.5)).toFloat()
            val dist = r + (g.rnd(i) - 0.5f) * u * 5.5f
            g.circle(cx + cos(ang) * dist, cy + sin(ang) * dist, u * (0.2f + 0.6f * g.rnd(i, 1)), g.p1, 0.65f)
        }
    }

    private fun fx17QuantumPortal(g: Gfx, cx: Float, cy: Float, s: Float) {
        val tau = g.tau; val u = g.u
        val open = easeOutBack(clamp01(tau / 1.0f))
        val r = s * 0.62f * open
        if (r <= 1f) return
        g.glow(cx, cy, r * 1.8f, g.p1, 0.38f)
        g.glow(cx, cy, r * 1.2f, g.p2, 0.28f)
        g.ring(cx, cy, r, g.p1, 0.9f, u * 0.65f)
        g.ring(cx, cy, r * 0.94f, Color.WHITE, 0.85f, u * 0.22f)
        for (i in 0 until 50) {
            val ang = i * 6.2832f / 50f + tau * 3.8f
            val px = cx + cos(ang) * r; val py = cy + sin(ang) * r
            val tx = -sin(ang) * u * (2.5f + 4.5f * g.rnd(i))
            val ty = cos(ang) * u * (2.5f + 4.5f * g.rnd(i))
            g.line(px, py, px + tx, py + ty, if (i % 3 == 0) Color.WHITE else g.p1, 0.8f, u * 0.26f)
        }
    }

    private fun fx18AudioEqualizer(g: Gfx, cx: Float, cy: Float, s: Float) {
        val tau = g.tau; val u = g.u
        val on = easeOut(clamp01(tau / 0.8f))
        val baseR = s * 0.58f * on
        g.glow(cx, cy, s * 1.25f, g.p1, 0.28f * on)
        for (i in 0 until 36) {
            val ang = i * 6.2832f / 36f + tau * 0.35f
            val amp = u * (1.2f + 4.8f * abs(sin(tau * 7.5f + i * 0.7f)) * g.rnd(i, floor(tau * 8f).toInt())) * on
            g.line(
                cx + cos(ang) * baseR, cy + sin(ang) * baseR,
                cx + cos(ang) * (baseR + amp), cy + sin(ang) * (baseR + amp),
                when (i % 3) { 0 -> g.p1; 1 -> g.p2; else -> g.p3 }, 0.88f, u * 0.42f
            )
        }
    }

    private fun fx19BlueprintScan(g: Gfx, cx: Float, cy: Float, s: Float, textLeft: Float, textWidth: Float, by1: Float) {
        val tau = g.tau; val w = g.w; val h = g.h; val u = g.u
        val step = u * 6.5f
        var gx = 0f; while (gx < w) { g.line(gx, 0f, gx, h, g.p1, 0.08f, u * 0.1f); gx += step }
        var gy = 0f; while (gy < h) { g.line(0f, gy, w, gy, g.p1, 0.08f, u * 0.1f); gy += step }
        val p = easeOut(clamp01(tau / 0.9f))
        val r = s * 0.65f * p
        g.ring(cx, cy, r, g.p1, 0.45f, u * 0.2f)
        for (k in 0 until 24) {
            if (k / 24f > p) break
            val a = Math.toRadians((k * 15).toDouble()).toFloat()
            val r2 = r + (if (k % 3 == 0) u * 1.6f else u * 0.8f)
            g.line(cx + cos(a) * r, cy + sin(a) * r, cx + cos(a) * r2, cy + sin(a) * r2, g.p1, 0.55f, u * 0.16f)
        }
    }

    private fun fx20AnamorphicFlare(g: Gfx, cx: Float, cy: Float, s: Float) {
        val tau = g.tau; val w = g.w; val h = g.h; val u = g.u
        val p = easeOut(clamp01(tau / 1.1f))
        g.glow(cx, cy, s * 1.5f, g.p1, 0.35f * p)
        g.glow(cx, cy, s * 0.9f, g.p2, 0.28f * p)
        val streakW = w * 0.85f * p
        g.paint.reset(); g.paint.isAntiAlias = true
        g.paint.shader = LinearGradient(
            cx - streakW / 2f, cy, cx + streakW / 2f, cy,
            intArrayOf(withAlpha(g.p1, 0f), withAlpha(g.p1, 0.8f * p), withAlpha(Color.WHITE, 0.95f * p), withAlpha(g.p2, 0.8f * p), withAlpha(g.p2, 0f)),
            floatArrayOf(0f, 0.35f, 0.5f, 0.65f, 1f), Shader.TileMode.CLAMP
        )
        g.c.drawRect(cx - streakW / 2f, cy - u * 0.45f, cx + streakW / 2f, cy + u * 0.45f, g.paint)
        for (i in 0 until 45) {
            val tw = 0.3f + 0.7f * abs(sin(tau * 2.2f + i))
            g.circle(g.rnd(i) * w, g.rnd(i, 1) * h, u * (0.15f + 0.32f * g.rnd(i, 2)), if (i % 3 == 0) g.p2 else Color.WHITE, tw * 0.65f * p)
        }
    }

    // =========================================================================================================
    // SECTION 2 STAGES: 21..40 (20 Streaming & Cinema Hero Text Intros: Netflix / Prime / Hotstar / etc.)
    // =========================================================================================================

    // 21 — Netflix "N" Ribbon & Prism Spectrum Bars
    private fun fx21NetflixPrismStage(g: Gfx) {
        val tau = g.tau; val w = g.w; val h = g.h; val cx = g.cx; val cy = g.cy
        g.glow(cx, cy, w * 0.65f, g.p1, 0.28f * clamp01(tau / 0.8f))
        // Prism light-spectrum stage starting around tau = 2.6s (Netflix prism rush)
        val prismP = clamp01((tau - 2.5f) / 2.2f)
        if (prismP > 0f) {
            val cols = intArrayOf(g.p1, g.p2, 0xFFFF8800.toInt(), g.p3, 0xFFB026FF.toInt(), Color.WHITE)
            val n = 64
            for (i in 0 until n) {
                val x = (i + 0.5f) / n * w
                val distFromCenter = abs(x - cx) / (w * 0.5f)
                val barAppear = clamp01((prismP - distFromCenter * 0.35f) / 0.65f)
                if (barAppear <= 0f) continue
                val hh = h * (0.3f + 0.7f * barAppear) * (0.7f + 0.3f * g.rnd(i, 1))
                val col = cols[i % cols.size]
                val sw = (w / n) * (0.55f + 0.65f * barAppear)
                g.line(x, cy - hh * 0.5f, x, cy + hh * 0.5f, col, 0.78f * barAppear, sw)
            }
        }
    }

    // 22 — Prime Video Cyan Smile Arrow Sweep
    private fun fx22PrimeSmileStage(g: Gfx, textLeft: Float, textWidth: Float, by1: Float, bs: Float) {
        val tau = g.tau; val cx = g.cx; val cy = g.cy; val u = g.u
        g.glow(cx, cy, g.w * 0.55f, g.p1, 0.22f * clamp01(tau / 0.8f))
        // Electric-cyan curved smile arc sweeping from first letter to last letter under the brand name
        val smileP = easeInOut(clamp01((tau - 0.75f) / 0.95f))
        if (smileP > 0f) {
            val startX = cx - textWidth * 0.42f
            val endX = cx + textWidth * 0.42f
            val smileY = by1 + bs * 0.28f
            val dipY = smileY + bs * 0.42f
            val smilePath = Path().apply {
                moveTo(startX, smileY)
                quadTo(cx, dipY, endX, smileY - bs * 0.04f)
            }
            val pm = PathMeasure(smilePath, false)
            val seg = Path()
            pm.getSegment(0f, pm.length * smileP, seg, true)
            g.c.drawPath(seg, g.strokeP(g.p1, 0.35f, u * 2.2f))
            g.c.drawPath(seg, g.strokeP(g.p2, 0.95f, u * 0.85f))
            g.c.drawPath(seg, g.strokeP(Color.WHITE, 0.85f, u * 0.28f))

            // Arrowhead tip at the end of the smile curve
            val pos = FloatArray(2)
            pm.getPosTan(pm.length * smileP, pos, null)
            g.glow(pos[0], pos[1], u * 4.5f, g.p2, 0.85f)
            if (smileP > 0.85f) {
                val ah = u * 2.2f * ((smileP - 0.85f) / 0.15f)
                g.line(pos[0], pos[1], pos[0] - ah * 1.1f, pos[1] - ah * 0.4f, g.p2, 0.95f, u * 0.65f)
                g.line(pos[0], pos[1], pos[0] - ah * 0.5f, pos[1] + ah * 1.0f, g.p2, 0.95f, u * 0.65f)
            }
        }
    }

    // 23 — Hotstar / Disney+ Shooting Star Constellation Arc
    private fun fx23HotstarArcStage(g: Gfx, textLeft: Float, textWidth: Float, by1: Float, bs: Float) {
        val tau = g.tau; val w = g.w; val h = g.h; val cx = g.cx; val u = g.u
        // Twinkling night sky stars
        for (i in 0 until 90) {
            val tw = 0.3f + 0.7f * abs(sin(tau * 2.5f + i))
            g.circle(g.rnd(i) * w, g.rnd(i, 1) * h * 0.75f, u * (0.12f + 0.22f * g.rnd(i, 2)), Color.WHITE, tw * 0.7f)
        }
        // Shooting star parabolic arc over the brand title
        val arcP = easeInOut(clamp01((tau - 0.25f) / 1.45f))
        if (arcP > 0f) {
            val sx = cx - max(textWidth * 0.58f, w * 0.28f)
            val ex = cx + max(textWidth * 0.58f, w * 0.28f)
            val baseY = by1 - bs * 0.15f
            val peakY = by1 - bs * 2.45f
            val arcPath = Path().apply {
                moveTo(sx, baseY)
                quadTo(cx, peakY, ex, baseY)
            }
            val pm = PathMeasure(arcPath, false)
            val seg = Path()
            pm.getSegment(max(0f, pm.length * (arcP - 0.45f)), pm.length * arcP, seg, true)
            g.c.drawPath(seg, g.strokeP(g.p1, 0.45f, u * 1.8f))
            g.c.drawPath(seg, g.strokeP(g.p2, 0.92f, u * 0.65f))
            g.c.drawPath(seg, g.strokeP(Color.WHITE, 0.95f, u * 0.25f))

            val pos = FloatArray(2)
            pm.getPosTan(pm.length * arcP, pos, null)
            drawStarburstFlash(g, pos[0], pos[1], bs * 0.85f, sin(arcP * 3.14159f).coerceAtLeast(0.25f))

            // Sparkling stardust raining down from the arc
            for (i in 0 until 45) {
                val f = (i / 45f)
                if (f > arcP) break
                val p2Pos = FloatArray(2)
                pm.getPosTan(pm.length * f, p2Pos, null)
                val fall = (arcP - f) * bs * (0.4f + 0.9f * g.rnd(i, 3))
                val alpha = (1f - (arcP - f) * 1.4f).coerceIn(0f, 1f)
                g.circle(p2Pos[0] + (g.rnd(i) - 0.5f) * u * 3f, p2Pos[1] + fall, u * 0.28f, if (i % 2 == 0) g.p2 else Color.WHITE, alpha * 0.85f)
            }
        }
    }

    // 24 — Marvel Flipbook Monolith Stage
    private fun fx24MarvelFlipStage(g: Gfx) {
        val tau = g.tau; val w = g.w; val h = g.h; val cx = g.cx; val cy = g.cy; val u = g.u
        // Rapid comic storyboard bars flipping in the background during 0.0s..1.4s
        val flipP = 1f - clamp01((tau - 1.1f) / 0.5f)
        if (flipP > 0f) {
            val slot = floor(tau * 24f).toInt()
            for (i in 0 until 14) {
                val y = g.rnd(slot, i) * h
                val x = g.rnd(slot, i + 20) * w
                g.line(x - w * 0.2f, y, x + w * 0.2f, y - u * 4f, Color.WHITE, 0.18f * flipP, u * 0.35f)
            }
        }
        // Glowing crimson monolith frame locking around the center wordmark
        val lockP = easeOutBack(clamp01((tau - 0.85f) / 0.7f))
        if (lockP > 0f) {
            val mw = w * 0.68f * lockP
            val mh = h * 0.24f * lockP
            g.glow(cx, cy, mw * 0.7f, g.p1, 0.45f * lockP)
            g.rectFill(cx - mw * 0.5f, cy - mh * 0.5f, cx + mw * 0.5f, cy + mh * 0.5f, g.p1, 0.92f * lockP)
            g.c.drawRect(cx - mw * 0.5f, cy - mh * 0.5f, cx + mw * 0.5f, cy + mh * 0.5f, g.strokeP(Color.WHITE, 0.85f * lockP, u * 0.45f))
        }
    }

    // 25 — Apple TV+ Liquid Glass Refraction Stage
    private fun fx25AppleGlassStage(g: Gfx) {
        val tau = g.tau; val cx = g.cx; val cy = g.cy; val h = g.h; val u = g.u
        val p = easeInOut(clamp01(tau / 1.6f))
        for (k in 0..3) {
            val r = h * (0.18f + k * 0.08f) * (1.25f - 0.25f * p)
            val ox = sin(tau * 1.2f + k) * u * 3f * (1f - p)
            val oy = cos(tau * 1.0f + k) * u * 2f * (1f - p)
            g.glow(cx + ox, cy + oy, r * 1.4f, if (k % 2 == 0) g.p1 else g.p2, 0.18f * (1f - 0.3f * p))
            g.ring(cx + ox, cy + oy, r, if (k % 2 == 0) g.p1 else g.p2, (0.35f - k * 0.06f) * (1f - 0.4f * p), u * 0.35f)
        }
    }

    // 26 — HBO 3D Chrome Spotlight Stage
    private fun fx26HboChromeStage(g: Gfx) {
        val tau = g.tau; val w = g.w; val h = g.h; val cx = g.cx; val u = g.u
        // Analog static sparkle at very start (0.0s..0.6s)
        val staticAmt = 1f - clamp01(tau / 0.65f)
        if (staticAmt > 0f) {
            val slot = floor(tau * 30f).toInt()
            for (i in 0 until 90) {
                g.circle(g.rnd(slot, i) * w, g.rnd(slot, i + 100) * h, u * 0.25f, Color.WHITE, 0.35f * staticAmt)
            }
        }
        // Overhead sweeping spotlight cone
        val spotX = cx + sin(tau * 1.1f) * w * 0.32f
        g.glow(spotX, h * 0.25f, h * 0.75f, g.p2, 0.28f * clamp01(tau / 0.8f))
    }

    // 27 — IMAX Dual Laser Projector Stage
    private fun fx27ImaxLaserStage(g: Gfx) {
        val tau = g.tau; val w = g.w; val h = g.h; val cx = g.cx; val cy = g.cy; val u = g.u
        val p = easeOut(clamp01(tau / 1.1f))
        // Twin volumetric projector cones from top-left and top-right intersecting at center
        for (side in 0..1) {
            val ox = if (side == 0) 0f else w
            g.path.reset()
            g.path.moveTo(ox, 0f)
            g.path.lineTo(cx - w * 0.25f, h)
            g.path.lineTo(cx + w * 0.25f, h)
            g.path.close()
            g.c.drawPath(g.path, g.fillP(if (side == 0) g.p1 else g.p2, 0.14f * p))
        }
        g.line(0f, cy, w, cy, g.p2, 0.55f * p, u * 0.3f)
        g.glow(cx, cy, w * 0.55f, g.p1, 0.32f * p)
    }

    // 28 — Dolby Atmos Acoustic Sound-Sphere Stage
    private fun fx28DolbySphereStage(g: Gfx) {
        val tau = g.tau; val cx = g.cx; val cy = g.cy; val h = g.h; val u = g.u
        val p = easeInOut(clamp01(tau / 1.2f))
        g.glow(cx, cy, h * 0.65f, g.p1, 0.25f * p)
        for (i in 0 until 6) {
            val wave = fmod(tau * 0.55f + i / 6f, 1f)
            g.ring(cx, cy, wave * h * 0.55f, g.p1, (1f - wave) * 0.55f * p, u * 0.35f)
        }
    }

    // 29 — Universal Planetary Horizon Orbit Stage
    private fun fx29UniversalOrbitStage(g: Gfx) {
        val tau = g.tau; val w = g.w; val h = g.h; val cx = g.cx; val u = g.u
        val p = easeOut(clamp01(tau / 1.3f))
        val planetR = w * 0.62f
        val planetCy = h * 0.52f + planetR * 0.82f
        // Rim sunrise glow along planetary curvature
        g.glow(cx, h * 0.42f, w * 0.55f, g.p2, 0.38f * p)
        g.glow(cx, h * 0.48f, w * 0.75f, g.p1, 0.32f * p)
        g.circle(cx, planetCy, planetR, 0xFF020612.toInt(), 0.96f)
        g.ring(cx, planetCy, planetR, g.p1, 0.85f * p, u * 0.65f)
        g.ring(cx, planetCy, planetR - u * 0.4f, Color.WHITE, 0.75f * p, u * 0.25f)
    }

    // 30 — Columbia Torch Light Beam Pillar Stage
    private fun fx30TorchPillarStage(g: Gfx) {
        val tau = g.tau; val w = g.w; val h = g.h; val cx = g.cx; val cy = g.cy; val u = g.u
        val p = easeOut(clamp01(tau / 1.2f))
        g.glow(cx, cy * 0.65f, h * 0.65f, g.p1, 0.45f * p)
        val rays = 18
        for (k in 0 until rays) {
            val a = Math.toRadians(k * 360.0 / rays + tau * 6.0).toFloat()
            val len = max(w, h) * 0.85f
            g.line(cx, cy * 0.65f, cx + cos(a) * len, cy * 0.65f + sin(a) * len, g.p2, 0.14f * p, u * 2.2f)
        }
        drawStarburstFlash(g, cx, cy * 0.62f, h * 0.28f, 0.65f * p)
    }

    // 31 — JioCinema Magenta Stadium Wave Stage
    private fun fx31JioStadiumStage(g: Gfx) {
        val tau = g.tau; val w = g.w; val h = g.h; val cx = g.cx; val cy = g.cy; val u = g.u
        val p = easeOut(clamp01(tau / 1.0f))
        g.glow(cx, cy, w * 0.65f, g.p1, 0.35f * p)
        for (i in 0 until 5) {
            val yBase = h * (0.35f + i * 0.08f)
            g.path.reset(); g.path.moveTo(0f, yBase)
            for (s in 0..30) {
                val x = w * s / 30f
                val y = yBase + sin(x / w * 6.28f + tau * 3f + i) * u * 4.5f
                g.path.lineTo(x, y)
            }
            g.c.drawPath(g.path, g.strokeP(if (i % 2 == 0) g.p1 else g.p2, 0.45f * p, u * 0.55f))
        }
    }

    // 32 — Zee5 Aurora Silk Ribbon Stage
    private fun fx32AuroraSilkStage(g: Gfx) {
        val tau = g.tau; val w = g.w; val h = g.h; val cy = g.cy; val u = g.u
        val p = easeInOut(clamp01(tau / 1.2f))
        val cols = intArrayOf(g.p1, g.p2, g.p3)
        for (r in 0..2) {
            g.path.reset()
            val maxS = (40 * p).toInt().coerceAtLeast(2)
            for (s in 0..maxS) {
                val x = w * s / 40f
                val y = cy + sin(x / w * 5.5f + tau * 2.4f + r * 1.4f) * h * 0.16f
                if (s == 0) g.path.moveTo(x, y) else g.path.lineTo(x, y)
            }
            g.c.drawPath(g.path, g.strokeP(cols[r], 0.55f, u * (2.4f - r * 0.5f)))
        }
    }

    // 33 — A24 Editorial Split-Frame Stage
    private fun fx33A24EditorialStage(g: Gfx) {
        val tau = g.tau; val w = g.w; val h = g.h; val u = g.u
        val p = easeInOut(clamp01(tau / 0.9f))
        val ins = u * 5.5f
        g.c.drawRect(ins, ins, w - ins, h - ins, g.strokeP(Color.WHITE, 0.45f * p, u * 0.22f))
        g.line(ins, h * 0.5f, ins + u * 6f * p, h * 0.5f, g.p2, 0.8f * p, u * 0.3f)
        g.line(w - ins, h * 0.5f, w - ins - u * 6f * p, h * 0.5f, g.p2, 0.8f * p, u * 0.3f)
    }

    // 34 — Hulu Emerald Sonic Pulse Stage
    private fun fx34HuluSonicStage(g: Gfx, textWidth: Float, bs: Float) {
        val tau = g.tau; val cx = g.cx; val cy = g.cy; val u = g.u
        val p = easeOutBack(clamp01(tau / 0.95f))
        g.glow(cx, cy, max(textWidth, bs * 4f) * 0.8f, g.p1, 0.28f * p)
        for (k in 0..2) {
            val pulse = fmod(tau * 0.7f + k * 0.33f, 1f)
            val hw = (textWidth * 0.58f + bs * (0.6f + pulse * 1.4f)) * p
            val hh = (bs * (0.95f + pulse * 0.95f)) * p
            g.rect.set(cx - hw, cy - hh, cx + hw, cy + hh)
            g.c.drawRoundRect(g.rect, u * 4f, u * 4f, g.strokeP(g.p1, (1f - pulse) * 0.65f * p, u * 0.45f))
        }
    }

    // 35 — Paramount 22-Star Halo Arc Stage
    private fun fx35ParamountStarHaloStage(g: Gfx, by1: Float, bs: Float) {
        val tau = g.tau; val cx = g.cx; val u = g.u
        val p = easeOut(clamp01(tau / 1.35f))
        val rx = bs * 3.8f; val ry = bs * 2.1f
        val arcCy = by1 - bs * 0.2f
        g.glow(cx, arcCy - ry * 0.5f, rx * 1.2f, g.p1, 0.28f * p)
        val stars = 22
        for (i in 0 until stars) {
            val sp = clamp01((p * 1.3f) - (i / stars.toFloat()) * 0.4f)
            if (sp <= 0f) continue
            val ang = Math.toRadians((195.0 + i * (150.0 / (stars - 1)))).toFloat()
            val sx = cx + cos(ang) * rx * easeOutBack(sp)
            val sy = arcCy + sin(ang) * ry * easeOutBack(sp)
            g.circle(sx, sy, u * 0.55f * sp, g.p2, 0.95f * sp)
            g.glow(sx, sy, u * 1.8f * sp, Color.WHITE, 0.55f * sp)
        }
    }

    // 36 — Warner 3D Golden Shield Monolith Stage
    private fun fx36WarnerMonolithStage(g: Gfx) {
        val tau = g.tau; val cx = g.cx; val cy = g.cy; val h = g.h
        val p = easeOut(clamp01(tau / 1.2f))
        g.glow(cx, cy, h * 0.65f, g.p1, 0.32f * p)
        // Rotating 3D golden shield watermark in background
        val shieldAlpha = (1f - 0.55f * clamp01((tau - 1.0f) / 0.8f)) * 0.45f * p
        g.c.save()
        g.c.translate(cx, cy)
        g.c.scale(cos(tau * 1.2f).coerceAtLeast(0.25f), 1f)
        drawUniversityCrest(g, h * 0.48f, g.p1, g.p2, shieldAlpha)
        g.c.restore()
    }

    // 37 — Peacock 6-Petal Prism Color Bloom Stage
    private fun fx37PeacockBloomStage(g: Gfx, by1: Float, bs: Float) {
        val tau = g.tau; val cx = g.cx; val u = g.u
        val p = easeOutBack(clamp01(tau / 1.1f))
        val cols = intArrayOf(
            0xFFFBBF24.toInt(), 0xFFF97316.toInt(), 0xFFEF4444.toInt(),
            0xFFA855F7.toInt(), 0xFF3B82F6.toInt(), 0xFF10B981.toInt()
        )
        val baseCy = by1 - bs * 0.85f
        for (k in 0 until 6) {
            val deg = (-75f + k * 30f) * p
            g.c.save()
            g.c.translate(cx, baseCy)
            g.c.rotate(deg)
            g.rect.set(-u * 2.2f, -bs * 1.9f * p, u * 2.2f, 0f)
            g.c.drawRoundRect(g.rect, u * 2.2f, u * 2.2f, g.fillP(cols[k], 0.55f * p))
            g.c.restore()
        }
    }

    // 38 — YouTube Originals Red Play-Triangle Split Stage
    private fun fx38RedPlaySplitStage(g: Gfx, textWidth: Float, by1: Float, bs: Float) {
        val tau = g.tau; val cx = g.cx; val cy = by1 - bs * 0.35f
        val split = easeInOut(clamp01((tau - 0.35f) / 0.85f))
        val triR = bs * 0.85f
        val offset = split * (textWidth * 0.58f + triR * 0.8f)
        // Left & right crimson play chevrons framing the brand title
        for (dir in intArrayOf(-1, 1)) {
            val tx = cx + dir * offset
            g.glow(tx, cy, triR * 1.6f, g.p1, 0.35f)
            g.path.reset()
            g.path.moveTo(tx - dir * triR * 0.45f, cy - triR * 0.65f)
            g.path.lineTo(tx + dir * triR * 0.55f, cy)
            g.path.lineTo(tx - dir * triR * 0.45f, cy + triR * 0.65f)
            g.path.close()
            g.c.drawPath(g.path, g.fillP(g.p1, 0.92f))
        }
    }

    // 39 — Cyberpunk Diagonal Kinetic Slash Stage
    private fun fx39CyberCutStage(g: Gfx) {
        val tau = g.tau; val w = g.w; val h = g.h; val cx = g.cx; val cy = g.cy; val u = g.u
        val p = easeOut(clamp01(tau / 0.8f))
        // Diagonal cyber slash line across the screen
        g.line(0f, h * 0.68f, w * p, h * 0.32f, g.p2, 0.85f, u * 0.55f)
        g.line(w * (1f - p), h * 0.70f, w, h * 0.34f, g.p3, 0.65f, u * 0.35f)
        g.glow(cx, cy, w * 0.55f, g.p2, 0.20f * p)
    }

    // 40 — Epic Trailer Molten Titanium Forge Stage
    private fun fx40MoltenTitaniumStage(g: Gfx) {
        val tau = g.tau; val w = g.w; val h = g.h; val cx = g.cx; val cy = g.cy; val u = g.u
        val heat = 1f - 0.55f * clamp01((tau - 1.2f) / 1.4f)
        g.glow(cx, cy, w * 0.65f, g.p1, 0.42f * heat)
        for (i in 0 until 65) {
            val ph = fmod(tau * (0.4f + 0.5f * g.rnd(i, 1)) + g.rnd(i), 1f)
            val x = g.rnd(i, 2) * w + sin(tau * 2.5f + i) * u * 2f
            val y = h - ph * h
            g.circle(x, y, u * (0.18f + 0.38f * (1f - ph)), if (i % 2 == 0) g.p1 else g.p3, (1f - ph) * 0.85f)
        }
    }

    // =========================================================================================================
    // Mascots, Vortex, Starburst Flash, and 10 Distinct Vector Emblems
    // =========================================================================================================

    fun drawMascot(
        g: Gfx,
        type: MascotType,
        x: Float,
        y: Float,
        size: Float,
        tiltDeg: Float,
        flapPhase: Float,
        motionBlur: Float,
        showLegs: Boolean,
        alpha: Float
    ) {
        if (alpha <= 0.01f || size <= 1f) return
        val c = g.c
        c.save(); c.translate(x, y); c.rotate(tiltDeg)
        when (type) {
            MascotType.BIRD, MascotType.FALCON, MascotType.HUMMINGBIRD, MascotType.PHOENIX, MascotType.OWL ->
                drawSongbird(g, type, size, flapPhase, motionBlur, showLegs, alpha)
            MascotType.BUTTERFLY, MascotType.DRAGONFLY, MascotType.BEE ->
                drawWingedInsect(g, size, flapPhase, motionBlur, showLegs, alpha)
            MascotType.ORIGAMI_PLANE ->
                drawOrigamiPlane(g, size, flapPhase, motionBlur, alpha)
            MascotType.ROCKET ->
                drawMiniRocket(g, size, flapPhase, motionBlur, alpha)
        }
        c.restore()
    }

    private fun drawSongbird(g: Gfx, type: MascotType, r: Float, flapPhase: Float, motionBlur: Float, showLegs: Boolean, alpha: Float) {
        val bodyCol = g.p1
        val wingLight = mixColor(g.p1, g.p2, 0.45f)
        val wingDark = mixColor(g.p1, Color.BLACK, 0.18f)
        val darkLegCol = mixColor(g.p1, Color.BLACK, 0.78f)
        if (showLegs) {
            val legSw = maxOf(1.5f, r * 0.042f)
            g.line(-r * 0.03f, r * 0.14f, -r * 0.08f, r * 0.35f, darkLegCol, alpha * 0.9f, legSw)
            g.line(-r * 0.14f, r * 0.35f, -r * 0.02f, r * 0.35f, darkLegCol, alpha * 0.9f, legSw * 0.85f)
            g.line(r * 0.07f, r * 0.15f, r * 0.03f, r * 0.36f, darkLegCol, alpha, legSw)
            g.line(-r * 0.04f, r * 0.36f, r * 0.09f, r * 0.36f, darkLegCol, alpha, legSw * 0.85f)
        }
        if (motionBlur > 0.05f) {
            for (ghost in 2 downTo 1) {
                drawBirdWing(g, r, sin(flapPhase - ghost * 0.45f), wingLight, alpha * motionBlur * (0.28f / ghost), ghost * r * 0.06f, -ghost * r * 0.04f)
            }
        }
        if (abs(flapPhase) > 0.05f) {
            drawBirdWing(g, r * 0.92f, cos(flapPhase) * 0.85f, wingDark, alpha * 0.85f, r * 0.05f, -r * 0.05f)
        }
        g.path.reset()
        val tailLen = if (type == MascotType.PHOENIX || type == MascotType.FALCON) 0.82f else 0.68f
        g.path.moveTo(r * 0.16f, r * 0.05f); g.path.lineTo(r * tailLen, r * 0.24f); g.path.lineTo(r * (tailLen - 0.06f), r * 0.31f); g.path.lineTo(r * 0.10f, r * 0.16f); g.path.close()
        g.c.drawPath(g.path, g.fillP(wingDark, alpha))
        g.path.reset()
        g.path.moveTo(-r * 0.38f, -r * 0.08f)
        g.path.cubicTo(-r * 0.42f, r * 0.14f, -r * 0.12f, r * 0.26f, r * 0.20f, r * 0.15f)
        g.path.cubicTo(r * 0.26f, r * 0.06f, r * 0.08f, -r * 0.12f, -r * 0.14f, -r * 0.14f)
        g.path.cubicTo(-r * 0.20f, -r * 0.30f, -r * 0.42f, -r * 0.28f, -r * 0.42f, -r * 0.12f)
        g.path.close()
        g.c.drawPath(g.path, g.fillP(bodyCol, alpha))
        g.path.reset()
        val beakLen = if (type == MascotType.HUMMINGBIRD) 0.72f else 0.56f
        g.path.moveTo(-r * 0.40f, -r * 0.18f); g.path.lineTo(-r * beakLen, -r * 0.13f); g.path.lineTo(-r * 0.39f, -r * 0.09f); g.path.close()
        g.c.drawPath(g.path, g.fillP(darkLegCol, alpha))
        g.circle(-r * 0.33f, -r * 0.16f, r * 0.032f, darkLegCol, alpha)
        if (abs(flapPhase) <= 0.05f) {
            g.path.reset()
            g.path.moveTo(-r * 0.12f, -r * 0.08f)
            g.path.cubicTo(r * 0.05f, -r * 0.18f, r * 0.28f, -r * 0.02f, r * 0.36f, r * 0.14f)
            g.path.cubicTo(r * 0.16f, r * 0.16f, -r * 0.08f, r * 0.08f, -r * 0.12f, -r * 0.08f)
            g.path.close()
            g.c.drawPath(g.path, g.fillP(wingDark, alpha * 0.9f))
        } else {
            drawBirdWing(g, r, sin(flapPhase), wingLight, alpha, 0f, 0f)
        }
    }

    private fun drawBirdWing(g: Gfx, r: Float, flap: Float, color: Int, alpha: Float, offsetX: Float, offsetY: Float) {
        val tipY = -r * (0.48f + 0.34f * flap) + offsetY
        val tipX = r * (0.22f + 0.12f * (1f - flap)) + offsetX
        g.path.reset()
        g.path.moveTo(-r * 0.12f + offsetX, -r * 0.08f + offsetY)
        g.path.cubicTo(-r * 0.10f + offsetX, tipY * 0.85f, r * 0.05f + offsetX, tipY, tipX, tipY)
        g.path.cubicTo(r * 0.46f + offsetX, tipY * 0.55f, r * 0.38f + offsetX, r * 0.02f + offsetY, r * 0.08f + offsetX, r * 0.04f + offsetY)
        g.path.close()
        g.c.drawPath(g.path, g.fillP(color, alpha))
    }

    private fun drawWingedInsect(g: Gfx, r: Float, flapPhase: Float, motionBlur: Float, showLegs: Boolean, alpha: Float) {
        val flap = if (abs(flapPhase) < 0.05f) 0.35f else (0.55f + 0.45f * sin(flapPhase))
        val darkCol = mixColor(g.p1, Color.BLACK, 0.72f)
        if (showLegs) {
            g.line(-r * 0.05f, r * 0.12f, -r * 0.08f, r * 0.34f, darkCol, alpha, maxOf(1.5f, r * 0.035f))
            g.line(r * 0.06f, r * 0.12f, r * 0.04f, r * 0.34f, darkCol, alpha, maxOf(1.5f, r * 0.035f))
        }
        if (motionBlur > 0.05f) g.circle(r * 0.08f, -r * 0.28f, r * 0.28f, g.p2, alpha * motionBlur * 0.25f)
        g.c.save(); g.c.scale(1f, flap)
        g.circle(r * 0.05f, -r * 0.28f, r * 0.30f, g.p1, alpha * 0.9f)
        g.circle(r * 0.18f, -r * 0.16f, r * 0.20f, g.p2, alpha * 0.85f)
        g.c.restore()
        g.line(-r * 0.28f, -r * 0.06f, r * 0.28f, r * 0.14f, darkCol, alpha, r * 0.11f)
    }

    private fun drawOrigamiPlane(g: Gfx, r: Float, flapPhase: Float, motionBlur: Float, alpha: Float) {
        val darkWing = mixColor(g.p1, Color.BLACK, 0.22f)
        val lightWing = mixColor(g.p1, g.p2, 0.35f)
        val bob = sin(flapPhase * 0.5f) * r * 0.05f
        if (motionBlur > 0.05f) g.line(r * 0.35f, bob, r * 0.85f, bob + r * 0.1f, g.p2, alpha * motionBlur * 0.45f, r * 0.06f)
        g.path.reset(); g.path.moveTo(-r * 0.55f, -r * 0.05f + bob); g.path.lineTo(r * 0.45f, -r * 0.32f + bob); g.path.lineTo(r * 0.22f, r * 0.06f + bob); g.path.close()
        g.c.drawPath(g.path, g.fillP(lightWing, alpha))
        g.path.reset(); g.path.moveTo(-r * 0.55f, -r * 0.05f + bob); g.path.lineTo(r * 0.22f, r * 0.06f + bob); g.path.lineTo(r * 0.12f, r * 0.24f + bob); g.path.close()
        g.c.drawPath(g.path, g.fillP(darkWing, alpha))
        g.path.reset(); g.path.moveTo(-r * 0.55f, -r * 0.05f + bob); g.path.lineTo(r * 0.42f, r * 0.22f + bob); g.path.lineTo(r * 0.22f, r * 0.06f + bob); g.path.close()
        g.c.drawPath(g.path, g.fillP(g.p1, alpha))
    }

    private fun drawMiniRocket(g: Gfx, r: Float, flapPhase: Float, motionBlur: Float, alpha: Float) {
        if (motionBlur > 0.05f || abs(flapPhase) > 0.05f) {
            val plume = 0.25f + 0.15f * abs(sin(flapPhase * 2f))
            g.circle(r * (0.35f + plume * 0.5f), r * 0.05f, r * plume, g.p2, alpha * 0.7f)
        }
        g.path.reset()
        g.path.moveTo(-r * 0.52f, 0f)
        g.path.cubicTo(-r * 0.25f, -r * 0.26f, r * 0.18f, -r * 0.22f, r * 0.32f, -r * 0.14f)
        g.path.lineTo(r * 0.32f, r * 0.14f)
        g.path.cubicTo(r * 0.18f, r * 0.22f, -r * 0.25f, r * 0.26f, -r * 0.52f, 0f)
        g.path.close()
        g.c.drawPath(g.path, g.fillP(g.p1, alpha))
        g.circle(-r * 0.12f, 0f, r * 0.09f, Color.WHITE, alpha * 0.9f)
    }

    fun drawVortex(g: Gfx, cx: Float, cy: Float, emblemSize: Float, prog: Float) {
        if (prog <= 0.001f || prog >= 0.999f) return
        val u = g.u
        val intensity = sin(prog * 3.1415927f)
        val coreR = emblemSize * (0.48f + 0.24f * sin(prog * 3.1415927f))
        g.glow(cx, cy, coreR * 2.1f, g.p2, 0.55f * intensity)
        g.glow(cx, cy, coreR * 1.35f, g.p1, 0.75f * intensity)
        val spinDeg = prog * 720f
        g.c.save(); g.c.translate(cx, cy); g.c.rotate(-spinDeg)
        for (k in 0 until 16) {
            val r = coreR * (0.28f + 0.72f * (k / 16f))
            val startAng = k * 47f + prog * 180f
            val sweepAng = 110f + (k % 3) * 35f
            val col = when { k % 3 == 0 -> Color.WHITE; k % 3 == 1 -> g.p2; else -> g.p1 }
            val sw = u * (0.35f + 0.45f * (1f - k / 16f)) * intensity
            g.rect.set(-r, -r, r, r)
            g.c.drawArc(g.rect, startAng, sweepAng, false, g.strokeP(col, (0.45f + 0.45f * (k % 2)) * intensity, sw))
        }
        g.c.restore()
        val splashExpand = if (prog < 0.62f) easeOut(prog / 0.62f) else 1f - 0.55f * easeInOut((prog - 0.62f) / 0.38f)
        for (i in 0 until 210) {
            val baseAng = g.rnd(i, 11) * 6.283185f + (1f - prog) * (1.2f * (g.rnd(i, 12) - 0.5f))
            val minD = coreR * 0.35f
            val maxD = emblemSize * (0.55f + 1.45f * g.rnd(i, 13) * g.rnd(i, 14))
            val d = lerp(minD, maxD, splashExpand)
            val px = cx + cos(baseAng) * d; val py = cy + sin(baseAng) * d
            val pr = u * (0.22f + 0.78f * g.rnd(i, 15)) * intensity
            val pCol = if (i % 3 == 0) g.p1 else g.p2
            val pAlpha = (0.25f + 0.65f * g.rnd(i, 16)) * intensity
            g.circle(px, py, pr * 1.45f, g.p2, pAlpha * 0.35f)
            g.circle(px, py, pr, pCol, pAlpha)
        }
    }

    fun drawStarburstFlash(g: Gfx, cx: Float, cy: Float, emblemSize: Float, flashProg: Float) {
        if (flashProg <= 0.001f || flashProg >= 0.999f) return
        val intensity = sin(flashProg * 3.1415927f)
        if (intensity <= 0.01f) return
        g.glow(cx, cy, emblemSize * 1.65f * intensity, g.p2, 0.55f * intensity)
        g.glow(cx, cy, emblemSize * 0.85f * intensity, Color.WHITE, 0.85f * intensity)
        for (k in 0 until 8) {
            val isMain = k % 2 == 0
            val ang = Math.toRadians((k * 45).toDouble()).toFloat()
            val len = emblemSize * (if (isMain) (if (k == 0 || k == 4) 1.85f else 1.45f) else 0.72f) * intensity
            val halfW = emblemSize * (if (isMain) 0.09f else 0.045f) * intensity
            val nx = -sin(ang) * halfW; val ny = cos(ang) * halfW
            g.path.reset()
            g.path.moveTo(cx + nx * 1.6f, cy + ny * 1.6f); g.path.lineTo(cx + cos(ang) * len * 1.15f, cy + sin(ang) * len * 1.15f); g.path.lineTo(cx - nx * 1.6f, cy - ny * 1.6f); g.path.close()
            g.c.drawPath(g.path, g.fillP(g.p2, 0.45f * intensity))
            g.path.reset()
            g.path.moveTo(cx + nx, cy + ny); g.path.lineTo(cx + cos(ang) * len, cy + sin(ang) * len); g.path.lineTo(cx - nx, cy - ny); g.path.close()
            g.c.drawPath(g.path, g.fillP(Color.WHITE, 0.92f * intensity))
        }
        g.circle(cx, cy, emblemSize * 0.14f * intensity, Color.WHITE, 0.98f * intensity)
    }

    fun drawVectorEmblem(g: Gfx, type: EmblemType, size: Float, primaryColor: Int, secondaryColor: Int, cutoutColor: Int, alpha: Float) {
        if (alpha <= 0.005f || size <= 2f) return
        when (type) {
            EmblemType.UNIVERSITY_CREST -> drawUniversityCrest(g, size, primaryColor, cutoutColor, alpha)
            EmblemType.CROWN_SHIELD -> drawCrownShield(g, size, primaryColor, secondaryColor, cutoutColor, alpha)
            EmblemType.HEX_CUBE -> drawIsoCube(g, 0f, 0f, size * 0.46f, primaryColor, secondaryColor, alpha)
            EmblemType.WINGED_SHIELD -> drawWingedShield(g, size, primaryColor, secondaryColor, cutoutColor, alpha)
            EmblemType.DIAMOND_CREST -> drawDiamondStarEmblem(g, size, primaryColor, secondaryColor, cutoutColor, alpha)
            EmblemType.FLAME_SHIELD -> drawFlameCrest(g, size, primaryColor, secondaryColor, cutoutColor, alpha)
            EmblemType.TORCH_SHIELD -> drawCompassCrest(g, size, primaryColor, secondaryColor, cutoutColor, alpha)
            EmblemType.HONEYCOMB_CREST -> drawHoneycombEmblem(g, size, primaryColor, secondaryColor, cutoutColor, alpha)
            EmblemType.LOTUS_CREST -> drawLotusSeal(g, size, primaryColor, secondaryColor, cutoutColor, alpha)
            EmblemType.ORBIT_SHIELD -> drawQuantumOrbitEmblem(g, size, primaryColor, secondaryColor, cutoutColor, alpha)
        }
    }

    private fun drawUniversityCrest(g: Gfx, s: Float, primaryColor: Int, cutoutColor: Int, alpha: Float) {
        val w2 = s * 0.43f; val topY = -s * 0.48f; val botY = s * 0.48f
        val notchY = -s * 0.36f; val curveStartY = s * 0.04f
        g.path.reset()
        g.path.moveTo(-w2, topY); g.path.lineTo(-w2 * 0.46f, topY)
        g.path.lineTo(-w2 * 0.46f, notchY); g.path.lineTo(-w2 * 0.20f, notchY)
        g.path.lineTo(-w2 * 0.20f, topY); g.path.lineTo(w2 * 0.20f, topY)
        g.path.lineTo(w2 * 0.20f, notchY); g.path.lineTo(w2 * 0.46f, notchY)
        g.path.lineTo(w2 * 0.46f, topY); g.path.lineTo(w2, topY)
        g.path.lineTo(w2, curveStartY)
        g.path.cubicTo(w2, s * 0.32f, w2 * 0.52f, botY, 0f, botY)
        g.path.cubicTo(-w2 * 0.52f, botY, -w2, s * 0.32f, -w2, curveStartY)
        g.path.close()
        g.c.drawPath(g.path, g.fillP(primaryColor, alpha))
        val starCy = s * 0.05f; val innerW = w2 * 0.66f; val barTopY = -s * 0.22f; val ribSw = s * 0.078f
        g.path.reset()
        g.path.moveTo(-innerW, barTopY); g.path.lineTo(innerW, barTopY); g.path.lineTo(innerW, curveStartY)
        g.path.cubicTo(innerW, s * 0.24f, innerW * 0.48f, s * 0.35f, 0f, s * 0.35f)
        g.path.cubicTo(-innerW * 0.48f, s * 0.35f, -innerW, s * 0.24f, -innerW, curveStartY)
        g.path.close()
        g.c.drawPath(g.path, g.fillP(cutoutColor, alpha))
        g.rectFill(-ribSw * 0.5f, topY, ribSw * 0.5f, botY * 0.92f, primaryColor, alpha)
        g.rectFill(-w2 * 0.92f, starCy - ribSw * 0.48f, w2 * 0.92f, starCy + ribSw * 0.48f, primaryColor, alpha)
        g.ring(0f, starCy, s * 0.23f, primaryColor, alpha, ribSw * 0.95f)
        val bossR = s * 0.21f
        g.path.reset()
        g.path.moveTo(0f, starCy - bossR); g.path.lineTo(bossR, starCy); g.path.lineTo(0f, starCy + bossR); g.path.lineTo(-bossR, starCy); g.path.close()
        g.c.drawPath(g.path, g.fillP(primaryColor, alpha))
        val starR = s * 0.145f; val waist = starR * 0.28f
        g.path.reset()
        g.path.moveTo(0f, starCy - starR)
        g.path.quadTo(waist, starCy - waist, starR, starCy)
        g.path.quadTo(waist, starCy + waist, 0f, starCy + starR)
        g.path.quadTo(-waist, starCy + waist, -starR, starCy)
        g.path.quadTo(-waist, starCy - waist, 0f, starCy - starR)
        g.path.close()
        g.c.drawPath(g.path, g.fillP(cutoutColor, alpha))
    }

    private fun drawCrownShield(g: Gfx, s: Float, c1: Int, c2: Int, cutout: Int, alpha: Float) {
        drawUniversityCrest(g, s * 0.92f, c1, cutout, alpha)
        g.circle(0f, -s * 0.48f, s * 0.045f, c2, alpha)
        g.circle(-s * 0.30f, -s * 0.48f, s * 0.04f, c2, alpha)
        g.circle(s * 0.30f, -s * 0.48f, s * 0.04f, c2, alpha)
    }

    private fun drawWingedShield(g: Gfx, s: Float, c1: Int, c2: Int, cutout: Int, alpha: Float) {
        for (dir in intArrayOf(-1, 1)) {
            for (row in 0..2) {
                val y = -s * 0.22f + row * s * 0.12f
                val wSpan = s * (0.48f - row * 0.08f) * dir
                g.line(0f, y, wSpan, y - s * 0.10f, c2, alpha * 0.9f, s * 0.065f)
            }
        }
        drawUniversityCrest(g, s * 0.78f, c1, cutout, alpha)
    }

    private fun drawDiamondStarEmblem(g: Gfx, s: Float, c1: Int, c2: Int, cutout: Int, alpha: Float) {
        val r = s * 0.46f
        g.path.reset(); g.path.moveTo(0f, -r); g.path.lineTo(r, 0f); g.path.lineTo(0f, r); g.path.lineTo(-r, 0f); g.path.close()
        g.c.drawPath(g.path, g.fillP(c1, alpha))
        g.ring(0f, 0f, r * 0.62f, cutout, alpha, s * 0.055f)
        val sr = r * 0.42f; val w = sr * 0.28f
        g.path.reset()
        g.path.moveTo(0f, -sr); g.path.quadTo(w, -w, sr, 0f); g.path.quadTo(w, w, 0f, sr); g.path.quadTo(-w, w, -sr, 0f); g.path.quadTo(-w, -w, 0f, -sr); g.path.close()
        g.c.drawPath(g.path, g.fillP(c2, alpha))
    }

    private fun drawFlameCrest(g: Gfx, s: Float, c1: Int, c2: Int, cutout: Int, alpha: Float) {
        g.circle(0f, s * 0.06f, s * 0.42f, c1, alpha)
        g.path.reset()
        g.path.moveTo(0f, -s * 0.48f)
        g.path.cubicTo(s * 0.38f, -s * 0.12f, s * 0.34f, s * 0.36f, 0f, s * 0.42f)
        g.path.cubicTo(-s * 0.34f, s * 0.36f, -s * 0.38f, -s * 0.12f, 0f, -s * 0.48f)
        g.path.close()
        g.c.drawPath(g.path, g.fillP(c2, alpha * 0.9f))
        g.circle(0f, s * 0.12f, s * 0.15f, cutout, alpha)
    }

    private fun drawCompassCrest(g: Gfx, s: Float, c1: Int, c2: Int, cutout: Int, alpha: Float) {
        val r = s * 0.44f
        g.ring(0f, 0f, r, c1, alpha, s * 0.075f)
        g.ring(0f, 0f, r * 0.72f, c2, alpha, s * 0.035f)
        val sr = r * 0.85f; val w = sr * 0.22f
        g.path.reset()
        g.path.moveTo(0f, -sr); g.path.lineTo(w, -w); g.path.lineTo(sr, 0f); g.path.lineTo(w, w)
        g.path.lineTo(0f, sr); g.path.lineTo(-w, w); g.path.lineTo(-sr, 0f); g.path.lineTo(-w, -w); g.path.close()
        g.c.drawPath(g.path, g.fillP(c1, alpha))
        g.circle(0f, 0f, s * 0.09f, cutout, alpha)
    }

    private fun drawHoneycombEmblem(g: Gfx, s: Float, c1: Int, c2: Int, cutout: Int, alpha: Float) {
        val cellR = s * 0.17f
        val offsets = arrayOf(
            0f to 0f, 0f to -cellR * 1.8f, 0f to cellR * 1.8f,
            -cellR * 1.56f to -cellR * 0.9f, cellR * 1.56f to -cellR * 0.9f,
            -cellR * 1.56f to cellR * 0.9f, cellR * 1.56f to cellR * 0.9f
        )
        for ((idx, off) in offsets.withIndex()) {
            val (ox, oy) = off
            g.path.reset()
            for (k in 0..5) {
                val a = Math.toRadians((k * 60).toDouble()).toFloat()
                val px = ox + cos(a) * cellR * 0.92f; val py = oy + sin(a) * cellR * 0.92f
                if (k == 0) g.path.moveTo(px, py) else g.path.lineTo(px, py)
            }
            g.path.close()
            g.c.drawPath(g.path, g.fillP(if (idx == 0) c2 else c1, alpha))
        }
    }

    private fun drawLotusSeal(g: Gfx, s: Float, c1: Int, c2: Int, cutout: Int, alpha: Float) {
        g.circle(0f, 0f, s * 0.45f, c1, alpha)
        g.ring(0f, 0f, s * 0.39f, cutout, alpha, s * 0.025f)
        for (k in -2..2) {
            g.c.save(); g.c.rotate(k * 24f, 0f, s * 0.18f)
            g.rect.set(-s * 0.09f, -s * 0.26f, s * 0.09f, s * 0.18f)
            g.c.drawOval(g.rect, g.fillP(if (k == 0) cutout else c2, alpha * 0.92f))
            g.c.restore()
        }
    }

    private fun drawQuantumOrbitEmblem(g: Gfx, s: Float, c1: Int, c2: Int, cutout: Int, alpha: Float) {
        g.circle(0f, 0f, s * 0.16f, c1, alpha)
        for (k in 0..2) {
            g.c.save(); g.c.rotate(k * 60f)
            g.rect.set(-s * 0.46f, -s * 0.18f, s * 0.46f, s * 0.18f)
            g.c.drawOval(g.rect, g.strokeP(if (k == 0) c2 else c1, alpha * 0.9f, s * 0.045f))
            g.c.restore()
        }
    }
}
