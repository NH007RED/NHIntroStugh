package com.nh.introstudio.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.nh.introstudio.database.ProjectEntity
import com.nh.introstudio.database.VideoEntity
import com.nh.introstudio.templates.TemplateCatalog
import com.nh.introstudio.ui.components.ConfirmDialog
import com.nh.introstudio.ui.components.EmptyState
import com.nh.introstudio.ui.components.GlassCard
import com.nh.introstudio.ui.components.GlassIconButton
import com.nh.introstudio.ui.components.NeonButton
import com.nh.introstudio.ui.components.ScreenHeader
import com.nh.introstudio.ui.components.TextInputDialog
import com.nh.introstudio.ui.theme.NH
import com.nh.introstudio.utils.Fmt
import com.nh.introstudio.utils.Share
import com.nh.introstudio.viewmodel.AppViewModel

@Composable
fun ProjectsScreen(app: AppViewModel, onOpen: (Long) -> Unit, onNew: () -> Unit) {
    val projects by app.projects.collectAsState()
    var rename by remember { mutableStateOf<ProjectEntity?>(null) }
    var delete by remember { mutableStateOf<ProjectEntity?>(null) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("My projects", subtitle = "${projects.size} saved") {
            GlassIconButton(Icons.Filled.Add, onNew)
        }
        if (projects.isEmpty()) {
            EmptyState("No projects yet", "Pick a template to create your first intro.") {
                NeonButton("Browse templates", onNew, icon = Icons.Filled.Add)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(projects, key = { it.id }) { p ->
                    ProjectRow(p, app, { onOpen(p.id) }, { rename = p }, { app.duplicateProject(p.id) }, { delete = p })
                }
            }
        }
    }
    rename?.let { p -> TextInputDialog("Rename project", p.name, "Rename", { app.renameProject(p.id, it) }, { rename = null }) }
    delete?.let { p -> ConfirmDialog("Delete project?", "“${p.name}” will be removed. Exported videos are not affected.", "Delete", { app.deleteProject(p.id) }, { delete = null }) }
}

@Composable
private fun ProjectRow(p: ProjectEntity, app: AppViewModel, onOpen: () -> Unit, onRename: () -> Unit, onDuplicate: () -> Unit, onDelete: () -> Unit) {
    val thumb by produceState<ImageBitmap?>(null, p.id, p.updatedAt) { value = app.projectThumb(p) }
    var menu by remember { mutableStateOf(false) }
    GlassCard(Modifier.fillMaxWidth(), onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(130.dp).aspectRatio(16f / 9f).background(Color.Black)) {
                thumb?.let { Image(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit) }
            }
            Column(Modifier.weight(1f).padding(12.dp)) {
                Text(p.name, color = NH.Text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(TemplateCatalog.byId(p.templateId).name, color = NH.Muted, fontSize = 12.sp)
                Text(Fmt.date(p.updatedAt), color = NH.Muted, fontSize = 11.sp)
            }
            Box {
                GlassIconButton(Icons.Filled.MoreVert, { menu = true }, size = 36.dp)
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem(text = { Text("Rename") }, leadingIcon = { androidx.compose.material3.Icon(Icons.Filled.Edit, null) }, onClick = { menu = false; onRename() })
                    DropdownMenuItem(text = { Text("Duplicate") }, leadingIcon = { androidx.compose.material3.Icon(Icons.Filled.ContentCopy, null) }, onClick = { menu = false; onDuplicate() })
                    DropdownMenuItem(text = { Text("Delete") }, leadingIcon = { androidx.compose.material3.Icon(Icons.Filled.Delete, null) }, onClick = { menu = false; onDelete() })
                }
            }
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
fun VideoGalleryScreen(app: AppViewModel, onNew: () -> Unit) {
    val videos by app.videos.collectAsState()
    val context = LocalContext.current
    var play by remember { mutableStateOf<VideoEntity?>(null) }
    var delete by remember { mutableStateOf<VideoEntity?>(null) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Video gallery", subtitle = "${videos.size} exported · Movies/NHIntroStudio")
        if (videos.isEmpty()) {
            EmptyState("No videos yet", "Exported intros appear here.") { NeonButton("Create an intro", onNew, icon = Icons.Filled.Add) }
        } else {
            LazyVerticalGrid(
                GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(videos, key = { it.id }) { v ->
                    val thumb by produceState<ImageBitmap?>(null, v.id) { value = app.videoThumb(v) }
                    GlassCard(Modifier.fillMaxWidth(), onClick = { play = v }) {
                        Column {
                            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Black), contentAlignment = Alignment.Center) {
                                thumb?.let { Image(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit) }
                                Box(Modifier.background(Color.Black.copy(alpha = 0.45f), androidx.compose.foundation.shape.CircleShape).padding(8.dp)) {
                                    androidx.compose.material3.Icon(Icons.Filled.PlayArrow, null, tint = Color.White)
                                }
                            }
                            Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Text(TemplateCatalog.byId(v.templateId).name, color = NH.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${v.width}×${v.height} · ${v.fps} FPS · ${Fmt.size(v.sizeBytes)}", color = NH.Muted, fontSize = 11.sp, maxLines = 1)
                                Text(Fmt.date(v.createdAt), color = NH.Muted, fontSize = 11.sp, maxLines = 1)
                                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.End) {
                                    GlassIconButton(Icons.Filled.Share, { Share.video(context, v.uri) }, size = 34.dp)
                                    Spacer(Modifier.width(8.dp))
                                    GlassIconButton(Icons.Filled.Delete, { delete = v }, size = 34.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    play?.let { v -> VideoPlayerDialog(v.uri) { play = null } }
    delete?.let { v -> ConfirmDialog("Delete video?", "This removes the file from Movies/NHIntroStudio.", "Delete", { app.deleteVideo(v) }, { delete = null }) }
}

@Composable
fun VideoPlayerDialog(uri: String, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(ctx).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(Unit) { onDispose { player.release() } }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(factory = { c -> PlayerView(c).apply { this.player = player } }, modifier = Modifier.fillMaxSize())
            GlassIconButton(Icons.Filled.Close, onDismiss, Modifier.align(Alignment.TopEnd).padding(16.dp))
        }
    }
}
