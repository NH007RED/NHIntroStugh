package com.nh.introstudio.utils

import java.text.DateFormat
import java.util.Date
import java.util.Locale

object Fmt {
    fun time(sec: Float): String {
        val s = sec.coerceAtLeast(0f)
        val m = (s / 60).toInt()
        val r = s - m * 60
        return String.format(Locale.US, "%d:%04.1f", m, r)
    }

    fun size(bytes: Long): String = when {
        bytes >= 1_048_576L -> String.format(Locale.US, "%.1f MB", bytes / 1_048_576.0)
        bytes >= 1024L -> String.format(Locale.US, "%.0f KB", bytes / 1024.0)
        else -> "$bytes B"
    }

    fun date(ms: Long): String = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(ms))

    fun hex(color: Int): String = String.format(Locale.US, "#%06X", color and 0xFFFFFF)

    fun parseHex(text: String): Int? {
        val t = text.trim().removePrefix("#")
        if (t.length != 6) return null
        return t.toIntOrNull(16)?.let { 0xFF000000.toInt() or it }
    }
}
