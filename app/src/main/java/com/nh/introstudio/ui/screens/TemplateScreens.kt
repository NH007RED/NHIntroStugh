package com.nh.introstudio.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nh.introstudio.domain.AspectRatio
import com.nh.introstudio.domain.BrandConfig
import com.nh.introstudio.renderer.IntroRenderer
import com.nh.introstudio.templates.TemplateCatalog
import com.nh.introstudio.ui.components.ChoiceChips
import com.nh.introstudio.ui.components.EmptyState
import com.nh.introstudio.ui.components.FramePreview
import com.nh.introstudio.ui.components.GlassCard
import com.nh.introstudio.ui.components.GlassIconButton
import com.nh.introstudio.ui.components.NeonButton
import com.nh.introstudio.ui.components.ScreenHeader
import com.nh.introstudio.ui.components.TemplateCard
import com.nh.introstudio.ui.theme.NH
import com.nh.introstudio.viewmodel.AppViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

@Composable
fun TemplateGalleryScreen(app: AppViewModel, favoritesOnly: Boolean, onBack: (() -> Unit)?, onOpen: (Int) -> Unit) {
    val favorites by app.favorites.collectAsState()
    var category by remember { mutableStateOf("All") }
    val categories = listOf("All", "Streaming Text", "Logo AI (1–20)") + TemplateCatalog.all.map { it.category }.distinct().filter { it != "Streaming Text" }
    val list = TemplateCatalog.all.filter { spec ->
        val favOk = !favoritesOnly || favorites.contains(spec.id)
        val catOk = when (category) {
            "All" -> true
            "Logo AI (1–20)" -> !spec.isTextIntro
            else -> spec.category == category
        }
        favOk && catOk
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            if (favoritesOnly) "Favorites" else "40 Templates",
            subtitle = if (favoritesOnly) "${favorites.size} saved templates" else "20 Deep Logo AI + 20 Streaming Text Intros",
            onBack = onBack
        )
        ChoiceChips(categories, category, { it }, { category = it }, Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(10.dp))
        if (list.isEmpty()) {
            EmptyState(
                if (favoritesOnly) "No favorites yet" else "Nothing here",
                if (favoritesOnly) "Tap the heart on any template to save it here." else "Try another category."
            )
        } else {
            LazyVerticalGrid(
                GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(list, key = { it.id }) { spec ->
                    TemplateCard(spec, app, favorites.contains(spec.id), { onOpen(spec.id) }, { app.toggleFavorite(spec.id) })
                }
                item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(4.dp)) }
            }
        }
    }
}

/** Looping live preview of a configuration, rendered with the exact renderer used for export. */
@Composable
fun LoopingPreview(cfg: BrandConfig, replayKey: Int, modifier: Modifier = Modifier) {
    val renderer = remember { IntroRenderer() }
    val dispatcher = remember { Executors.newSingleThreadExecutor().asCoroutineDispatcher() }
    DisposableEffect(Unit) {
        onDispose { dispatcher.close(); renderer.release() }
    }
    var frame by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(cfg, replayKey) {
        val ar = cfg.aspect
        val long = 720
        val w = if (ar.rw >= ar.rh) long else long * ar.rw / ar.rh
        val h = if (ar.rw >= ar.rh) long * ar.rh / ar.rw else long
        val bmps = Array(3) { Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888) }
        var i = 0
        val start = withFrameNanos { it }
        while (isActive) {
            val now = withFrameNanos { it }
            val loop = cfg.durationSec + 0.8f
            val t = (((now - start) / 1_000_000_000f) % loop).coerceAtMost(cfg.durationSec)
            val b = bmps[i]
            i = (i + 1) % bmps.size
            withContext(dispatcher) { renderer.render(Canvas(b), w, h, t, cfg, isPreview = true) }
            frame = b.asImageBitmap()
        }
    }
    FramePreview(frame, cfg.aspect, modifier)
}

@Composable
fun TemplatePreviewScreen(templateId: Int, app: AppViewModel, onBack: () -> Unit, onUse: (Int) -> Unit) {
    val spec = TemplateCatalog.byId(templateId)
    val favorites by app.favorites.collectAsState()
    val fav = favorites.contains(templateId)
    val cfg = remember(templateId) { TemplateCatalog.defaultConfig(templateId) }
    var replay by remember { mutableIntStateOf(0) }
    var aspect by remember { mutableStateOf(AspectRatio.R16_9) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("${spec.id}. ${spec.name}", subtitle = spec.category, onBack = onBack) {
            GlassIconButton(if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, { app.toggleFavorite(templateId) }, tint = if (fav) NH.Red else Color.White)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            LoopingPreview(cfg.copy(aspect = aspect), replay, Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                ChoiceChips(AspectRatio.values().toList(), aspect, { it.label }, { aspect = it }, Modifier.weight(1f))
                GlassIconButton(Icons.Filled.Refresh, { replay++ })
            }
            Spacer(Modifier.height(14.dp))
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(spec.description, color = NH.Text, fontSize = 15.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        for (c in listOf(spec.c1, spec.c2, spec.c3)) {
                            Box(Modifier.size(26.dp).clip(CircleShape).background(Color(c)).border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape))
                        }
                        Text("${spec.inOutStyle.label} · ${spec.textStyle.label}", color = NH.Muted, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
            Text("Preview shows sample text. You will add your own brand name, tagline and logo next.", color = NH.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
            Spacer(Modifier.height(16.dp))
        }
        NeonButton("Use this template", { onUse(templateId) }, Modifier.fillMaxWidth().padding(16.dp), Icons.Filled.Tune)
    }
}
