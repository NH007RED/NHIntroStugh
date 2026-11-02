package com.nh.introstudio.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.nh.introstudio.domain.BgMode
import com.nh.introstudio.domain.BrandConfig
import com.nh.introstudio.domain.FONT_OPTIONS
import com.nh.introstudio.domain.InOutStyle
import com.nh.introstudio.domain.LayoutMode
import com.nh.introstudio.domain.LogoStyle
import com.nh.introstudio.domain.TemplateSpec
import com.nh.introstudio.domain.TextStyle
import com.nh.introstudio.templates.TemplateCatalog
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Broadcast-grade frame renderer for NH Intro Studio v4:
 * 1. Deep Unified IN + IDLE + OUT Camera & Compositing Pipeline (`InOutStyle`):
 *    - Camera Dolly & 3D Parallax push-in across the entire scene
 *    - Sub-frame velocity motion-blur trails during IN and OUT transitions
 *    - Unified Specular Light Sweep + Foreground Volumetric Light-Wrap so logo, text, and background
 *      look fused into a single broadcast render
 *    - True physical OUT transitions (Hyper-Zoom Fly-Through, Particle Vortex Dissolve,
 *      Anamorphic Prism Warp, Liquid Vacuum Implosion, and Laser Light-Wipe Vaporize)
 * 2. 20 Distinct AI Logo Animation Choreographies (Templates 1..20)
 * 3. 20 Streaming & Cinema Hero Text Intro Engines (Templates 21..40: Netflix, Prime, Hotstar,
 *    Marvel, Apple TV+, HBO, IMAX, Dolby, Universal, Columbia, JioCinema, Zee5, A24, Hulu,
 *    Paramount, Warner, Peacock, YouTube Originals, Cyberpunk, and Molten Titanium).
 */
class IntroRenderer {
    private val g = Gfx()
    val media = MediaCache()
    private val tpBold = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tpReg = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bp = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val dst = RectF()
    private val boldFaces = HashMap<Int, Typeface>()
    private val regFaces = HashMap<Int, Typeface>()

    private fun boldTypeface(index: Int): Typeface = boldFaces.getOrPut(index) {
        val f = FONT_OPTIONS[index.coerceIn(0, FONT_OPTIONS.lastIndex)]
        Typeface.create(f.family, Typeface.BOLD)
    }

    private fun regTypeface(index: Int): Typeface = regFaces.getOrPut(index) {
        val f = FONT_OPTIONS[index.coerceIn(0, FONT_OPTIONS.lastIndex)]
        Typeface.create(f.family, Typeface.NORMAL)
    }

