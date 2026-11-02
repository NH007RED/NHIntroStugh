package com.nh.introstudio.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.floor
import kotlin.math.pow

fun clamp01(x: Float): Float = if (x < 0f) 0f else if (x > 1f) 1f else x
fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
fun fmod(a: Float, b: Float): Float = a - b * floor(a / b)

fun easeOut(t: Float): Float {
    val x = 1f - clamp01(t)
    return 1f - x * x * x
}

fun easeInOut(t: Float): Float {
    val x = clamp01(t)
    return if (x < 0.5f) 4f * x * x * x else 1f - (-2f * x + 2f).pow(3) / 2f
}

fun easeOutBack(t: Float): Float {
    val x = clamp01(t)
    val c1 = 1.70158f
    val c3 = c1 + 1f
    return 1f + c3 * (x - 1f).pow(3) + c1 * (x - 1f).pow(2)
}

fun easeOutBounce(t: Float): Float {
    var x = clamp01(t)
    val n = 7.5625f
    val d = 2.75f
    return when {
        x < 1f / d -> n * x * x
        x < 2f / d -> { x -= 1.5f / d; n * x * x + 0.75f }
        x < 2.5f / d -> { x -= 2.25f / d; n * x * x + 0.9375f }
        else -> { x -= 2.625f / d; n * x * x + 0.984375f }
    }
}

fun withAlpha(c: Int, a: Float): Int =
    (c and 0x00FFFFFF) or ((a.coerceIn(0f, 1f) * 255f).toInt() shl 24)

fun mixColor(c1: Int, c2: Int, t: Float): Int {
    val k = clamp01(t)
    val a = ((c1 ushr 24) + (((c2 ushr 24) - (c1 ushr 24)) * k)).toInt()
    val r = (((c1 shr 16) and 255) + ((((c2 shr 16) and 255) - ((c1 shr 16) and 255)) * k)).toInt()
    val g = (((c1 shr 8) and 255) + ((((c2 shr 8) and 255) - ((c1 shr 8) and 255)) * k)).toInt()
    val b = ((c1 and 255) + (((c2 and 255) - (c1 and 255)) * k)).toInt()
    return (a shl 24) or (r shl 16) or (g shl 8) or b
}

/** Deterministic pseudo random value in [0,1) so every frame can be rendered independently (scrubbing / export). */
fun hash(seed: Int, k: Int = 0): Float {
    var h = seed * 374761393 + k * 668265263 + 1274126177
    h = (h xor (h ushr 13)) * 1274126177.toInt()
    h = h xor (h ushr 16)
    return (h and 0xFFFFFF) / 16777216f
}

/** Small drawing toolkit shared by every template effect. */
internal class Gfx {
    lateinit var c: Canvas
    var w = 0f
    var h = 0f
    var u = 0f
    var cx = 0f
    var cy = 0f
    var tau = 0f
    var p1 = 0
    var p2 = 0
    var p3 = 0
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val path = Path()
    val rect = RectF()

    fun begin(canvas: Canvas, w: Float, h: Float, tau: Float, p1: Int, p2: Int, p3: Int) {
        this.c = canvas; this.w = w; this.h = h; this.tau = tau
        this.u = minOf(w, h) / 100f; this.cx = w / 2f; this.cy = h / 2f
        this.p1 = p1; this.p2 = p2; this.p3 = p3
    }

    fun rnd(seed: Int, k: Int = 0): Float = hash(seed, k)

    fun fillP(color: Int, a: Float = 1f): Paint {
        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.FILL; paint.color = withAlpha(color, a)
        return paint
    }

    fun strokeP(color: Int, a: Float, sw: Float): Paint {
        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE; paint.strokeWidth = sw
        paint.strokeCap = Paint.Cap.ROUND; paint.color = withAlpha(color, a)
        return paint
    }

    fun circle(x: Float, y: Float, r: Float, color: Int, a: Float = 1f) {
        if (a <= 0.004f || r <= 0f) return
        c.drawCircle(x, y, r, fillP(color, a))
    }

    fun ring(x: Float, y: Float, r: Float, color: Int, a: Float, sw: Float) {
        if (a <= 0.004f || r <= 0f) return
        c.drawCircle(x, y, r, strokeP(color, a, sw))
    }

    fun line(x1: Float, y1: Float, x2: Float, y2: Float, color: Int, a: Float, sw: Float) {
        if (a <= 0.004f) return
        c.drawLine(x1, y1, x2, y2, strokeP(color, a, sw))
    }

    fun rectFill(l: Float, t: Float, r: Float, b: Float, color: Int, a: Float) {
        if (a <= 0.004f) return
        c.drawRect(l, t, r, b, fillP(color, a))
    }

    fun glow(x: Float, y: Float, r: Float, color: Int, a: Float) {
        if (a <= 0.004f || r <= 1f) return
        paint.reset(); paint.isAntiAlias = true
        paint.shader = RadialGradient(x, y, r, intArrayOf(withAlpha(color, a), withAlpha(color, 0f)), null, Shader.TileMode.CLAMP)
        c.drawCircle(x, y, r, paint)
    }
}
