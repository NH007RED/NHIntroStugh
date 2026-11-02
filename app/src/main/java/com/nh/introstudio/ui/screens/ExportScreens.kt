package com.nh.introstudio.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.nh.introstudio.domain.AspectRatio
import com.nh.introstudio.domain.Codec
import com.nh.introstudio.domain.Quality
import com.nh.introstudio.exporter.ExportManager
import com.nh.introstudio.exporter.ExportState
import com.nh.introstudio.templates.TemplateCatalog
import com.nh.introstudio.ui.components.ChoiceChips
import com.nh.introstudio.ui.components.ConfirmDialog
import com.nh.introstudio.ui.components.GlassCard
import com.nh.introstudio.ui.components.NeonButton
import com.nh.introstudio.ui.components.ScreenHeader
import com.nh.introstudio.ui.components.SectionLabel
import com.nh.introstudio.ui.components.SliderRow
import com.nh.introstudio.ui.theme.NH
import com.nh.introstudio.utils.Fmt
import com.nh.introstudio.utils.Share
import com.nh.introstudio.viewmodel.AppViewModel
import com.nh.introstudio.viewmodel.EditorViewModel
import com.nh.introstudio.viewmodel.ExportViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun ExportSettingsScreen(
    editor: EditorViewModel,
    exportVm: ExportViewModel,
    app: AppViewModel,
    projectId: Long,
    onBack: () -> Unit,
    onStarted: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(projectId) { editor.load(projectId) }
    val ui by editor.ui.collectAsState()
    val settings by exportVm.settings.collectAsState()
    val compat by exportVm.compat.collectAsState()
    val appSettings by app.settings.collectAsState()
    LaunchedEffect(ui.cfg.aspect, appSettings) { exportVm.initFrom(appSettings, ui.cfg.aspect) }

    val startNow = {
        editor.flush()
        exportVm.start(ui.projectId, ui.name, ui.cfg)
        onStarted()
    }
    val storageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { startNow() }
    val afterNotif = {
        if (Build.VERSION.SDK_INT < 29 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) storageLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE) else startNow()
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { afterNotif() }
    val onStart = {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) else afterNotif()
    }

    val (w, h) = settings.copy(aspect = ui.cfg.aspect).dimensions()
    val bitrate = settings.copy(aspect = ui.cfg.aspect).effectiveBitrate()
    val estBytes = (bitrate / 8.0 * ui.cfg.durationSec + 192_000 / 8.0 * ui.cfg.durationSec).toLong()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Export settings", subtitle = ui.name, onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("${TemplateCatalog.byId(ui.cfg.templateId).name} · ${Fmt.time(ui.cfg.durationSec)}", color = NH.Text, fontWeight = FontWeight.SemiBold)
                    Text("$w × $h px · ${settings.fps} FPS · ${settings.codec.label} · AAC audio · MP4", color = NH.Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                    Text("Estimated size ≈ ${Fmt.size(estBytes)} · saved to Movies/NHIntroStudio", color = NH.Muted, fontSize = 13.sp)
                }
            }
            SectionLabel("Resolution")
            ChoiceChips(Quality.values().toList(), settings.quality, { it.label }, { q -> exportVm.update { it.copy(quality = q) } })
            SectionLabel("Aspect ratio")
            ChoiceChips(AspectRatio.values().toList(), ui.cfg.aspect, { "${it.label} ${it.sub}" }, { a -> editor.setAspect(a); editor.commit() })
            SectionLabel("Frame rate")
            ChoiceChips(listOf(30, 60), settings.fps, { "$it FPS" }, { f -> exportVm.update { it.copy(fps = f) } })
            SectionLabel("Video codec")
            ChoiceChips(Codec.values().toList(), settings.codec, { it.label }, { c -> exportVm.update { it.copy(codec = c) } })
            Spacer(Modifier.height(8.dp))
            SliderRow(
                "Bitrate", settings.bitrateMbps, 0f..80f,
                { v -> exportVm.update { it.copy(bitrateMbps = if (v < 1f) 0f else v.roundToInt().toFloat()) } },
                valueText = if (settings.bitrateMbps < 1f) "Auto (${String.format("%.1f", bitrate / 1_000_000f)} Mbps)" else "${settings.bitrateMbps.roundToInt()} Mbps"
            )
            SectionLabel("Device compatibility")
            val c = compat
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    when {
                        c == null -> Text("Checking what this device can encode…", color = NH.Muted, fontSize = 14.sp)
                        c.ok -> Text("✓ ${c.message}", color = NH.Green, fontSize = 14.sp)
                        else -> {
                            Text("✕ ${c.message}", color = Color(0xFFFF8A9A), fontSize = 14.sp)
                            if (c.suggestion != null) {
                                Spacer(Modifier.height(10.dp))
                                NeonButton("Use supported settings", { exportVm.applySuggestion() }, Modifier.fillMaxWidth(), outlined = true)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        NeonButton("Start rendering", onStart, Modifier.fillMaxWidth().padding(16.dp), Icons.Filled.Movie, enabled = compat?.ok == true && ui.loaded && !ExportManager.isRunning)
    }
}

@Composable
fun RenderingScreen(app: AppViewModel, onHome: () -> Unit, onGallery: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val state by ExportManager.state.collectAsState()
    val videos by app.videos.collectAsState()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var confirmCancel by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { while (true) { delay(500); now = System.currentTimeMillis() } }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Rendering", subtitle = when (state) {
            is ExportState.Running -> (state as ExportState.Running).projectName
            else -> "Export"
        })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when (val s = state) {
                is ExportState.Running -> {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${(s.progress * 100).roundToInt()}%", color = NH.Text, fontSize = 56.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(progress = { s.progress }, modifier = Modifier.fillMaxWidth().height(10.dp), color = NH.Purple, trackColor = Color.White.copy(alpha = 0.12f))
                            Spacer(Modifier.height(14.dp))
                            Text(s.stage, color = NH.Text, fontSize = 15.sp)
                            Text("Elapsed ${Fmt.time((now - s.startedAt) / 1000f)}", color = NH.Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                            Text("Real frames are being rendered and encoded on your device. You can leave the app; the export continues in the background.", color = NH.Muted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    NeonButton("Cancel export", { confirmCancel = true }, Modifier.fillMaxWidth(), Icons.Filled.Close, outlined = true)
                    Spacer(Modifier.height(10.dp))
                    NeonButton("Continue in background", onHome, Modifier.fillMaxWidth(), Icons.Filled.Home, outlined = true)
                }
                is ExportState.Done -> {
                    val v = videos.firstOrNull { it.id == s.videoId }
                    val thumb by produceState<ImageBitmap?>(null, v?.id) { value = v?.let { app.videoThumb(it) } }
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✓ Your intro is ready", color = NH.Green, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            Box(Modifier.fillMaxWidth().heightIn(max = 240.dp).background(Color.Black), contentAlignment = Alignment.Center) {
                                thumb?.let { Image(it, null, Modifier.fillMaxWidth(), contentScale = ContentScale.Fit) }
                            }
                            Spacer(Modifier.height(10.dp))
                            Text("${s.output.width}×${s.output.height} · ${s.output.fps} FPS · ${Fmt.size(s.output.sizeBytes)}", color = NH.Muted, fontSize = 13.sp)
                            Text("Saved to Movies/NHIntroStudio", color = NH.Muted, fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    NeonButton("Share video", { Share.video(context, s.output.uri.toString()) }, Modifier.fillMaxWidth(), Icons.Filled.Share)
                    Spacer(Modifier.height(10.dp))
                    NeonButton("Open video gallery", { ExportManager.reset(); onGallery() }, Modifier.fillMaxWidth(), Icons.Filled.VideoLibrary, outlined = true)
                    Spacer(Modifier.height(10.dp))
                    NeonButton("Back to home", { ExportManager.reset(); onHome() }, Modifier.fillMaxWidth(), Icons.Filled.Home, outlined = true)
                }
                is ExportState.Failed -> {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Export failed", color = Color(0xFFFF8A9A), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(s.message, color = NH.Text, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    NeonButton("Back to export settings", { ExportManager.reset(); onBack() }, Modifier.fillMaxWidth())
                }
                ExportState.Cancelled -> {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Export cancelled", color = NH.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("No video was saved.", color = NH.Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    NeonButton("Back to export settings", { ExportManager.reset(); onBack() }, Modifier.fillMaxWidth())
                }
                ExportState.Idle -> {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp)) {
                            Text("No export in progress", color = NH.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    NeonButton("Back to home", onHome, Modifier.fillMaxWidth(), Icons.Filled.Home)
                }
            }
        }
    }
    if (confirmCancel) ConfirmDialog("Cancel export?", "The partially rendered video will be discarded.", "Cancel export", { ExportManager.cancel() }, { confirmCancel = false })
}