    fun render(canvas: Canvas, w: Int, h: Int, t: Float, cfg: BrandConfig, isPreview: Boolean = true) {
        val spec = TemplateCatalog.byId(cfg.templateId)
        val fw = w.toFloat(); val fh = h.toFloat()
        val tau = t * cfg.speed
        val p1 = if (cfg.useCustomColors) cfg.colorPrimary else spec.c1
        val p2 = if (cfg.useCustomColors) cfg.colorSecondary else spec.c2
        val textColor = if (cfg.useCustomColors) cfg.colorText else spec.textColor
        val inOutStyle = cfg.inOutStyleOverride ?: spec.inOutStyle

        // 1. Background Mode
        val bgCutoutColor: Int = when (cfg.bgMode) {
            BgMode.TRANSPARENT -> {
                if (isPreview) drawCheckerboard(canvas, fw, fh)
                else canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                Color.WHITE
            }
            BgMode.CHROMA_GREEN -> {
                bp.reset(); bp.color = 0xFF00FF00.toInt()
                canvas.drawRect(0f, 0f, fw, fh, bp)
                0xFF00FF00.toInt()
            }
            BgMode.SOLID -> {
                val c = cfg.bgSolidColor or 0xFF000000.toInt()
                bp.reset(); bp.color = c
                canvas.drawRect(0f, 0f, fw, fh, bp)
                c
            }
            BgMode.AI_AUTO -> {
                bp.reset(); bp.isAntiAlias = true
                bp.shader = LinearGradient(0f, 0f, 0f, fh, spec.bg1, spec.bg2, Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, fw, fh, bp)
                drawBackgroundMedia(canvas, cfg, fw, fh, t, w, h)
                spec.bg1
            }
        }

        g.begin(canvas, fw, fh, tau, p1, p2, spec.c3)

        // 2. Unified Camera Dolly & Deep IN / IDLE / OUT Lifecycle Progress
        val inProg = easeOut(clamp01(t / 0.85f))
        val outroDur = min(1.25f, cfg.durationSec * 0.22f)
        val outroProg = clamp01((t - (cfg.durationSec - outroDur)) / outroDur)

        // Calculate global camera dolly + outro physical transformation
        var camScaleX = 0.96f + 0.04f * inProg + 0.03f * (t / cfg.durationSec.coerceAtLeast(1f))
        var camScaleY = camScaleX
        var camAlpha = 1f
        if (outroProg > 0f) {
            val eo = easeInOut(outroProg)
            when (inOutStyle) {
                InOutStyle.HYPER_ZOOM_THROUGH -> {
                    val z = 1f + 3.6f * outroProg * outroProg * outroProg
                    camScaleX *= z; camScaleY *= z
                    camAlpha = 1f - eo * eo
                }
                InOutStyle.PARTICLE_VORTEX_DISSOLVE -> {
                    camScaleX *= (1f + 0.12f * eo)
                    camScaleY *= (1f + 0.12f * eo)
                    camAlpha = 1f - clamp01((outroProg - 0.15f) / 0.85f)
                }
                InOutStyle.PRISM_CHROMATIC_WARP -> {
                    camScaleX *= (1f + 2.4f * eo * eo)
                    camScaleY *= (1f - 0.92f * eo).coerceAtLeast(0.03f)
                    camAlpha = 1f - eo * eo
                }
                InOutStyle.LIQUID_ELASTIC_MORPH -> {
                    val swell = if (outroProg < 0.28f) 1f + 0.14f * sin(outroProg / 0.28f * 3.14159f)
                    else (1f - easeInOut((outroProg - 0.28f) / 0.72f)).coerceAtLeast(0.01f)
                    camScaleX *= swell; camScaleY *= swell
                    camAlpha = if (outroProg < 0.35f) 1f else 1f - ((outroProg - 0.35f) / 0.65f)
                }
                InOutStyle.LASER_LIGHT_SWEEP -> {
                    camAlpha = 1f - clamp01((outroProg - 0.65f) / 0.35f)
                }
            }
        }

        val isTextHero = cfg.layoutMode == LayoutMode.TEXT_HERO || (cfg.layoutMode == LayoutMode.AUTO && spec.isTextIntro && !cfg.showLogo)
        val isHorizontal = !isTextHero && when (cfg.layoutMode) {
            LayoutMode.HORIZONTAL -> true
            LayoutMode.CENTERED -> false
            else -> spec.defaultHorizontal
        }

        val name = cfg.brandName.trim().ifEmpty { if (spec.isTextIntro) "NETSTREAM" else "Logoipsum" }
        val tag = cfg.tagline.trim()
        val sub = cfg.subtitle.trim()
        val tfBold = boldTypeface(cfg.fontIndex)
        val tfReg = regTypeface(cfg.fontIndex)

        // 3. Compute Lockup Coordinates
        var bs = g.u * (when {
            isTextHero -> 13.5f
            isHorizontal -> 8.4f
            else -> 8.8f
        }) * cfg.fontScale
        tpBold.reset(); tpBold.isAntiAlias = true; tpBold.typeface = tfBold; tpBold.textSize = bs
        tpBold.letterSpacing = if (isTextHero) 0.06f else -0.02f

        var tagSize = bs * (when {
            isTextHero -> 0.34f
            isHorizontal -> 0.78f
            else -> 0.44f
        })
        tpReg.reset(); tpReg.isAntiAlias = true; tpReg.typeface = tfReg; tpReg.textSize = tagSize
        tpReg.letterSpacing = if (isHorizontal) -0.01f else 0.22f

        var emblemSize = if (isHorizontal) bs * 2.55f * cfg.logoScale else min(fw, fh) * 0.28f * cfg.logoScale
        val showLogoNow = cfg.showLogo && !isTextHero
        var emblemW = if (showLogoNow) emblemSize * 0.88f else 0f
        var gap = if (showLogoNow) emblemSize * 0.24f else 0f

        val rawW1 = tpBold.measureText(name)
        val rawW2 = if (tag.isNotEmpty()) tpReg.measureText(tag) else 0f
        val rawTextW = max(rawW1, rawW2)

        if (isHorizontal) {
            val rawTotalW = emblemW + gap + rawTextW
            val maxAllowedW = fw * 0.86f
            if (rawTotalW > maxAllowedW && rawTotalW > 1f) {
                val scale = maxAllowedW / rawTotalW
                bs *= scale; tagSize *= scale; emblemSize *= scale; emblemW *= scale; gap *= scale
                tpBold.textSize = bs; tpReg.textSize = tagSize
            }
        } else {
            val maxAllowedW = fw * 0.86f
            if (rawTextW > maxAllowedW && rawTextW > 1f) {
                val scale = maxAllowedW / rawTextW
                bs *= scale; tagSize *= scale
                tpBold.textSize = bs; tpReg.textSize = tagSize
            }
        }

        val w1 = tpBold.measureText(name)
        val w2 = if (tag.isNotEmpty()) tpReg.measureText(tag) else 0f
        val textBlockW = max(w1, w2)

        val logoCx: Float
        val logoCy: Float
        val textLeft: Float
        val textCenterX: Float
        val by1: Float
        val by2: Float

        when {
            isTextHero -> {
                textCenterX = cfg.textX * fw
                textLeft = textCenterX - w1 * 0.5f
                by1 = cfg.textY * fh + bs * 0.18f
                by2 = by1 + bs * 0.62f
                logoCx = textCenterX
                logoCy = by1 - bs * 1.15f
            }
            isHorizontal -> {
                val totalLockupW = emblemW + gap + textBlockW
                val lockupLeft = cfg.textX * fw - totalLockupW * 0.5f
                logoCx = lockupLeft + emblemW * 0.5f
                textLeft = lockupLeft + emblemW + gap
                textCenterX = textLeft + textBlockW * 0.5f
                val centerY = cfg.textY * fh
                by1 = if (tag.isNotEmpty()) centerY - bs * 0.10f else centerY + bs * 0.32f
                by2 = by1 + bs * 0.90f
                logoCy = centerY - bs * 0.02f
            }
            else -> {
                logoCx = cfg.logoX * fw
                logoCy = fh * 0.36f
                textCenterX = cfg.textX * fw
                textLeft = textCenterX - w1 * 0.5f
                by1 = fh * 0.66f
                by2 = by1 + bs * 0.72f
            }
        }

        // 4. Apply Unified Camera Dolly & Outro Transform
        canvas.save()
        canvas.scale(camScaleX, camScaleY, fw * 0.5f, fh * 0.5f)
        if (inOutStyle == InOutStyle.LASER_LIGHT_SWEEP && outroProg > 0.005f) {
            val clipRight = lerp(fw * 0.88f, fw * 0.12f, easeInOut(outroProg))
            canvas.clipRect(0f, 0f, clipRight, fh)
        }

        // 5. Render Stage & Background Effects (Templates 1..40)
        Effects.drawStageEffect(
            id = spec.id,
            g = g,
            isLightPaper = spec.isLightPaper,
            logoCx = logoCx,
            logoCy = logoCy,
            emblemSize = emblemSize,
            textLeft = textLeft,
            textWidth = w1,
            by1 = by1,
            bs = bs
        )

        val effectiveLogoStyle = cfg.logoStyleOverride ?: spec.logoStyle
        val effectiveTextStyle = cfg.textStyleOverride ?: spec.textStyle

        // 6. Render Typography (11 Logo Lockup Styles + 20 Streaming Hero Text Intro Styles)
        if (camAlpha > 0.01f) {
            drawTypography(
                canvas = canvas,
                cfg = cfg,
                spec = spec,
                textStyle = effectiveTextStyle,
                isHorizontal = isHorizontal,
                isTextHero = isTextHero,
                name = name,
                tag = tag,
                sub = sub,
                textLeft = textLeft,
                textCenterX = textCenterX,
                by1 = by1,
                by2 = by2,
                bs = bs,
                tagSize = tagSize,
                tfBold = tfBold,
                tfReg = tfReg,
                textColor = withAlpha(textColor, Color.alpha(textColor) / 255f * camAlpha),
                p1 = p1,
                p2 = p2,
                tau = tau,
                outroProg = outroProg
            )
        }

        // 7. Render Deep Unified Logo / Emblem Reveal (with Sub-Frame Motion Blur & Specular Sheen)
        if (showLogoNow && camAlpha > 0.01f) {
            val firstCharCapTopY = by1 - bs * 0.74f
            val firstCharPerchX = textLeft + bs * 0.14f
            drawLogoReveal(
                canvas = canvas,
                cfg = cfg,
                spec = spec,
                style = effectiveLogoStyle,
                tau = tau,
                p1 = p1,
                p2 = p2,
                bgCutoutColor = bgCutoutColor,
                perchX = firstCharPerchX,
                perchY = firstCharCapTopY,
                logoCx = logoCx,
                logoCy = logoCy,
                emblemSize = emblemSize,
                camAlpha = camAlpha,
                outroProg = outroProg,
                inOutStyle = inOutStyle
            )
        }

        canvas.restore()

        // 8. Unified Foreground Light-Wrap, Specular Sheen & Outro Particle/Warp Pass
        Effects.drawUnifiedForegroundLightWrap(
            g = g,
            isLightPaper = spec.isLightPaper,
            logoCx = logoCx,
            logoCy = logoCy,
            emblemSize = emblemSize,
            textCenterX = textCenterX,
            by1 = by1,
            bs = bs,
            inProg = inProg,
            outroProg = outroProg,
            inOutStyle = inOutStyle
        )

        // 9. Vignette & Optional Watermark
        drawOverlay(canvas, cfg, spec, fw, fh, t, cfg.durationSec)
    }

    fun release() {
        media.release()
    }

