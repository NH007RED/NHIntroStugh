package com.nh.introstudio.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nh.introstudio.NHApp
import com.nh.introstudio.data.AppSettings
import com.nh.introstudio.database.ProjectEntity
import com.nh.introstudio.database.VideoEntity
import com.nh.introstudio.domain.BrandConfig
import com.nh.introstudio.domain.LogoAiAnalyzer
import com.nh.introstudio.domain.LogoStyle
import com.nh.introstudio.renderer.IntroRenderer
import com.nh.introstudio.templates.TemplateCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/** Library state: projects, favorites, exported videos, settings and cached thumbnails. */
class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val a = app as NHApp
    private val repo = a.repository

    val projects: StateFlow<List<ProjectEntity>> = repo.projects.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val favorites: StateFlow<Set<Int>> = repo.favorites.map { it.toSet() }.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())
    val videos: StateFlow<List<VideoEntity>> = repo.videos.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val settings: StateFlow<AppSettings> = a.settingsStore.flow.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun toggleFavorite(templateId: Int) {
        val isFav = favorites.value.contains(templateId)
        viewModelScope.launch { repo.setFavorite(templateId, !isFav) }
    }

    fun createProject(templateId: Int, onCreated: (Long) -> Unit) {
        viewModelScope.launch { onCreated(repo.createProject(templateId)) }
    }

    fun createLogoAiProject(name: String, cfg: BrandConfig, onCreated: (Long) -> Unit) {
        viewModelScope.launch { onCreated(repo.createProjectWithConfig(name, cfg)) }
    }

    fun importLogoStandalone(uri: Uri, onImported: (String, BrandConfig) -> Unit, currentCfg: BrandConfig) {
        viewModelScope.launch {
            val imported = a.importer.importLogo(uri) ?: return@launch
            val bmp = withContext(Dispatchers.IO) {
                runCatching { BitmapFactory.decodeFile(imported.path) }.getOrNull()
            }
            val analysis = withContext(Dispatchers.Default) { LogoAiAnalyzer.analyzeBitmap(bmp) }
            val spec = TemplateCatalog.byId(currentCfg.templateId)
            val updated = currentCfg.copy(
                logoPath = imported.path,
                showLogo = true,
                removeLogoBg = !analysis.hasTransparency,
                useCustomColors = true,
                colorPrimary = analysis.primaryColor,
                colorSecondary = analysis.secondaryColor,
                colorText = if (spec.isLightPaper) analysis.darkTextColor else spec.textColor
            )
            onImported(imported.displayName, updated)
        }
    }

    fun renameProject(id: Long, name: String) { viewModelScope.launch { repo.renameProject(id, name) } }
    fun duplicateProject(id: Long) { viewModelScope.launch { repo.duplicateProject(id) } }
    fun deleteProject(id: Long) { viewModelScope.launch { repo.deleteProject(id) } }

    fun deleteVideo(v: VideoEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try { a.contentResolver.delete(Uri.parse(v.uri), null, null) } catch (_: Exception) {}
            }
            repo.deleteVideo(v.id)
        }
    }

    fun updateSettings(s: AppSettings) { viewModelScope.launch { a.settingsStore.update(s) } }
    fun resetSettings() { viewModelScope.launch { a.settingsStore.reset() } }

    // ---- thumbnails -------------------------------------------------------------------------------------------------
    private val thumbDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val renderer = IntroRenderer()
    private val thumbCache = ConcurrentHashMap<String, ImageBitmap>()

    private fun render(cfg: BrandConfig, longSide: Int, t: Float): ImageBitmap {
        val ar = cfg.aspect
        val w: Int
        val h: Int
        if (ar.rw >= ar.rh) { w = longSide; h = (longSide * ar.rh / ar.rw) } else { h = longSide; w = (longSide * ar.rw / ar.rh) }
        val bmp = Bitmap.createBitmap(w.coerceAtLeast(2), h.coerceAtLeast(2), Bitmap.Config.ARGB_8888)
        renderer.render(Canvas(bmp), bmp.width, bmp.height, t, cfg, isPreview = true)
        return bmp.asImageBitmap()
    }

    private fun thumbTimeFor(cfg: BrandConfig): Float {
        val spec = TemplateCatalog.byId(cfg.templateId)
        val style = cfg.logoStyleOverride ?: spec.logoStyle
        return when {
            spec.isTextIntro -> 1.65f / cfg.speed
            style == LogoStyle.MASCOT_VORTEX_MORPH -> minOf(7.0f / cfg.speed, cfg.durationSec * 0.82f)
            else -> minOf((spec.revealAt + 1.35f) / cfg.speed, cfg.durationSec * 0.65f)
        }
    }

    suspend fun templateThumb(id: Int): ImageBitmap {
        val key = "t_v4_$id"
        thumbCache[key]?.let { return it }
        return withContext(thumbDispatcher) {
            val cfg = TemplateCatalog.defaultConfig(id)
            render(cfg, 480, thumbTimeFor(cfg))
        }.also { thumbCache[key] = it }
    }

    suspend fun projectThumb(p: ProjectEntity): ImageBitmap {
        val key = "p_v4_${p.id}_${p.updatedAt}"
        thumbCache[key]?.let { return it }
        return withContext(thumbDispatcher) {
            val cfg = BrandConfig.fromJson(p.configJson)
            render(cfg, 480, thumbTimeFor(cfg))
        }.also { thumbCache[key] = it }
    }

    suspend fun videoThumb(v: VideoEntity): ImageBitmap? {
        val key = "v${v.id}"
        thumbCache[key]?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                val r = MediaMetadataRetriever()
                try {
                    r.setDataSource(a, Uri.parse(v.uri))
                    r.getFrameAtTime(2_000_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)?.let { src ->
                        val s = 480f / maxOf(src.width, src.height)
                        val b = if (s < 1f) Bitmap.createScaledBitmap(src, (src.width * s).toInt(), (src.height * s).toInt(), true) else src
                        b.asImageBitmap()
                    }
                } finally {
                    r.release()
                }
            } catch (e: Exception) {
                null
            }
        }?.also { thumbCache[key] = it }
    }

    override fun onCleared() {
        renderer.release()
        thumbDispatcher.close()
    }
}
