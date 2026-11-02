package com.nh.introstudio.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nh.introstudio.templates.TemplateCatalog
import com.nh.introstudio.ui.components.GlassCard
import com.nh.introstudio.ui.components.GlassIconButton
import com.nh.introstudio.ui.components.NeonButton
import com.nh.introstudio.ui.components.SectionLabel
import com.nh.introstudio.ui.components.TemplateCard
import com.nh.introstudio.ui.theme.NH
import com.nh.introstudio.utils.Fmt
import com.nh.introstudio.viewmodel.AppViewModel

@Composable
fun HomeScreen(
    app: AppViewModel,
    onLogoAi: () -> Unit,
    onTextIntros: () -> Unit,
    onTemplates: () -> Unit,
    onFavorites: () -> Unit,
    onVideos: () -> Unit,
    onSettings: () -> Unit,
    onOpenTemplate: (Int) -> Unit,
    onOpenProject: (Long) -> Unit
) {
    val projects by app.projects.collectAsState()
    val videos by app.videos.collectAsState()
    val favorites by app.favorites.collectAsState()
    val logoTemplates = TemplateCatalog.logoAiTemplates
    val textIntroTemplates = TemplateCatalog.textIntroTemplates

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("NH INTRO STUDIO v4", color = NH.Text, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("20 Deep Logo AI Reveals + 20 Streaming Text Intros", color = NH.Muted, fontSize = 13.sp)
                }
                GlassIconButton(Icons.Filled.Settings, onSettings, size = 40.dp)
            }
        }
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Logo AI & Streaming Text Studio", color = NH.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Create broadcast-grade Logo Animations with unified light-wrap & deep In/Out transitions, OR launch the new Streaming Text Intro Studio (Netflix · Prime · Hotstar · Marvel · HBO · IMAX).",
                        color = NH.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )
                    NeonButton("Logo Animation AI (20 Templates)", onLogoAi, Modifier.fillMaxWidth(), Icons.Filled.AutoAwesome)
                    Spacer(Modifier.height(10.dp))
                    NeonButton("Streaming Text Intros (Netflix · Prime · Hotstar)", onTextIntros, Modifier.fillMaxWidth(), Icons.Filled.Movie)
                    Spacer(Modifier.height(10.dp))
                    NeonButton("Browse All 40 Templates", onTemplates, Modifier.fillMaxWidth(), Icons.Filled.Add, outlined = true)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Templates", TemplateCatalog.all.size.toString(), Modifier.weight(1f), onTemplates)
                StatCard("Projects", projects.size.toString(), Modifier.weight(1f), null)
                StatCard("Videos", videos.size.toString(), Modifier.weight(1f), onVideos)
            }
        }
        if (projects.isNotEmpty()) {
            item { SectionLabel("Continue editing", Modifier.padding(top = 0.dp)) }
            item {
                val p = projects.first()
                val thumb by produceState<ImageBitmap?>(null, p.id, p.updatedAt) { value = app.projectThumb(p) }
                GlassCard(Modifier.fillMaxWidth(), onClick = { onOpenProject(p.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(150.dp).aspectRatio(16f / 9f).background(Color.Black)) {
                            thumb?.let { Image(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit) }
                        }
                        Column(Modifier.padding(14.dp)) {
                            Text(p.name, color = NH.Text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1)
                            Text(TemplateCatalog.byId(p.templateId).name, color = NH.Muted, fontSize = 13.sp)
                            Text(Fmt.date(p.updatedAt), color = NH.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
        item { SectionLabel("Streaming & Cinema Text Intros (21–40)") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(textIntroTemplates, key = { it.id }) { spec ->
                    TemplateCard(spec, app, favorites.contains(spec.id), { onOpenTemplate(spec.id) }, { app.toggleFavorite(spec.id) }, Modifier.width(220.dp))
                }
            }
        }
        item { SectionLabel("Deep Unified Logo AI Templates (1–20)") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(logoTemplates, key = { it.id }) { spec ->
                    TemplateCard(spec, app, favorites.contains(spec.id), { onOpenTemplate(spec.id) }, { app.toggleFavorite(spec.id) }, Modifier.width(220.dp))
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NeonButton("Favorites", onFavorites, Modifier.weight(1f), Icons.Filled.Favorite, outlined = true)
                NeonButton("My videos", onVideos, Modifier.weight(1f), Icons.Filled.VideoLibrary, outlined = true)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier, onClick: (() -> Unit)?) {
    GlassCard(modifier, onClick, corner = 20.dp) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = NH.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(label, color = NH.Muted, fontSize = 12.sp)
        }
    }
}