    private fun drawTypography(
        canvas: Canvas,
        cfg: BrandConfig,
        spec: TemplateSpec,
        textStyle: TextStyle,
        isHorizontal: Boolean,
        isTextHero: Boolean,
        name: String,
        tag: String,
        sub: String,
        textLeft: Float,
        textCenterX: Float,
        by1: Float,
        by2: Float,
        bs: Float,
        tagSize: Float,
        tfBold: Typeface,
        tfReg: Typeface,
        textColor: Int,
        p1: Int,
        p2: Int,
        tau: Float,
        outroProg: Float
    ) {
        val textStart = if (textStyle == TextStyle.CASCADE_STAIRCASE || textStyle == TextStyle.BOUNCE_WAVE || spec.isTextIntro) 0f else (spec.revealAt * 0.45f)
        val pr = clamp01((tau - textStart) / 0.95f)

        tpBold.reset(); tpBold.isAntiAlias = true
        tpBold.typeface = tfBold; tpBold.textSize = bs
        tpBold.letterSpacing = if (isTextHero) 0.06f else -0.02f

        if (name.isNotEmpty() && pr > 0f) {
            when (textStyle) {
                // =========================================================================================
                // 20 STREAMING & CINEMA HERO TEXT INTRO ENGINES (Netflix, Prime, Hotstar, Marvel, etc.)
                // =========================================================================================
                TextStyle.STREAM_NETFLIX_PRISM -> {
                    // Phase A (0..1.1s): Giant 3D Crimson initial ribbon, Phase B (0.9..2.6s): Full curved red wordmark, Phase C (2.6s+): Zooms into prism bars!
                    val prismRush = clamp01((tau - 2.5f) / 1.4f)
                    if (prismRush < 0.95f) {
                        val e = easeOutBack(pr)
                        val zoom = 1f + 3.8f * prismRush * prismRush
                        val fade = 1f - prismRush
                        tpBold.textAlign = Paint.Align.CENTER
                        canvas.save()
                        canvas.scale(zoom * (0.82f + 0.18f * e), zoom * (0.82f + 0.18f * e), textCenterX, by1 - bs * 0.35f)
                        // 8-layer 3D dark-crimson extrusion shadow
                        for (layer in 8 downTo 1) {
                            tpBold.color = withAlpha(mixColor(p1, Color.BLACK, 0.65f), 0.35f * e * fade)
                            canvas.drawText(name, textCenterX, by1 + layer * bs * 0.025f, tpBold)
                        }
                        tpBold.color = withAlpha(textColor, e * fade)
                        canvas.drawText(name, textCenterX, by1, tpBold)
                        canvas.restore()
                    }
                }
                TextStyle.STREAM_PRIME_SMILE -> {
                    // Each character pops in with clean kinetic scale & bounce as the smile arrow sweeps underneath
                    tpBold.textAlign = Paint.Align.LEFT
                    var curX = textLeft
                    val n = name.length
                    for (i in 0 until n) {
                        val ch = name[i].toString()
                        val charW = tpBold.measureText(ch)
                        val cp = clamp01((tau - i * 0.06f) / 0.45f)
                        if (cp > 0f) {
                            val e = easeOutBack(cp)
                            canvas.save()
                            canvas.scale(e, e, curX + charW * 0.5f, by1 - bs * 0.35f)
                            tpBold.color = withAlpha(textColor, clamp01(cp * 2.5f))
                            canvas.drawText(ch, curX, by1, tpBold)
                            canvas.restore()
                        }
                        curX += charW
                    }
                }
                TextStyle.STREAM_HOTSTAR_ARC -> {
                    // Metallic gold/cyan shimmer across the letters as the shooting star arcs overhead
                    val e = easeOut(pr)
                    tpBold.textAlign = Paint.Align.CENTER
                    val tw = tpBold.measureText(name)
                    val sweep = clamp01((tau - 0.25f) / 1.45f)
                    val hx = textLeft + tw * sweep
                    tpBold.shader = LinearGradient(
                        hx - tw * 0.25f, 0f, hx + tw * 0.25f, 0f,
                        intArrayOf(textColor, p2, Color.WHITE, p1, textColor),
                        floatArrayOf(0f, 0.35f, 0.5f, 0.65f, 1f), Shader.TileMode.CLAMP
                    )
                    tpBold.alpha = (e * 255f).toInt().coerceIn(0, 255)
                    canvas.save()
                    canvas.scale(0.90f + 0.10f * e, 0.90f + 0.10f * e, textCenterX, by1 - bs * 0.35f)
                    canvas.drawText(name, textCenterX, by1, tpBold)
                    canvas.restore()
                    tpBold.shader = null
                }
                TextStyle.STREAM_MARVEL_FLIP -> {
                    // Pulls back from 2.4x scale into the locked crimson monolith block
                    val pull = easeOut(clamp01(tau / 1.25f))
                    val sc = lerp(2.35f, 0.92f, pull)
                    tpBold.textAlign = Paint.Align.CENTER
                    tpBold.letterSpacing = -0.03f
                    canvas.save()
                    canvas.scale(sc, sc, textCenterX, g.cy)
                    tpBold.color = withAlpha(textColor, clamp01(tau * 3f))
                    canvas.drawText(name.uppercase(), textCenterX, g.cy + bs * 0.32f, tpBold)
                    canvas.restore()
                }
                TextStyle.STREAM_APPLE_GLASS -> {
                    // Multi-pass optical focus pull from soft glass refraction into crisp focus
                    val focus = easeInOut(clamp01(tau / 1.35f))
                    tpBold.textAlign = Paint.Align.CENTER
                    for (k in 5 downTo 0) {
                        val spread = (1f - focus) * k * bs * 0.08f
                        val a = if (k == 0) focus else (1f - focus) * 0.18f
                        tpBold.color = withAlpha(if (k % 2 == 0) textColor else p1, a)
                        canvas.drawText(name, textCenterX + spread, by1, tpBold)
                        if (k > 0) canvas.drawText(name, textCenterX - spread, by1, tpBold)
                    }
                }
                TextStyle.STREAM_HBO_CHROME, TextStyle.STREAM_GOLD_MONOLITH, TextStyle.STREAM_TITANIUM_FORGE -> {
                    // 12-layer 3D metallic extrusion whose shadow angle responds to the sweeping overhead light!
                    val e = easeOut(pr)
                    tpBold.textAlign = Paint.Align.CENTER
                    val lightShiftX = -sin(tau * 1.1f) * bs * 0.14f
                    val lightShiftY = bs * 0.14f
                    for (layer in 12 downTo 1) {
                        val frac = layer / 12f
                        val extCol = mixColor(p1, Color.BLACK, 0.35f + 0.45f * frac)
                        tpBold.color = withAlpha(extCol, 0.55f * e)
                        canvas.drawText(name, textCenterX + lightShiftX * frac, by1 + lightShiftY * frac, tpBold)
                    }
                    val tw = tpBold.measureText(name)
                    val hx = textCenterX + sin(tau * 1.1f) * tw * 0.45f
                    tpBold.shader = LinearGradient(
                        hx - tw * 0.25f, by1 - bs, hx + tw * 0.25f, by1,
                        intArrayOf(textColor, Color.WHITE, p2, textColor),
                        floatArrayOf(0f, 0.45f, 0.6f, 1f), Shader.TileMode.CLAMP
                    )
                    tpBold.alpha = (e * 255f).toInt().coerceIn(0, 255)
                    canvas.drawText(name, textCenterX, by1, tpBold)
                    tpBold.shader = null
                }
                TextStyle.STREAM_IMAX_LASER, TextStyle.STREAM_ORBIT_TEXT, TextStyle.STREAM_BEAM_PILLAR, TextStyle.STREAM_STAR_RING -> {
                    // Wide cinematic letter-spacing convergence + volumetric downward shadow rays
                    val e = easeOut(pr)
                    tpBold.textAlign = Paint.Align.CENTER
                    tpBold.letterSpacing = (1f - e) * 0.55f + 0.10f
                    for (k in 6 downTo 1) {
                        val s = 1f + k * 0.025f
                        canvas.save()
                        canvas.scale(s, s, textCenterX, by1 - bs * 0.4f)
                        tpBold.color = withAlpha(p1, 0.12f * e)
                        canvas.drawText(name, textCenterX, by1, tpBold)
                        canvas.restore()
                    }
                    tpBold.color = withAlpha(textColor, e)
                    canvas.drawText(name, textCenterX, by1, tpBold)
                }
                TextStyle.STREAM_DOLBY_SPHERE, TextStyle.STREAM_GREEN_SONIC, TextStyle.STREAM_COLOR_BLOOM, TextStyle.STREAM_RED_PLAY -> {
                    val e = easeOutBack(pr)
                    tpBold.textAlign = Paint.Align.CENTER
                    canvas.save()
                    canvas.scale(e, e, textCenterX, by1 - bs * 0.35f)
                    tpBold.color = withAlpha(textColor, clamp01(pr * 2.5f))
                    canvas.drawText(name, textCenterX, by1, tpBold)
                    canvas.restore()
                }
                TextStyle.STREAM_STADIUM_WAVE, TextStyle.STREAM_AURORA_SILK -> {
                    // Sinusoidal wave entrance across characters
                    tpBold.textAlign = Paint.Align.LEFT
                    var curX = textLeft
                    val n = name.length
                    for (i in 0 until n) {
                        val ch = name[i].toString()
                        val charW = tpBold.measureText(ch)
                        val cp = clamp01((tau - i * 0.065f) / 0.55f)
                        if (cp > 0f) {
                            val e = easeOutBack(cp)
                            val waveY = (1f - easeOut(cp)) * sin(tau * 8f + i) * bs * 0.45f
                            tpBold.color = withAlpha(textColor, clamp01(cp * 2.2f))
                            canvas.save()
                            canvas.scale(e, e, curX + charW * 0.5f, by1 - bs * 0.35f)
                            canvas.drawText(ch, curX, by1 + waveY, tpBold)
                            canvas.restore()
                        }
                        curX += charW
                    }
                }
                TextStyle.STREAM_A24_EDITORIAL, TextStyle.STREAM_CYBER_CUT -> {
                    // Diagonal razor-split typography where top and bottom halves shear into alignment
                    val e = easeOut(pr)
                    tpBold.textAlign = Paint.Align.CENTER
                    val midY = by1 - bs * 0.36f
                    val shear = (1f - e) * bs * 0.85f
                    canvas.save()
                    canvas.clipRect(0f, by1 - bs * 1.2f, g.w, midY)
                    tpBold.color = withAlpha(textColor, e)
                    canvas.drawText(name, textCenterX + shear, by1, tpBold)
                    canvas.restore()

                    canvas.save()
                    canvas.clipRect(0f, midY, g.w, by1 + bs * 0.45f)
                    tpBold.color = withAlpha(if (textStyle == TextStyle.STREAM_CYBER_CUT && pr < 0.85f) p2 else textColor, e)
                    canvas.drawText(name, textCenterX - shear, by1, tpBold)
                    canvas.restore()
                }

                // =========================================================================================
                // 11 LOGO LOCKUP TYPOGRAPHY ENGINES
                // =========================================================================================
                TextStyle.CASCADE_STAIRCASE -> {
                    tpBold.textAlign = Paint.Align.LEFT
                    var curX = textLeft
                    val n = name.length
                    for (i in 0 until n) {
                        val ch = name[i].toString()
                        val charW = tpBold.measureText(ch)
                        val localT = (tau - i * 0.085f) / 0.48f
                        if (localT > 0f) {
                            val cp = clamp01(localT)
                            val ease = if (cp < 0.82f) {
                                val u = cp / 0.82f; u * u * (3f - 2f * u)
                            } else {
                                val u = (cp - 0.82f) / 0.18f; 1f + sin(u * 3.14159f) * 0.025f
                            }
                            val startY = by1 - (bs * 1.6f + i * bs * 0.72f)
                            val curY = lerp(startY, by1, ease)
                            if (cp in 0.06f..0.80f) {
                                val blurAmt = sin((cp - 0.06f) / 0.74f * 3.14159f)
                                for (b in 1..3) {
                                    tpBold.color = withAlpha(textColor, 0.18f * blurAmt / b)
                                    canvas.drawText(ch, curX, curY - b * bs * 0.14f * blurAmt, tpBold)
                                }
                            }
                            tpBold.color = withAlpha(textColor, clamp01(cp * 3.5f))
                            canvas.drawText(ch, curX, curY, tpBold)
                        }
                        curX += charW
                    }
                }
                TextStyle.BOUNCE_WAVE -> {
                    tpBold.textAlign = Paint.Align.LEFT
                    var curX = textLeft
                    val n = name.length
                    for (i in 0 until n) {
                        val ch = name[i].toString()
                        val charW = tpBold.measureText(ch)
                        val revIdx = (n - 1 - i)
                        val cp = clamp01((tau - revIdx * 0.11f) / 0.45f)
                        if (cp > 0f) {
                            val e = easeOutBack(cp)
                            val dy = (1f - e) * bs * 0.85f
                            tpBold.color = withAlpha(textColor, clamp01(cp * 2.5f))
                            canvas.drawText(ch, curX, by1 + dy, tpBold)
                        }
                        curX += charW
                    }
                }
                TextStyle.FADE_SCALE -> {
                    val e = easeOut(pr)
                    tpBold.textAlign = if (isHorizontal) Paint.Align.LEFT else Paint.Align.CENTER
                    val x = if (isHorizontal) textLeft else textCenterX
                    canvas.save()
                    canvas.scale(0.85f + 0.15f * e, 0.85f + 0.15f * e, textCenterX, by1 - bs * 0.35f)
                    tpBold.color = withAlpha(textColor, e)
                    canvas.drawText(name, x, by1, tpBold)
                    canvas.restore()
                }
                TextStyle.SLIDE_UP -> {
                    val e = easeOut(pr)
                    tpBold.textAlign = if (isHorizontal) Paint.Align.LEFT else Paint.Align.CENTER
                    val x = if (isHorizontal) textLeft else textCenterX
                    canvas.save()
                    canvas.clipRect(0f, by1 - bs * 1.15f, g.w, by1 + bs * 0.35f)
                    tpBold.color = textColor
                    canvas.drawText(name, x, by1 + (1f - e) * bs * 1.15f, tpBold)
                    canvas.restore()
                }
                TextStyle.SPLIT_REVEAL -> {
                    val e = easeOut(pr)
                    tpBold.textAlign = if (isHorizontal) Paint.Align.LEFT else Paint.Align.CENTER
                    val x = if (isHorizontal) textLeft else textCenterX
                    val midY = by1 - bs * 0.35f
                    canvas.save()
                    canvas.clipRect(0f, by1 - bs * 1.2f, g.w, midY)
                    tpBold.color = withAlpha(textColor, e)
                    canvas.drawText(name, x, by1 + (1f - e) * bs * 0.45f, tpBold)
                    canvas.restore()

                    canvas.save()
                    canvas.clipRect(0f, midY, g.w, by1 + bs * 0.4f)
                    tpBold.color = withAlpha(textColor, e)
                    canvas.drawText(name, x, by1 - (1f - e) * bs * 0.45f, tpBold)
                    canvas.restore()
                }
                TextStyle.TYPEWRITER -> {
                    tpBold.textAlign = Paint.Align.LEFT
                    var curX = textLeft
                    val n = name.length
                    for (i in 0 until n) {
                        val ch = name[i].toString()
                        val cp = clamp01(pr * (n + 2f) - i)
                        val adv = tpBold.measureText(ch)
                        if (cp > 0f) {
                            tpBold.color = withAlpha(textColor, cp)
                            canvas.drawText(ch, curX, by1, tpBold)
                        }
                        curX += adv
                    }
                    if (pr < 1f && (floor(tau * 5f).toInt() % 2 == 0)) {
                        val shown = clamp01(pr * (n + 2f) / n)
                        val cxp = textLeft + tpBold.measureText(name) * shown
                        g.rectFill(cxp + bs * 0.05f, by1 - bs * 0.74f, cxp + bs * 0.14f, by1 + bs * 0.06f, p1, 0.9f)
                    }
                }
                TextStyle.GLITCH -> {
                    val e = easeOut(pr)
                    tpBold.textAlign = if (isHorizontal) Paint.Align.LEFT else Paint.Align.CENTER
                    val x = if (isHorizontal) textLeft else textCenterX
                    val j = (1f - pr) * bs * 0.16f * (if (sin(tau * 55f) > 0f) 1f else -1f)
                    if (pr < 1f) {
                        tpBold.color = withAlpha(p1, 0.7f * e); canvas.drawText(name, x + j, by1, tpBold)
                        tpBold.color = withAlpha(p2, 0.7f * e); canvas.drawText(name, x - j, by1, tpBold)
                    }
                    tpBold.color = withAlpha(textColor, e)
                    canvas.drawText(name, x, by1, tpBold)
                }
                TextStyle.ZOOM_BLUR -> {
                    val e = easeOut(pr)
                    tpBold.textAlign = if (isHorizontal) Paint.Align.LEFT else Paint.Align.CENTER
                    val x = if (isHorizontal) textLeft else textCenterX
                    for (k in 4 downTo 0) {
                        val s = 1f + (1f - e) * (0.45f + k * 0.15f)
                        canvas.save(); canvas.scale(s, s, textCenterX, by1 - bs * 0.35f)
                        tpBold.color = withAlpha(textColor, e * (if (k == 0) 1f else 0.14f))
                        canvas.drawText(name, x, by1, tpBold)
                        canvas.restore()
                    }
                }
                TextStyle.SPREAD -> {
                    val e = easeOut(pr)
                    tpBold.textAlign = if (isHorizontal) Paint.Align.LEFT else Paint.Align.CENTER
                    val x = if (isHorizontal) textLeft else textCenterX
                    tpBold.letterSpacing = (1f - e) * 0.45f
                    tpBold.color = withAlpha(textColor, e)
                    canvas.drawText(name, x, by1, tpBold)
                }
                TextStyle.SHINE -> {
                    val e = easeOut(pr)
                    tpBold.textAlign = if (isHorizontal) Paint.Align.LEFT else Paint.Align.CENTER
                    val x = if (isHorizontal) textLeft else textCenterX
                    val tw = tpBold.measureText(name)
                    val hp = fmod(tau * 0.55f, 1.4f) - 0.2f
                    val hx = textLeft + tw * hp
                    tpBold.shader = LinearGradient(
                        hx - tw * 0.18f, 0f, hx + tw * 0.18f, 0f,
                        intArrayOf(textColor, p2, textColor), floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP
                    )
                    tpBold.alpha = (e * 255f).toInt().coerceIn(0, 255)
                    canvas.drawText(name, x, by1, tpBold)
                    tpBold.shader = null
                }
                TextStyle.SIGNATURE -> {
                    val e = easeInOut(pr)
                    tpBold.textAlign = if (isHorizontal) Paint.Align.LEFT else Paint.Align.CENTER
                    val x = if (isHorizontal) textLeft else textCenterX
                    val tw = tpBold.measureText(name)
                    val left = textLeft - bs * 0.1f
                    val edge = left + (tw + bs * 0.2f) * e
                    canvas.save()
                    canvas.clipRect(left, by1 - bs * 1.2f, edge, by1 + bs * 0.45f)
                    tpBold.color = textColor
                    canvas.drawText(name, x, by1, tpBold)
                    canvas.restore()
                    if (pr < 1f) g.glow(edge, by1 - bs * 0.3f, bs * 0.45f, p1, 0.65f * (1f - e))
                }
            }
        }

        // Line 2: Secondary Word / Tagline ("University" or "ORIGINALS")
        val tagStart = if (textStyle == TextStyle.CASCADE_STAIRCASE) 1.25f else (textStart + 0.45f)
        val tagP = clamp01((tau - tagStart) / 0.75f)
        if (tag.isNotEmpty() && tagP > 0f && !(textStyle == TextStyle.STREAM_NETFLIX_PRISM && tau > 2.8f)) {
            val e = easeOut(tagP)
            tpReg.reset(); tpReg.isAntiAlias = true
            tpReg.typeface = tfReg; tpReg.textSize = tagSize
            tpReg.letterSpacing = if (isHorizontal) -0.01f else 0.24f
            tpReg.textAlign = if (isHorizontal) Paint.Align.LEFT else Paint.Align.CENTER
            tpReg.color = withAlpha(textColor, clamp01(tagP * 1.8f) * (if (isHorizontal) 0.96f else 0.88f))
            val x = if (isHorizontal) textLeft else textCenterX

            canvas.save()
            canvas.clipRect(0f, by1 + bs * 0.05f, g.w, by2 + tagSize * 0.45f)
            canvas.drawText(tag, x, lerp(by1 + bs * 0.10f, by2, e), tpReg)
            canvas.restore()
        }

        // Optional Line 3: Subtitle
        val subP = clamp01((tau - (tagStart + 0.55f)) / 0.7f)
        if (sub.isNotEmpty() && subP > 0f) {
            val subSize = tagSize * 0.68f
            tpReg.reset(); tpReg.isAntiAlias = true
            tpReg.typeface = tfReg; tpReg.textSize = subSize
            tpReg.textAlign = if (isHorizontal) Paint.Align.LEFT else Paint.Align.CENTER
            tpReg.color = withAlpha(textColor, easeOut(subP) * 0.75f)
            val x = if (isHorizontal) textLeft else textCenterX
            canvas.drawText(sub, x, by2 + subSize * 1.4f, tpReg)
        }
    }

