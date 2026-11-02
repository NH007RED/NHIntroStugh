package com.nh.introstudio.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nh.introstudio.domain.AspectRatio
import com.nh.introstudio.domain.Codec
import com.nh.introstudio.domain.Quality
import com.nh.introstudio.ui.components.ChoiceChips
import com.nh.introstudio.ui.components.GlassCard
import com.nh.introstudio.ui.components.NeonButton
import com.nh.introstudio.ui.components.ScreenHeader
import com.nh.introstudio.ui.components.SectionLabel
import com.nh.introstudio.ui.theme.NH
import com.nh.introstudio.viewmodel.AppViewModel

@Composable
fun SettingsScreen(app: AppViewModel, onAbout: () -> Unit, onFavorites: () -> Unit) {
    val s by app.settings.collectAsState()
    val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Settings", subtitle = "Defaults for new exports and the editor")
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    SectionLabel("Default resolution")
                    ChoiceChips(Quality.values().toList(), s.quality, { it.label }, { app.updateSettings(s.copy(quality = it)) })
                    SectionLabel("Default aspect ratio")
                    ChoiceChips(AspectRatio.values().toList(), s.aspect, { "${it.label} ${it.sub}" }, { app.updateSettings(s.copy(aspect = it)) })
                    SectionLabel("Default frame rate")
                    ChoiceChips(listOf(30, 60), s.fps, { "$it FPS" }, { app.updateSettings(s.copy(fps = it)) })
                    SectionLabel("Default codec")
                    ChoiceChips(Codec.values().toList(), s.codec, { it.label }, { app.updateSettings(s.copy(codec = it)) })
                    SectionLabel("Live preview quality")
                    ChoiceChips(listOf(0, 1, 2), s.previewQuality, { listOf("Low (480p)", "Medium (720p)", "High (1080p)")[it] }, { app.updateSettings(s.copy(previewQuality = it)) })
                    Text("Lower preview quality is smoother on older phones. Export quality is not affected.", color = NH.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            NeonButton("Favorite templates", onFavorites, Modifier.fillMaxWidth(), outlined = true)
            Spacer(Modifier.height(10.dp))
            NeonButton("Clear temporary files", {
                val n = clearTemp(context)
                Toast.makeText(context, "Cleared $n temporary file(s).", Toast.LENGTH_SHORT).show()
            }, Modifier.fillMaxWidth(), Icons.Filled.Delete, outlined = true)
            Spacer(Modifier.height(10.dp))
            NeonButton("Reset settings", { app.resetSettings() }, Modifier.fillMaxWidth(), Icons.Filled.RestartAlt, outlined = true)
            Spacer(Modifier.height(10.dp))
            NeonButton("About NH Intro Studio", onAbout, Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))
        }
    }
}

private fun clearTemp(context: Context): Int {
    var n = 0
    context.cacheDir.listFiles()?.forEach { f ->
        if (f.isFile && f.name.startsWith("export_") && f.delete()) n++
    }
    return n
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val version = remember(context) {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0" } catch (e: Exception) { "1.0.0" }
    }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("About", onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NH INTRO STUDIO v4", color = NH.Text, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                    Text("Version $version · 40 Logo AI & Streaming Text Intros · com.nh.introstudio", color = NH.Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            AboutCard(
                "What it does",
                "Generates broadcast-grade Logo Animations (Templates 1–20, with unified light-wrap, sub-frame motion blur, and deep In/Out camera transitions) AND Streaming & Cinema Text Intros (Templates 21–40, inspired by Netflix, Prime Video, Hotstar, Marvel, Apple TV+, HBO, IMAX, Dolby, Universal, Columbia, JioCinema, Zee5, A24, Hulu, Paramount, Warner, Peacock, YouTube Originals, Cyberpunk, and Epic Trailers)."
            )
            AboutCard("Originality & copyright", "All 40 templates, animations, sound effects and the 6 bundled music tracks are original and procedurally generated for this app. Import only logos and audio you are authorized to use.")
            AboutCard("Built with", "Kotlin · Jetpack Compose · Material 3 · MVVM · Coroutines · Room · DataStore · Media3 · MediaCodec / MediaMuxer · OpenGL ES")
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AboutCard(title: String, body: String) {
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = NH.Text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(body, color = NH.Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
        }
    }
}
