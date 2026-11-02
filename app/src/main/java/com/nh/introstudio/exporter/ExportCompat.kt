package com.nh.introstudio.exporter

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build
import com.nh.introstudio.domain.Codec
import com.nh.introstudio.domain.ExportSettings
import com.nh.introstudio.domain.Quality
import kotlin.math.max

/** Checks whether this device can really render and encode a chosen configuration, and proposes a supported one if not. */
object ExportCompat {

    data class Result(
        val ok: Boolean,
        val width: Int,
        val height: Int,
        val encoderName: String?,
        val message: String,
        val suggestion: ExportSettings?
    )

    private fun isSoftware(i: MediaCodecInfo): Boolean =
        if (Build.VERSION.SDK_INT >= 29) i.isSoftwareOnly else i.name.startsWith("OMX.google.") || i.name.startsWith("c2.android.")

    private fun findEncoder(mime: String): MediaCodecInfo? {
        val list = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.filter { info ->
            info.isEncoder && info.supportedTypes.any { it.equals(mime, true) }
        }
        return list.firstOrNull { !isSoftware(it) } ?: list.firstOrNull()
    }

    fun supportedBitrateRange(set: ExportSettings): IntRange? {
        val info = findEncoder(set.codec.mime) ?: return null
        val r = info.getCapabilitiesForType(set.codec.mime).videoCapabilities.bitrateRange
        return r.lower..r.upper
    }

    /** Returns null when supported, otherwise a human readable reason. */
    private fun problem(set: ExportSettings): Pair<String, String?> {
        val (w, h) = set.dimensions()
        val info = findEncoder(set.codec.mime) ?: return "This device has no ${set.codec.label} encoder." to null
        val vc = info.getCapabilitiesForType(set.codec.mime).videoCapabilities
        if (!vc.isSizeSupported(w, h)) return "This device's ${set.codec.label} encoder cannot encode $w×$h." to null
        if (!vc.areSizeAndRateSupported(w, h, set.fps.toDouble())) return "This device's ${set.codec.label} encoder cannot encode $w×$h at ${set.fps} FPS." to null
        val maxTex = EncoderGl.maxTextureSize()
        if (max(w, h) > maxTex) return "This device's GPU supports textures up to $maxTex px, so $w×$h cannot be rendered." to null
        val need = w.toLong() * h * 4L * 2L
        if (need > Runtime.getRuntime().maxMemory() * 0.7) return "There is not enough memory to render $w×$h on this device." to null
        return "" to info.name
    }

    fun check(set: ExportSettings): Result {
        val (w, h) = set.dimensions()
        val (reason, enc) = problem(set)
        if (reason.isEmpty() && enc != null) {
            return Result(true, w, h, enc, "Supported: $w×$h @ ${set.fps} FPS (${set.codec.label}).", null)
        }
        val suggestion = suggest(set)
        val msg = if (suggestion != null) {
            val (sw, sh) = suggestion.dimensions()
            "$reason Try ${suggestion.quality.label} $sw×$sh @ ${suggestion.fps} FPS (${suggestion.codec.label}) instead."
        } else "$reason No supported alternative was found."
        return Result(false, w, h, null, msg, suggestion)
    }

    private fun ok(set: ExportSettings) = problem(set).first.isEmpty()

    private fun suggest(set: ExportSettings): ExportSettings? {
        val tries = ArrayList<ExportSettings>()
        if (set.fps > 30) tries += set.copy(fps = 30)
        if (set.codec == Codec.H265) { tries += set.copy(codec = Codec.H264); if (set.fps > 30) tries += set.copy(codec = Codec.H264, fps = 30) }
        val lower = Quality.values().filter { it.refH < set.quality.refH }.sortedByDescending { it.refH }
        for (q in lower) {
            tries += set.copy(quality = q, fps = minOf(set.fps, 30))
            tries += set.copy(quality = q, fps = minOf(set.fps, 30), codec = Codec.H264)
        }
        return tries.firstOrNull { ok(it) }
    }
}