    /**
     * Deeply Integrated Logo Reveal Engine:
     * Renders sub-frame velocity motion-blur trails during entrance, ambient rim glow underlay,
     * and surface specular light sweep so the logo and scene look like one inseparable render.
     */
    private fun drawLogoReveal(
        canvas: Canvas,
        cfg: BrandConfig,
        spec: TemplateSpec,
        style: LogoStyle,
        tau: Float,
        p1: Int,
        p2: Int,
        bgCutoutColor: Int,
        perchX: Float,
        perchY: Float,
        logoCx: Float,
        logoCy: Float,
        emblemSize: Float,
        camAlpha: Float,
        outroProg: Float,
        inOutStyle: InOutStyle
    ) {
        val mascotType = cfg.mascotOverride ?: spec.mascot
        val emblemType = cfg.emblemOverride ?: spec.emblem
        val bmp = media.logo(cfg.logoPath, cfg.removeLogoBg)

        fun renderEmblemOrBitmap(alpha: Float, tintColor: Int? = null) {
            val finalA = (alpha * camAlpha).coerceIn(0f, 1f)
            if (finalA <= 0.005f) return
            if (bmp != null) {
                val ar = bmp.width.toFloat() / bmp.height
                val dw = if (ar >= 1f) emblemSize else emblemSize * ar
                val dh = if (ar >= 1f) emblemSize / ar else emblemSize
                dst.set(-dw / 2f, -dh / 2f, dw / 2f, dh / 2f)
                bp.reset(); bp.isAntiAlias = true; bp.isFilterBitmap = true
                bp.alpha = (finalA * 255f).toInt().coerceIn(0, 255)
                bp.colorFilter = if (tintColor != null) PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN) else null
                canvas.drawBitmap(bmp, null, dst, bp)
                bp.colorFilter = null
            } else {
                val cPrimary = tintColor ?: p1
                val cSecondary = tintColor ?: p2
                Effects.drawVectorEmblem(g, emblemType, emblemSize, cPrimary, cSecondary, bgCutoutColor, finalA)
            }
        }

