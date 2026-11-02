package com.nh.introstudio.domain

import android.graphics.Bitmap
import android.graphics.Color
import com.nh.introstudio.templates.TemplateCatalog
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Offline AI motion & logo analyzer for NH Intro Studio v4:
 * Maps natural-language prompts or uploaded logo characteristics to any of the 40 templates
 * (20 Deep Logo AI templates + 20 Streaming & Cinema Text Intro templates).
 */
object LogoAiAnalyzer {

    data class LogoAnalysis(
        val primaryColor: Int,
        val secondaryColor: Int,
        val darkTextColor: Int,
        val hasTransparency: Boolean,
        val warmth: Float,
        val brightness: Float
    )

    fun analyzeBitmap(bmp: Bitmap?): LogoAnalysis {
        if (bmp == null) {
            return LogoAnalysis(0xFF00B179.toInt(), 0xFF7DD5B4.toInt(), 0xFF013929.toInt(), true, 0.35f, 0.7f)
        }
        val w = bmp.width
        val h = bmp.height
        val stepX = max(1, w / 32)
        val stepY = max(1, h / 32)

        var transparentCount = 0
        var totalSamples = 0
        val buckets = IntArray(12)
        val bucketR = IntArray(12)
        val bucketG = IntArray(12)
        val bucketB = IntArray(12)
        var warmSum = 0f
        var brightSum = 0f
        var opaqueSamples = 0

        val hsv = FloatArray(3)
        for (y in 0 until h step stepY) {
            for (x in 0 until w step stepX) {
                totalSamples++
                val px = bmp.getPixel(x, y)
                val a = Color.alpha(px)
                if (a < 40) {
                    transparentCount++
                    continue
                }
                val r = Color.red(px)
                val g = Color.green(px)
                val b = Color.blue(px)
                Color.RGBToHSV(r, g, b, hsv)
                opaqueSamples++
                brightSum += hsv[2]
                warmSum += if (r >= b) 1f else 0f

                val weight = if (hsv[1] > 0.25f && hsv[2] > 0.2f) 3 else 1
                val idx = ((hsv[0] / 30f).toInt()).coerceIn(0, 11)
                buckets[idx] += weight
                bucketR[idx] += r * weight
                bucketG[idx] += g * weight
                bucketB[idx] += b * weight
            }
        }

        val hasAlpha = transparentCount > totalSamples * 0.08f
        if (opaqueSamples == 0) {
            return LogoAnalysis(0xFF00B179.toInt(), 0xFF7DD5B4.toInt(), 0xFF013929.toInt(), true, 0.35f, 0.7f)
        }

        var best1 = 0
        var best2 = 6
        for (i in 0 until 12) {
            if (buckets[i] > buckets[best1]) {
                best2 = best1
                best1 = i
            } else if (i != best1 && buckets[i] > buckets[best2]) {
                best2 = i
            }
        }

        fun colorFromBucket(b: Int, fallback: Int): Int {
            val cnt = buckets[b]
            if (cnt <= 0) return fallback
            val r = (bucketR[b] / cnt).coerceIn(30, 235)
            val g = (bucketG[b] / cnt).coerceIn(30, 235)
            val bl = (bucketB[b] / cnt).coerceIn(30, 235)
            return Color.rgb(r, g, bl)
        }

        val c1 = colorFromBucket(best1, 0xFF00B179.toInt())
        val c2 = mixWithWhite(c1, 0.48f)
        val darkText = darkenColor(c1, 0.28f)
        return LogoAnalysis(
            primaryColor = c1,
            secondaryColor = c2,
            darkTextColor = darkText,
            hasTransparency = hasAlpha,
            warmth = (warmSum / opaqueSamples).coerceIn(0f, 1f),
            brightness = (brightSum / opaqueSamples).coerceIn(0f, 1f)
        )
    }

    fun removeSolidBackground(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        if (w < 4 || h < 4) return src

        val corners = intArrayOf(
            src.getPixel(0, 0),
            src.getPixel(w - 1, 0),
            src.getPixel(0, h - 1),
            src.getPixel(w - 1, h - 1)
        )
        if (corners.count { Color.alpha(it) < 32 } >= 2) return src

        val bgR = corners.map { Color.red(it) }.average().toInt()
        val bgG = corners.map { Color.green(it) }.average().toInt()
        val bgB = corners.map { Color.blue(it) }.average().toInt()

        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)
        val lowThresh = 26f
        val highThresh = 68f

