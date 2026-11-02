package com.nh.introstudio.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nh.introstudio.ui.components.AnimationPanel
import com.nh.introstudio.ui.components.AudioPanel
import com.nh.introstudio.ui.components.BackgroundPanel
import com.nh.introstudio.ui.components.ChoiceChips
import com.nh.introstudio.ui.components.ColorsPanel
import com.nh.introstudio.ui.components.ConfirmDialog
import com.nh.introstudio.ui.components.FramePreview
import com.nh.introstudio.ui.components.GlassCard
import com.nh.introstudio.ui.components.GlassIconButton
import com.nh.introstudio.ui.components.LogoPanel
import com.nh.introstudio.ui.components.NeonButton
import com.nh.introstudio.ui.components.ScreenHeader
import com.nh.introstudio.ui.components.TextInputDialog
import com.nh.introstudio.ui.components.TextPanel
import com.nh.introstudio.ui.theme.NH
import com.nh.introstudio.utils.Fmt
import com.nh.introstudio.viewmodel.EditorViewModel

@Composable
private fun ProjectLifecycle(editor: EditorViewModel, projectId: Long) {
    val context = LocalContext.current
    LaunchedEffect(projectId) { editor.load(projectId) }
    LaunchedEffect(Unit) { editor.messages.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
    DisposableEffect(projectId) {
        onDispose { editor.pause(); editor.flush() }
    }
}

@Composable
fun PreviewWithControls(editor: EditorViewModel, showFrameStep: Boolean, maxPreviewHeight: Int = 270) {
    val ui by editor.ui.collectAsState()
    val frame by editor.frame.collectAsState()
    val playing by editor.playing.collectAsState()
    val pos by editor.position.collectAsState()
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Box(Modifier.fillMaxWidth().heightIn(max = maxPreviewHeight.dp), contentAlignment = Alignment.Center) {
            FramePreview(frame, ui.cfg.aspect)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (showFrameStep) {
                GlassIconButton(Icons.Filled.SkipPrevious, { editor.stepFrame(-1) })
                Spacer(Modifier.width(10.dp))
            }
            GlassIconButton(if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow, { editor.togglePlay() }, size = 52.dp)
            if (showFrameStep) {
                Spacer(Modifier.width(10.dp))
                GlassIconButton(Icons.Filled.SkipNext, { editor.stepFrame(1) })
            }
            Spacer(Modifier.width(14.dp))
            Text("${Fmt.time(pos)} / ${Fmt.time(ui.cfg.durationSec)}", color = NH.Muted, fontSize = 13.sp)
        }
    }
}

@Composable
fun CustomizeScreen(editor: EditorViewModel, projectId: Long, onBack: () -> Unit, onEditor: () -> Unit) {
    ProjectLifecycle(editor, projectId)
    val ui by editor.ui.collectAsState()
    val tabs = listOf("Text", "Logo", "Background", "Colors")
    var tab by remember { mutableStateOf("Text") }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Brand customization", subtitle = ui.name, onBack = onBack)
        if (!ui.loaded) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("Loading project…", color = NH.Muted) }
        } else {
            PreviewWithControls(editor, showFrameStep = false)
            Spacer(Modifier.height(8.dp))
            ChoiceChips(tabs, tab, { it }, { tab = it }, Modifier.padding(horizontal = 16.dp))
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)) {
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        when (tab) {
                            "Text" -> TextPanel(editor, ui.cfg)
                            "Logo" -> LogoPanel(editor, ui.cfg)
                            "Background" -> BackgroundPanel(editor, ui.cfg)
                            else -> ColorsPanel(editor, ui.cfg)
                        }
                    }
                }
            }
            NeonButton("Open video editor", onEditor, Modifier.fillMaxWidth().padding(16.dp), Icons.Filled.Movie)
        }
    }
}

@Composable
fun EditorScreen(editor: EditorViewModel, projectId: Long, onBack: () -> Unit, onExport: () -> Unit) {
    ProjectLifecycle(editor, projectId)
    val ui by editor.ui.collectAsState()
    val pos by editor.position.collectAsState()
    val thumbs by editor.thumbs.collectAsState()
    val tabs = listOf("Text", "Logo", "Animation", "Audio", "Colors", "Background")
    var tab by remember { mutableStateOf("Text") }
    var showRename by remember { mutableStateOf(false) }
    var showReset by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Video editor", subtitle = ui.name, onBack = onBack) {
            GlassIconButton(Icons.Filled.Edit, { showRename = true }, size = 38.dp)
            Spacer(Modifier.width(6.dp))
            GlassIconButton(Icons.Filled.Undo, { editor.undo() }, enabled = ui.canUndo, size = 38.dp)
            Spacer(Modifier.width(6.dp))
            GlassIconButton(Icons.Filled.Redo, { editor.redo() }, enabled = ui.canRedo, size = 38.dp)
            Spacer(Modifier.width(6.dp))
            GlassIconButton(Icons.Filled.RestartAlt, { showReset = true }, size = 38.dp)
        }
        if (!ui.loaded) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("Loading project…", color = NH.Muted) }
        } else {
            PreviewWithControls(editor, showFrameStep = true, maxPreviewHeight = 230)
            Spacer(Modifier.height(6.dp))
            // Timeline: frame strip + scrubber
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                val shape = RoundedCornerShape(12.dp)
                Row(Modifier.fillMaxWidth().height(44.dp).clip(shape).background(Color.Black).border(1.dp, Color.White.copy(alpha = 0.2f), shape)) {
                    if (thumbs.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Building timeline…", color = NH.Muted, fontSize = 11.sp) }
                    } else {
                        for ((i, t) in thumbs.withIndex()) {
                            Image(t, null, Modifier.weight(1f).fillMaxSize().clickable { editor.seek(ui.cfg.durationSec * i / 7f * 0.97f) }, contentScale = ContentScale.Crop)
                        }
                    }
                }
                Slider(
                    value = pos.coerceIn(0f, ui.cfg.durationSec),
                    onValueChange = { editor.seek(it) },
                    valueRange = 0f..ui.cfg.durationSec,
                    colors = SliderDefaults.colors(thumbColor = NH.Red, activeTrackColor = NH.Red, inactiveTrackColor = Color.White.copy(alpha = 0.15f))
                )
            }
            ChoiceChips(tabs, tab, { it }, { tab = it }, Modifier.padding(horizontal = 16.dp))
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)) {
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        when (tab) {
                            "Text" -> TextPanel(editor, ui.cfg)
                            "Logo" -> LogoPanel(editor, ui.cfg)
                            "Animation" -> AnimationPanel(editor, ui.cfg)
                            "Audio" -> AudioPanel(editor, ui.cfg)
                            "Colors" -> ColorsPanel(editor, ui.cfg)
                            else -> BackgroundPanel(editor, ui.cfg)
                        }
                    }
                }
            }
            NeonButton("Export video", { editor.pause(); onExport() }, Modifier.fillMaxWidth().padding(16.dp), Icons.Filled.Movie)
        }
    }
    if (showRename) TextInputDialog("Rename project", ui.name, "Rename", { editor.rename(it) }, { showRename = false })
    if (showReset) ConfirmDialog("Reset project?", "All brand, layout, animation and audio settings return to the template defaults. You can undo this.", "Reset", { editor.resetProject() }, { showReset = false })
}