        // Style 1: Exact 5-Phase Bird Perch -> Vortex -> Shield Crest
        if (style == LogoStyle.MASCOT_VORTEX_MORPH) {
            val mascotRadius = emblemSize * 0.46f
            if (tau in 2.35f..5.90f) {
                when {
                    tau < 3.55f -> {
                        val fp = clamp01((tau - 2.35f) / 1.20f)
                        val e = easeOut(fp)
                        val startX = perchX + emblemSize * 1.95f
                        val startY = perchY - emblemSize * 1.75f
                        val mx = lerp(startX, perchX, e)
                        val my = lerp(startY, perchY - mascotRadius * 0.34f, e) - sin(fp * 3.14159f) * emblemSize * 0.22f
                        Effects.drawMascot(g, mascotType, mx, my, mascotRadius, (1f - e) * (-14f), tau * 44f, 1f - 0.5f * e, fp > 0.65f, clamp01(fp * 3.5f) * camAlpha)
                    }
                    tau < 5.15f -> {
                        val perchT = tau - 3.55f
                        val isFluttering = tau >= 4.65f
                        val flapPhase = if (isFluttering) (tau - 4.65f) * 48f else 0f
                        val blur = if (isFluttering) 0.85f else 0f
                        val headBobY = if (isFluttering) -sin((tau - 4.65f) * 18f) * mascotRadius * 0.06f else sin(perchT * 5f) * mascotRadius * 0.025f
                        val tilt = if (isFluttering) -6f else sin(perchT * 3.5f) * 2.5f
                        Effects.drawMascot(g, mascotType, perchX, perchY - mascotRadius * 0.34f + headBobY, mascotRadius, tilt, flapPhase, blur, true, camAlpha)
                    }
                    else -> {
                        val hp = clamp01((tau - 5.15f) / 0.70f)
                        val e = easeInOut(hp)
                        val startY = perchY - mascotRadius * 0.34f
                        val mx = lerp(perchX, logoCx, e)
                        val my = lerp(startY, logoCy, e) - sin(hp * 3.14159f) * emblemSize * 0.38f
                        val scale = mascotRadius * (1f - 0.45f * hp * hp)
                        val tilt = -25f * sin(hp * 3.14159f) - hp * 120f
                        val alpha = (1f - easeInOut(clamp01((hp - 0.45f) / 0.55f))) * camAlpha
                        Effects.drawMascot(g, mascotType, mx, my, scale, tilt, tau * 52f, 0.95f, hp < 0.35f, alpha)
                    }
                }
            }
            if (tau in 5.58f..6.72f) {
                Effects.drawVortex(g, logoCx, logoCy, emblemSize, clamp01((tau - 5.58f) / 1.14f))
            }
            val logoRevealP = clamp01((tau - 6.30f) / 0.50f)
            if (logoRevealP > 0f) {
                // Soft ambient rim glow behind the revealed crest
                g.glow(logoCx, logoCy, emblemSize * 0.85f, p2, 0.28f * logoRevealP * camAlpha)
                val sc = (0.82f + 0.18f * easeOutBack(logoRevealP)) * (1f + 0.015f * sin(tau * 3.5f))
                canvas.save(); canvas.translate(logoCx, logoCy); canvas.rotate(cfg.logoRotation); canvas.scale(sc, sc)
                renderEmblemOrBitmap(clamp01(logoRevealP * 2.4f))
                canvas.restore()
            }
            if (tau in 6.42f..7.12f) {
                Effects.drawStarburstFlash(g, logoCx, logoCy, emblemSize, clamp01((tau - 6.42f) / 0.70f))
            }
            return
        }

