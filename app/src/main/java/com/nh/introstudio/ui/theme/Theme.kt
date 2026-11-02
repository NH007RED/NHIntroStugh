package com.nh.introstudio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object NH {
    val Black = Color(0xFF05030A)
    val DeepPurple = Color(0xFF1A0B33)
    val Surface = Color(0xFF120A22)
    val Purple = Color(0xFFB026FF)
    val Red = Color(0xFFFF2D55)
    val Text = Color(0xFFF4F0FF)
    val Muted = Color(0xFFA79BC4)
    val Green = Color(0xFF34D399)
}

private val scheme = darkColorScheme(
    primary = NH.Purple,
    onPrimary = Color.White,
    secondary = NH.Red,
    onSecondary = Color.White,
    background = NH.Black,
    onBackground = NH.Text,
    surface = NH.Surface,
    onSurface = NH.Text,
    surfaceVariant = Color(0xFF241638),
    onSurfaceVariant = NH.Muted,
    outline = Color(0xFF4A3A68),
    error = Color(0xFFFF6B81)
)

@Composable
fun NHTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
