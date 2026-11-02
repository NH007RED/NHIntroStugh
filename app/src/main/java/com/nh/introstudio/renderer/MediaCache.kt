package com.nh.introstudio.renderer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import com.nh.introstudio.domain.LogoAiAnalyzer
import kotlin.math.max

/** Decodes and caches the user's logo, background image and background video frames. Not thread safe: one instance per render thread. */
class MediaCache {
    private var logoPath: String? = null
    private var logoRemoveBg: Boolean = false
    private var logo: Bitmap? = null
    private var imgPath: String? = null
    private var img: Bitmap? = null
    private var vidPath: String? = null
    private var retriever: MediaMetadataRetriever? = null
    private var vidDurMs = 0L
    private val frames = LinkedHashMap<Int, Bitmap>()

    fun logo(path: String?, removeBg: Boolean = false): Bitmap? {
        if (path == null) { logoPath = null; logo = null; return null }
        if (path != logoPath || removeBg != logoRemoveBg) {
            logoPath = path
            logoRemoveBg = removeBg
            val raw = decode(path, 1024, false)
            logo = if (raw != null && removeBg) LogoAiAnalyzer.removeSolidBackground(raw) else raw
        }
        return logo
    }

    fun image(path: String?, maxDim: Int): Bitmap? {
        if (path == null) { imgPath = null; img = null; return null }
        if (path != imgPath) { imgPath = path; img = decode(path, max(512, maxDim), true) }
        return img
    }

    fun videoFrame(path: String?, tSec: Float, maxDim: Int): Bitmap? {
        if (path == null) { closeRetriever(); return null }
        if (path != vidPath) {
            closeRetriever()
            vidPath = path
            try {
                val r = MediaMetadataRetriever()
                r.setDataSource(path)
                vidDurMs = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                retriever = r
            } catch (e: Exception) {
                retriever = null
            }
        }
        val r = retriever ?: return null
        val q = (tSec * 15f).toInt()
        frames[q]?.let { return it }
        return try {
            var ms = (q * 1000L) / 15L
            if (vidDurMs > 0L) ms %= vidDurMs
            var bmp = r.getFrameAtTime(ms * 1000L, MediaMetadataRetriever.OPTION_CLOSEST) ?: return null
            val big = max(bmp.width, bmp.height)
            if (big > maxDim) {
                val s = maxDim.toFloat() / big
                bmp = Bitmap.createScaledBitmap(bmp, (bmp.width * s).toInt().coerceAtLeast(2), (bmp.height * s).toInt().coerceAtLeast(2), true)
            }
            frames[q] = bmp
            if (frames.size > 24) frames.remove(frames.keys.first())
            bmp
        } catch (e: Exception) {
            null
        }
    }

    private fun closeRetriever() {
        try { retriever?.release() } catch (_: Exception) {}
        retriever = null; vidPath = null; frames.clear()
    }

    fun release() {
        closeRetriever(); logo = null; img = null; logoPath = null; imgPath = null
    }

    private fun decode(path: String, maxDim: Int, applyExif: Boolean): Bitmap? {
        return try {
            val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, o)
            var sample = 1
            while (max(o.outWidth, o.outHeight) / sample > maxDim * 2) sample *= 2
            val o2 = BitmapFactory.Options().apply { inSampleSize = sample }
            var bmp = BitmapFactory.decodeFile(path, o2) ?: return null
            val big = max(bmp.width, bmp.height)
            if (big > maxDim) {
                val s = maxDim.toFloat() / big
                bmp = Bitmap.createScaledBitmap(bmp, (bmp.width * s).toInt().coerceAtLeast(1), (bmp.height * s).toInt().coerceAtLeast(1), true)
            }
            if (applyExif) {
                val deg = when (ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
                if (deg != 0f) {
                    val m = Matrix(); m.postRotate(deg)
                    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
                }
            }
            bmp
        } catch (e: Throwable) {
            null
        }
    }
}