        // Styles 2..20 with Sub-Frame Motion-Blur Echoes & Ambient Rim-Light Integration
        val lp = clamp01((tau - spec.revealAt) / 0.95f)
        if (lp <= 0f) return

        // Ambient rim-light bleed under the logo so it sits *inside* the lighting environment
        if (cfg.bgMode != BgMode.CHROMA_GREEN) {
            val breathe = 1f + 0.08f * sin(tau * 3.2f)
            g.glow(logoCx, logoCy, emblemSize * 0.95f * breathe, p1, 0.28f * lp * camAlpha)
            g.glow(logoCx, logoCy, emblemSize * 0.65f * breathe, p2, 0.22f * lp * camAlpha)
        }

        // Chromatic Prism Split ghost layers during entrance or prism outro
        if ((inOutStyle == InOutStyle.PRISM_CHROMATIC_WARP && (lp < 0.75f || outroProg > 0.05f))) {
            val splitAmt = if (outroProg > 0.05f) outroProg * emblemSize * 0.28f else (1f - lp / 0.75f) * emblemSize * 0.16f
            canvas.save(); canvas.translate(logoCx - splitAmt, logoCy); canvas.rotate(cfg.logoRotation); renderEmblemOrBitmap(0.45f * lp, p1); canvas.restore()
            canvas.save(); canvas.translate(logoCx + splitAmt, logoCy); canvas.rotate(cfg.logoRotation); renderEmblemOrBitmap(0.45f * lp, p2); canvas.restore()
        }

        val idleBreathe = if (lp >= 1f) (1f + 0.018f * sin(tau * 3.0f)) else 1f
        val idleFloatY = if (lp >= 1f) sin(tau * 2.4f) * emblemSize * 0.018f else 0f