        for (i in pixels.indices) {
            val px = pixels[i]
            val a = Color.alpha(px)
            if (a == 0) continue
            val r = Color.red(px)
            val g = Color.green(px)
            val b = Color.blue(px)
            val dr = (r - bgR).toFloat()
            val dg = (g - bgG).toFloat()
            val db = (b - bgB).toFloat()
            val dist = sqrt(dr * dr + dg * dg + db * db)
            if (dist <= lowThresh) {
                pixels[i] = 0x00000000
            } else if (dist < highThresh) {
                val factor = ((dist - lowThresh) / (highThresh - lowThresh)).coerceIn(0f, 1f)
                val newA = (a * factor).toInt().coerceIn(0, 255)
                pixels[i] = Color.argb(newA, r, g, b)
            }
        }
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return out
    }

    fun designFromPrompt(prompt: String, current: BrandConfig, logoBmp: Bitmap?): BrandConfig {
        val p = prompt.trim().lowercase()
        val analysis = analyzeBitmap(logoBmp)

        val templateId = when {
            p.contains("netflix") || p.contains("n-ribbon") || p.contains("prism") -> 21
            p.contains("prime") || p.contains("amazon") || p.contains("smile") -> 22
            p.contains("hotstar") || p.contains("disney") || p.contains("shooting star") -> 23
            p.contains("marvel") || p.contains("flipbook") || p.contains("comic") -> 24
            p.contains("apple") || p.contains("glass") || p.contains("refraction") -> 25
            p.contains("hbo") || p.contains("chrome") || p.contains("spotlight") -> 26
            p.contains("imax") -> 27
            p.contains("dolby") || p.contains("atmos") -> 28
            p.contains("universal") || p.contains("globe") || p.contains("planet") -> 29
            p.contains("columbia") || p.contains("sony") || p.contains("torch") -> 30
            p.contains("jio") || p.contains("stadium") -> 31
            p.contains("zee") || p.contains("aurora") -> 32
            p.contains("a24") || p.contains("editorial") -> 33
            p.contains("hulu") || p.contains("sonic") -> 34
            p.contains("paramount") || p.contains("halo") -> 35
            p.contains("warner") || p.contains("monolith") -> 36
            p.contains("peacock") || p.contains("bloom") -> 37
            p.contains("youtube") || p.contains("play") -> 38
            p.contains("cyberpunk") || p.contains("slash") -> 39
            p.contains("titanium") || p.contains("trailer") || p.contains("molten") -> 40
            p.contains("bird") || p.contains("perch") || p.contains("university") -> 1
            p.contains("splash") || p.contains("paint") || p.contains("drop") -> 2
            p.contains("3d") || p.contains("spin") || p.contains("gyro") || p.contains("coin") -> 3
            p.contains("origami") || p.contains("fold") || p.contains("paper") -> 4
            p.contains("glitch") || p.contains("rgb") || p.contains("hack") -> 5
            p.contains("particle") || p.contains("galaxy") || p.contains("burst") -> 6
            p.contains("laser") || p.contains("trace") || p.contains("outline") -> 7
            p.contains("fire") || p.contains("flame") || p.contains("blaze") -> 8
            p.contains("minimal") || p.contains("shutter") || p.contains("split") -> 9
            p.contains("neon") || p.contains("tube") || p.contains("cyber") -> 10
            p.contains("bounce") || p.contains("ball") || p.contains("kinetic") -> 11
            p.contains("royal") || p.contains("gold") || p.contains("ribbon") || p.contains("crown") -> 12
            p.contains("liquid") || p.contains("metaball") || p.contains("morph") -> 13
            p.contains("cube") || p.contains("isometric") || p.contains("matrix") -> 14
            p.contains("lightning") || p.contains("thunder") || p.contains("storm") -> 15
            p.contains("ink") || p.contains("brush") || p.contains("zen") || p.contains("enso") -> 16
            p.contains("portal") || p.contains("quantum") || p.contains("warp") -> 17
            p.contains("audio") || p.contains("equalizer") || p.contains("music") || p.contains("beat") -> 18
            p.contains("blueprint") || p.contains("sketch") || p.contains("wireframe") || p.contains("scan") -> 19
            p.contains("4k") || p.contains("flare") || p.contains("cinema") || p.contains("star") -> 20
            else -> when {
                analysis.warmth > 0.65f -> 8
                analysis.brightness < 0.45f -> 3
                else -> 1
            }
        }

        val speed = when {
            p.contains("fast") || p.contains("quick") || p.contains("snappy") -> 1.25f
            p.contains("slow") || p.contains("calm") || p.contains("smooth") -> 0.85f
            else -> 1.0f
        }

        val bgMode = when {
            p.contains("transparent") || p.contains("alpha") || p.contains("overlay") -> BgMode.TRANSPARENT
            p.contains("green screen") || p.contains("chroma") -> BgMode.CHROMA_GREEN
            p.contains("solid") -> BgMode.SOLID
            else -> current.bgMode
        }

        val spec = TemplateCatalog.byId(templateId)
        val useColors = logoBmp != null && !spec.isTextIntro
        val c1 = if (useColors) analysis.primaryColor else spec.c1
        val c2 = if (useColors) analysis.secondaryColor else spec.c2
        val tCol = if (useColors && spec.isLightPaper) analysis.darkTextColor else spec.textColor
        val track = BUILTIN_MUSIC.firstOrNull { it.id == spec.defaultMusic } ?: BUILTIN_MUSIC.first()

        return current.copy(
            templateId = templateId,
            motionPrompt = prompt,
            showLogo = if (spec.isTextIntro) false else current.showLogo,
            layoutMode = if (spec.isTextIntro) LayoutMode.TEXT_HERO else LayoutMode.AUTO,
            logoStyleOverride = spec.logoStyle,
            textStyleOverride = spec.textStyle,
            inOutStyleOverride = spec.inOutStyle,
            mascotOverride = spec.mascot,
            emblemOverride = spec.emblem,
            durationSec = if (templateId == 1) 8f else 6f,
            speed = speed,
            bgMode = bgMode,
            useCustomColors = useColors,
            colorPrimary = c1,
            colorSecondary = c2,
            colorText = tCol,
            musicSource = "builtin:${track.id}",
            musicName = track.title
        )
    }

    private fun mixWithWhite(c: Int, f: Float): Int {
        val r = (Color.red(c) + (255 - Color.red(c)) * f).toInt().coerceIn(0, 255)
        val g = (Color.green(c) + (255 - Color.green(c)) * f).toInt().coerceIn(0, 255)
        val b = (Color.blue(c) + (255 - Color.blue(c)) * f).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }

    private fun darkenColor(c: Int, factor: Float): Int {
        val r = (Color.red(c) * factor).toInt().coerceIn(5, 80)
        val g = (Color.green(c) * factor).toInt().coerceIn(20, 90)
        val b = (Color.blue(c) * factor).toInt().coerceIn(15, 80)
        return Color.rgb(r, g, b)
    }
}