        when (style) {
            LogoStyle.SPLASH_DROP_BURST -> {
                val e = easeOutBack(lp)
                val squash = if (lp < 0.45f) 1f + 0.22f * sin(lp / 0.45f * 3.14159f) else 1f
                // Sub-frame radial motion blur echoes during pop
                if (lp < 0.65f) {
                    for (gIdx in 1..3) {
                        val gs = (e * (1f - gIdx * 0.12f)).coerceAtLeast(0.1f)
                        canvas.save(); canvas.translate(logoCx, logoCy); canvas.scale(gs * squash, gs / squash)
                        renderEmblemOrBitmap(0.18f * (1f - lp / 0.65f))
                        canvas.restore()
                    }
                }
                canvas.save(); canvas.translate(logoCx, logoCy + idleFloatY); canvas.rotate(cfg.logoRotation)
                canvas.scale(e * squash * idleBreathe, (e / squash) * idleBreathe)
                renderEmblemOrBitmap(clamp01(lp * 3.2f))
                canvas.restore()
            }
            LogoStyle.GYRO_SPIN_3D -> {
                val e = easeOut(lp)
                val angleRad = (1f - e) * 1.5f * 3.1415927f
                val cosA = cos(angleRad)
                val scaleX = max(0.06f, abs(cosA)) * (0.75f + 0.25f * e) * idleBreathe
                val scaleY = (0.75f + 0.25f * e) * idleBreathe
                val depthX = sin(angleRad) * emblemSize * 0.09f
                canvas.save(); canvas.translate(logoCx, logoCy + idleFloatY); canvas.rotate(cfg.logoRotation)
                for (layer in 6 downTo 1) {
                    val frac = layer / 6f
                    canvas.save()
                    canvas.translate(depthX * frac, frac * emblemSize * 0.02f)
                    canvas.scale(scaleX, scaleY)
                    renderEmblemOrBitmap(0.38f * e, mixColor(p1, Color.BLACK, 0.55f))
                    canvas.restore()
                }
                canvas.save(); canvas.scale(scaleX, scaleY)
                renderEmblemOrBitmap(clamp01(lp * 2.5f))
                canvas.restore()
                canvas.restore()
            }
            LogoStyle.ORIGAMI_FOLD -> {
                val e = easeOutBack(lp)
                val sx = clamp01(lp * 1.6f) * idleBreathe
                val sy = e * idleBreathe
                if (lp < 0.75f) {
                    for (gIdx in 1..3) {
                        canvas.save()
                        canvas.translate(logoCx, logoCy)
                        canvas.rotate(cfg.logoRotation + (1f - easeOut(lp)) * (-45f - gIdx * 12f))
                        canvas.scale(sx * 0.9f, sy * 0.9f)
                        renderEmblemOrBitmap(0.16f * (1f - lp / 0.75f), p2)
                        canvas.restore()
                    }
                }
                canvas.save(); canvas.translate(logoCx, logoCy + idleFloatY); canvas.rotate(cfg.logoRotation + (1f - easeOut(lp)) * -45f)
                canvas.scale(sx, sy)
                renderEmblemOrBitmap(clamp01(lp * 2.5f))
                canvas.restore()
            }
            LogoStyle.CYBER_GLITCH_SNAP -> {
                val e = easeOut(lp)
                val slot = floor(tau * 24f).toInt()
                val jitter = (1f - lp) * emblemSize * 0.18f * (if (sin(tau * 65f) > 0f) 1f else -1f)
                canvas.save(); canvas.translate(logoCx, logoCy + idleFloatY); canvas.rotate(cfg.logoRotation)
                if (lp < 0.92f) {
                    canvas.save(); canvas.translate(jitter, -jitter * 0.25f); renderEmblemOrBitmap(0.6f * e, p1); canvas.restore()
                    canvas.save(); canvas.translate(-jitter, jitter * 0.25f); renderEmblemOrBitmap(0.6f * e, p2); canvas.restore()
                }
                val snapX = if (lp < 0.8f && hash(slot, 2) < 0.4f) jitter * 0.5f else 0f
                canvas.translate(snapX, 0f)
                canvas.scale(idleBreathe, idleBreathe)
                renderEmblemOrBitmap(clamp01(lp * 2.6f))
                canvas.restore()
            }
            LogoStyle.GALAXY_PARTICLE_CONVERGE -> {
                val e = easeOutBack(lp)
                if (lp < 0.75f) {
                    for (gIdx in 1..3) {
                        val rotOff = (1f - easeOut(lp)) * (120f + gIdx * 22f)
                        val sc = (0.35f + 0.65f * e) * (1f - gIdx * 0.08f)
                        canvas.save(); canvas.translate(logoCx, logoCy); canvas.rotate(cfg.logoRotation + rotOff); canvas.scale(sc, sc)
                        renderEmblemOrBitmap(0.18f * (1f - lp / 0.75f), p2)
                        canvas.restore()
                    }
                }
                canvas.save(); canvas.translate(logoCx, logoCy + idleFloatY); canvas.rotate(cfg.logoRotation + (1f - easeOut(lp)) * 120f)
                val sc = (0.35f + 0.65f * e) * idleBreathe
                canvas.scale(sc, sc)
                renderEmblemOrBitmap(clamp01(lp * 2.2f))
                canvas.restore()
            }
            LogoStyle.LASER_PERIMETER_TRACE -> {
                val e = easeOut(lp)
                canvas.save(); canvas.translate(logoCx, logoCy + idleFloatY); canvas.rotate(cfg.logoRotation)
                val sc = (0.92f + 0.08f * e) * idleBreathe
                canvas.scale(sc, sc)
                renderEmblemOrBitmap(e)
                canvas.restore()
                if (lp < 0.55f) Effects.drawStarburstFlash(g, logoCx, logoCy, emblemSize * 0.85f, lp / 0.55f)
            }
            LogoStyle.FLAME_WALL_WIPE -> {
                val e = easeInOut(lp)
                val clipLeft = -emblemSize * 0.65f
                val clipRight = lerp(clipLeft, emblemSize * 0.65f, e)
                canvas.save(); canvas.translate(logoCx, logoCy + idleFloatY); canvas.rotate(cfg.logoRotation)
                canvas.scale(idleBreathe, idleBreathe)
                canvas.clipRect(clipLeft, -emblemSize * 0.65f, clipRight, emblemSize * 0.65f)
                renderEmblemOrBitmap(1f)
                canvas.restore()
                if (lp < 0.96f) g.glow(logoCx + clipRight * 0.8f, logoCy, emblemSize * 0.38f, p2, 0.85f * sin(lp * 3.14159f))
            }
            LogoStyle.SHUTTER_SPLIT_SLIDE -> {
                val e = easeOut(lp)
                canvas.save(); canvas.translate(logoCx, logoCy + idleFloatY); canvas.rotate(cfg.logoRotation)
                canvas.scale(idleBreathe, idleBreathe)
                canvas.save()
                canvas.clipRect(-emblemSize * 0.65f, -emblemSize * 0.65f, 0f, emblemSize * 0.65f)
                canvas.translate(0f, (1f - e) * emblemSize * 0.65f)
                renderEmblemOrBitmap(e)
                canvas.restore()
                canvas.save()
                canvas.clipRect(0f, -emblemSize * 0.65f, emblemSize * 0.65f, emblemSize * 0.65f)
                canvas.translate(0f, -(1f - e) * emblemSize * 0.65f)
                renderEmblemOrBitmap(e)
                canvas.restore()
                canvas.restore()
            }
            LogoStyle.NEON_TUBE_IGNITE -> {
                val flicker = if (lp < 0.65f && hash(floor(tau * 28f).toInt(), 9) < 0.38f) 0.22f else 1f
                val pulse = 1f + 0.035f * sin(tau * 6.5f)
                canvas.save(); canvas.translate(logoCx, logoCy + idleFloatY); canvas.rotate(cfg.logoRotation)
                canvas.scale(pulse * easeOut(lp), pulse * easeOut(lp))
                renderEmblemOrBitmap(flicker * clamp01(lp * 2.2f))
                canvas.restore()
            }
            LogoStyle.BOUNCING_BALL_MORPH -> {
                val e = easeOutBounce(lp)
                val dy = -(1f - e) * emblemSize * 0.55f
                if (lp < 0.65f) {
                    for (b in 1..2) {
                        canvas.save(); canvas.translate(logoCx, logoCy + dy - b * emblemSize * 0.08f * (1f - lp))
                        renderEmblemOrBitmap(0.18f * (1f - lp / 0.65f))
                        canvas.restore()
                    }
                }
                canvas.save(); canvas.translate(logoCx, logoCy + dy + idleFloatY); canvas.rotate(cfg.logoRotation)
                val sc = (0.75f + 0.25f * easeOutBack(lp)) * idleBreathe
                canvas.scale(sc, sc)
                renderEmblemOrBitmap(clamp01(lp * 3.5f))
                canvas.restore()
            }
            LogoStyle.ROYAL_RIBBON_CROWN -> {
                val e = easeOut(lp)
                canvas.save(); canvas.translate(logoCx, logoCy + idleFloatY); canvas.rotate(cfg.logoRotation)
                val sc = (0.85f + 0.15f * e) * idleBreathe
                canvas.scale(sc, sc)
                renderEmblemOrBitmap(e)
                canvas.restore()
                if (lp < 0.7f) Effects.drawStarburstFlash(g, logoCx, logoCy - emblemSize * 0.35f, emblemSize * 0.55f, lp / 0.7f)
            }
            LogoStyle.LIQUID_METABALL_FUSE -> {
                val e = easeOut(lp)
                val wobble = (1f - e) * 0.18f * sin(tau * 15f)
                canvas.save(); canvas.translate(logoCx, logoCy + idleFloatY); canvas.rotate(cfg.logoRotation)
                canvas.scale((e + wobble) * idleBreathe, (e - wobble) * idleBreathe)
                renderEmblemOrBitmap(clamp01(lp * 2.5f))
                canvas.restore()
            }
            LogoStyle.ISOMETRIC_CUBE_RISE -> {
                val e = easeOutBack(lp)
                val dy = (1f - e) * emblemSize * 0.45f
                canvas.save(); canvas.translate(logoCx, logoCy + dy + idleFloatY); canvas.rotate(cfg.logoRotation)
                canvas.scale(e * idleBreathe, e * idleBreathe)
                renderEmblemOrBitmap(clamp01(lp * 2.5f))
                canvas.restore()
            }
            LogoStyle.LIGHTNING_SHOCKWAVE -> {
                val shakeX = if (lp < 0.45f) sin(tau * 80f) * (1f - lp / 0.45f) * emblemSize * 0.12f else 0f
                val shakeY = if (lp < 0.45f) cos(tau * 75f) * (1f - lp / 0.45f) * emblemSize * 0.08f else 0f
                canvas.save(); canvas.translate(logoCx + shakeX, logoCy + shakeY + idleFloatY); canvas.rotate(cfg.logoRotation)
                val sc = (1.25f - 0.25f * easeOut(lp)) * idleBreathe
                canvas.scale(sc, sc)
                renderEmblemOrBitmap(clamp01(lp * 4f))
                canvas.restore()
            }
            LogoStyle.ZEN_INK_BRUSH -> {
                val e = easeOut(lp)
                canvas.save(); canvas.translate(logoCx, logoCy + idleFloatY); canvas.rotate(cfg.logoRotation + (1f - e) * -15f)
                val sc = (0.88f + 0.12f * e) * idleBreathe
                canvas.scale(sc, sc)
                renderEmblemOrBitmap(e)
                canvas.restore()
            }
            LogoStyle.QUANTUM_PORTAL_WARP -> {
                val e = easeOutBack(lp)
                if (lp < 0.75f) {
                    for (gIdx in 1..3) {
                        val gs = (e * (1f - gIdx * 0.14f)).coerceAtLeast(0.08f)
                        canvas.save(); canvas.translate(logoCx, logoCy); canvas.rotate(cfg.logoRotation + (1f - easeOut(lp)) * (180f + gIdx * 25f)); canvas.scale(gs, gs)
                        renderEmblemOrBitmap(0.18f * (1f - lp / 0.75f), p1)
                        canvas.restore()
                    }
                }
                canvas.save(); canvas.translate(logoCx, logoCy + idleFloatY); canvas.rotate(cfg.logoRotation + (1f - easeOut(lp)) * 180f)
                canvas.scale(e * idleBreathe, e * idleBreathe)
                renderEmblemOrBitmap(clamp01(lp * 2.5f))
                canvas.restore()
            }
            LogoStyle.AUDIO_EQUALIZER_DROP -> {
                val e = easeOutBack(lp)
                val beatPulse = 1f + 0.045f * abs(sin(tau * 8f))
                canvas.save(); canvas.translate(logoCx, logoCy + idleFloatY); canvas.rotate(cfg.logoRotation)
                canvas.scale(e * beatPulse, e * beatPulse)
                renderEmblemOrBitmap(clamp01(lp * 3f))
                canvas.restore()
            }
            LogoStyle.BLUEPRINT_WIREFRAME_SCAN -> {
                val scanP = easeInOut(lp)
                val top = -emblemSize * 0.6f; val bot = emblemSize * 0.6f
                val scanY = lerp(top, bot, scanP)
                canvas.save(); canvas.translate(logoCx, logoCy + idleFloatY); canvas.rotate(cfg.logoRotation)
                canvas.scale(idleBreathe, idleBreathe)
                if (scanP < 1f) {
                    canvas.save()
                    canvas.clipRect(-emblemSize * 0.6f, scanY, emblemSize * 0.6f, bot)
                    renderEmblemOrBitmap(0.26f, p2)
                    canvas.restore()
                }
                canvas.save()
                canvas.clipRect(-emblemSize * 0.6f, top, emblemSize * 0.6f, scanY)
                renderEmblemOrBitmap(1f)
                canvas.restore()
                canvas.restore()
                if (scanP < 0.98f) {
                    g.line(logoCx - emblemSize * 0.55f, logoCy + scanY, logoCx + emblemSize * 0.55f, logoCy + scanY, p2, 0.9f, g.u * 0.38f)
                }
            }
            LogoStyle.ANAMORPHIC_STAR_RISE -> {
                val e = easeOut(lp)
                val dy = (1f - e) * emblemSize * 0.25f
                canvas.save(); canvas.translate(logoCx, logoCy + dy + idleFloatY); canvas.rotate(cfg.logoRotation)
                val sc = (0.88f + 0.12f * e) * idleBreathe
                canvas.scale(sc, sc)
                renderEmblemOrBitmap(e)
                canvas.restore()
                if (lp < 0.75f) Effects.drawStarburstFlash(g, logoCx, logoCy, emblemSize * 0.95f, lp / 0.75f)
            }
            else -> {
                val e = easeOutBack(lp)
                canvas.save(); canvas.translate(logoCx, logoCy + idleFloatY); canvas.rotate(cfg.logoRotation)
                canvas.scale(e * idleBreathe, e * idleBreathe)
                renderEmblemOrBitmap(clamp01(lp * 2.5f))
                canvas.restore()
            }
        }
    }

    private fun drawCheckerboard(canvas: Canvas, fw: Float, fh: Float) {
        val cell = max(16f, min(fw, fh) / 18f)
        bp.reset(); bp.color = 0xFFEDEFF2.toInt()
        canvas.drawRect(0f, 0f, fw, fh, bp)
        bp.color = 0xFFDCE1E7.toInt()
        val cols = (fw / cell).toInt() + 1; val rows = (fh / cell).toInt() + 1
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if ((r + c) % 2 == 0) canvas.drawRect(c * cell, r * cell, (c + 1) * cell, (r + 1) * cell, bp)
            }
        }
    }

    private fun drawBackgroundMedia(canvas: Canvas, cfg: BrandConfig, fw: Float, fh: Float, t: Float, w: Int, h: Int) {
        val maxDim = max(w, h)
        val bmp: Bitmap? = when {
            cfg.bgVideoPath != null -> media.videoFrame(cfg.bgVideoPath, t, maxDim)
            cfg.bgImagePath != null -> media.image(cfg.bgImagePath, maxDim)
            else -> null
        }
        if (cfg.bgVideoPath == null) media.videoFrame(null, 0f, maxDim)
        if (cfg.bgImagePath == null) media.image(null, maxDim)
        if (bmp != null) {
            val s = max(fw / bmp.width, fh / bmp.height)
            val dw = bmp.width * s; val dh = bmp.height * s
            dst.set((fw - dw) / 2f, (fh - dh) / 2f, (fw + dw) / 2f, (fh + dh) / 2f)
            bp.reset(); bp.isAntiAlias = true; bp.isFilterBitmap = true
            canvas.drawBitmap(bmp, null, dst, bp)
        }
    }

    private fun drawOverlay(canvas: Canvas, cfg: BrandConfig, spec: TemplateSpec, fw: Float, fh: Float, t: Float, duration: Float) {
        if ((cfg.bgMode == BgMode.AI_AUTO || cfg.bgMode == BgMode.SOLID) && spec.vignette > 0.01f) {
            bp.reset(); bp.isAntiAlias = true
            bp.shader = RadialGradient(
                fw / 2f, fh / 2f, max(fw, fh) * 0.78f,
                intArrayOf(withAlpha(Color.BLACK, 0f), withAlpha(Color.BLACK, spec.vignette)),
                floatArrayOf(0.58f, 1f), Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, fw, fh, bp)
        }
        if (cfg.watermarkEnabled) {
            tpReg.reset(); tpReg.isAntiAlias = true
            tpReg.typeface = regTypeface(0); tpReg.textSize = g.u * 2.2f
            tpReg.textAlign = Paint.Align.RIGHT
            val wmCol = if (spec.isLightPaper) 0xFF013929.toInt() else Color.WHITE
            tpReg.color = withAlpha(wmCol, 0.35f)
            canvas.drawText("logoanimationai.com · NH Intro Studio", fw - g.u * 3f, fh - g.u * 2.5f, tpReg)
        }
    }
}
