package com.nh.introstudio.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.SystemClock
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nh.introstudio.NHApp
import com.nh.introstudio.audio.AudioDecoder
import com.nh.introstudio.audio.AudioMixer
import com.nh.introstudio.audio.PreviewAudio
import com.nh.introstudio.domain.AspectRatio
import com.nh.introstudio.domain.BUILTIN_MUSIC
import com.nh.introstudio.domain.BrandConfig
import com.nh.introstudio.domain.LogoAiAnalyzer
import com.nh.introstudio.renderer.IntroRenderer
import com.nh.introstudio.templates.TemplateCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

data class EditorUi(
    val projectId: Long = 0,
    val name: String = "",
    val cfg: BrandConfig = BrandConfig(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val loaded: Boolean = false
)

/** Holds the project being edited: live preview rendering, playback, undo/redo, auto-save, media import. */
class EditorViewModel(app: Application) : AndroidViewModel(app) {
    private val a = app as NHApp

    private val _ui = MutableStateFlow(EditorUi())
    val ui: StateFlow<EditorUi> = _ui.asStateFlow()
    val frame = MutableStateFlow<ImageBitmap?>(null)
    val position = MutableStateFlow(0f)
    val playing = MutableStateFlow(false)
    val thumbs = MutableStateFlow<List<ImageBitmap>>(emptyList())
    val musicDuration = MutableStateFlow(0f)
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val undoStack = ArrayDeque<BrandConfig>()
    private val redoStack = ArrayDeque<BrandConfig>()
    private var lastTag: String? = null
    private var lastTime = 0L

    private val renderDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val renderer = IntroRenderer()
    private val bitmaps = arrayOfNulls<Bitmap>(3)
    private var nextBmp = 0
    private val previewAudio = PreviewAudio()
    private var previewLong = 720

    private var renderJob: Job? = null
    private var playJob: Job? = null
    private var audioJob: Job? = null
    private var thumbJob: Job? = null
    private var saveJob: Job? = null
    private var audioKey = ""

    init {
        viewModelScope.launch {
            a.settingsStore.flow.collect {
                val v = when (it.previewQuality) { 0 -> 480; 2 -> 1080; else -> 720 }
                if (v != previewLong) { previewLong = v; requestRender() }
            }
        }
    }

    fun load(id: Long) {
        if (_ui.value.loaded && _ui.value.projectId == id) return
        viewModelScope.launch {
            val p = a.repository.getProject(id) ?: return@launch
            pause()
            undoStack.clear(); redoStack.clear(); lastTag = null
            val cfg = BrandConfig.fromJson(p.configJson)
            _ui.value = EditorUi(id, p.name, cfg, canUndo = false, canRedo = false, loaded = true)
            position.value = 0f
            audioKey = ""
            thumbs.value = emptyList()
            requestRender(); scheduleAudio(); scheduleThumbs(); refreshMusicDuration(cfg)
        }
    }

    // ---- editing --------------------------------------------------------------------------------------------------
    fun edit(tag: String, block: (BrandConfig) -> BrandConfig) {
        val cur = _ui.value.cfg
        val next = block(cur)
        if (next == cur) return
        val now = SystemClock.elapsedRealtime()
        if (tag != lastTag || now - lastTime > 1200 || undoStack.isEmpty()) {
            undoStack.addLast(cur)
            if (undoStack.size > 100) undoStack.removeFirst()
        }
        lastTag = tag
        lastTime = now
        redoStack.clear()
        apply(next, cur)
    }

    /** Call when a slider gesture ends so the next change becomes a new undo step. */
    fun commit() { lastTag = null }

    fun undo() {
        val prev = undoStack.removeLastOrNull() ?: return
        val cur = _ui.value.cfg
        redoStack.addLast(cur)
        lastTag = null
        apply(prev, cur)
    }

    fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        val cur = _ui.value.cfg
        undoStack.addLast(cur)
        lastTag = null
        apply(next, cur)
    }

    fun resetProject() {
        val cur = _ui.value.cfg
        val def = TemplateCatalog.defaultConfig(cur.templateId).copy(aspect = cur.aspect)
        undoStack.addLast(cur); redoStack.clear(); lastTag = null
        apply(def, cur)
    }

    fun rename(name: String) {
        if (name.isBlank()) return
        _ui.update { it.copy(name = name.trim()) }
        scheduleSave()
    }

    /** AI Motion Prompt matcher inside the editor. */
    fun applyAiMotionPrompt(prompt: String) {
        viewModelScope.launch {
            val cur = _ui.value.cfg
            val bmp = withContext(Dispatchers.IO) {
                cur.logoPath?.let { runCatching { BitmapFactory.decodeFile(it) }.getOrNull() }
            }
            val designed = withContext(Dispatchers.Default) {
                LogoAiAnalyzer.designFromPrompt(prompt, cur, bmp)
            }
            edit("aiPrompt") { designed }
            commit()
            val spec = TemplateCatalog.byId(designed.templateId)
            val styleName = (designed.logoStyleOverride ?: spec.logoStyle).label
            _messages.tryEmit("AI Motion applied: ${spec.name} ($styleName)")
        }
    }

    /** Extract brand colors from the currently imported logo. */
    fun extractColorsFromLogo() {
        viewModelScope.launch {
            val path = _ui.value.cfg.logoPath ?: run {
                _messages.tryEmit("Import a logo first to extract brand colors.")
                return@launch
            }
            val bmp = withContext(Dispatchers.IO) { runCatching { BitmapFactory.decodeFile(path) }.getOrNull() }
            val analysis = withContext(Dispatchers.Default) { LogoAiAnalyzer.analyzeBitmap(bmp) }
            edit("logoPalette") {
                it.copy(
                    useCustomColors = true,
                    colorPrimary = analysis.primaryColor,
                    colorSecondary = analysis.secondaryColor
                )
            }
            commit()
            _messages.tryEmit("Extracted brand palette from logo.")
        }
    }

    private fun apply(cfg: BrandConfig, old: BrandConfig) {
        _ui.update { it.copy(cfg = cfg, canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty()) }
        if (position.value > cfg.durationSec) position.value = cfg.durationSec
        requestRender()
        scheduleSave()
        scheduleAudio()
        scheduleThumbs()
        if (cfg.musicSource != old.musicSource) refreshMusicDuration(cfg)
    }

    // ---- media import ---------------------------------------------------------------------------------------------
    fun importLogo(uri: Uri) = importMedia("logo") {
        a.importer.importLogo(uri)?.let { r ->
            val bmp = withContext(Dispatchers.IO) { runCatching { BitmapFactory.decodeFile(r.path) }.getOrNull() }
            val analysis = withContext(Dispatchers.Default) { LogoAiAnalyzer.analyzeBitmap(bmp) }
            edit("logo") {
                it.copy(
                    logoPath = r.path,
                    showLogo = true,
                    removeLogoBg = !analysis.hasTransparency
                )
            }
            commit()
        }
    }

    fun importBackgroundImage(uri: Uri) = importMedia("image") { a.importer.importBackgroundImage(uri)?.let { r -> edit("bg") { it.copy(bgImagePath = r.path, bgVideoPath = null) } } }
    fun importBackgroundVideo(uri: Uri) = importMedia("video") { a.importer.importBackgroundVideo(uri)?.let { r -> edit("bg") { it.copy(bgVideoPath = r.path, bgImagePath = null) } } }
    fun importAudio(uri: Uri) = importMedia("audio") {
        a.importer.importAudio(uri)?.let { r ->
            val d = AudioDecoder.durationSec(a, "file:${r.path}")
            if (d <= 0f) _messages.tryEmit("That audio file could not be read. Try MP3, AAC, M4A, WAV or OGG.")
            else edit("music") { it.copy(musicSource = "file:${r.path}", musicName = r.displayName, musicStartSec = 0f, musicEndSec = 0f) }
        }
    }

    private fun importMedia(kind: String, block: suspend () -> Any?) {
        viewModelScope.launch {
            val before = _ui.value.cfg
            block()
            if (_ui.value.cfg == before && kind != "audio") _messages.tryEmit("Could not import that $kind file.")
        }
    }

    fun setBuiltinMusic(id: String) {
        val t = BUILTIN_MUSIC.firstOrNull { it.id == id } ?: return
        edit("music") { it.copy(musicSource = "builtin:$id", musicName = t.title, musicStartSec = 0f, musicEndSec = 0f) }
    }

    fun clearMusic() = edit("music") { it.copy(musicSource = "none", musicName = "No music") }
    fun setAspect(ar: AspectRatio) = edit("aspect") { it.copy(aspect = ar) }

    private fun refreshMusicDuration(cfg: BrandConfig) {
        viewModelScope.launch {
            musicDuration.value = if (cfg.musicSource == "none") 0f
            else withContext(Dispatchers.IO) { AudioDecoder.durationSec(a, cfg.musicSource) }
        }
    }

    // ---- preview rendering & playback -------------------------------------------------------------------------------
    private fun previewDims(cfg: BrandConfig): Pair<Int, Int> {
        val ar = cfg.aspect
        val w: Int
        val h: Int
        if (ar.rw >= ar.rh) { w = previewLong; h = previewLong * ar.rh / ar.rw } else { h = previewLong; w = previewLong * ar.rw / ar.rh }
        return w.coerceAtLeast(2) to h.coerceAtLeast(2)
    }

    private fun requestRender() {
        renderJob?.cancel()
        renderJob = viewModelScope.launch { renderNow(position.value) }
    }

    private suspend fun renderNow(t: Float) {
        val cfg = _ui.value.cfg
        val (w, h) = previewDims(cfg)
        frame.value = withContext(renderDispatcher) {
            var b = bitmaps[nextBmp]
            if (b == null || b.width != w || b.height != h) {
                b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                bitmaps[nextBmp] = b
            }
            renderer.render(Canvas(b), w, h, t, cfg, isPreview = true)
            nextBmp = (nextBmp + 1) % bitmaps.size
            b.asImageBitmap()
        }
    }

    fun play() {
        if (playing.value) return
        val d = _ui.value.cfg.durationSec
        if (position.value >= d - 0.02f) position.value = 0f
        val startPos = position.value
        val startNs = System.nanoTime()
        playing.value = true
        previewAudio.play(startPos)
        playJob?.cancel()
        playJob = viewModelScope.launch {
            while (isActive) {
                val t = startPos + (System.nanoTime() - startNs) / 1_000_000_000f
                val dur = _ui.value.cfg.durationSec
                if (t >= dur) {
                    position.value = dur
                    renderNow(dur)
                    break
                }
                position.value = t
                val s = SystemClock.elapsedRealtime()
                renderNow(t)
                delay((33L - (SystemClock.elapsedRealtime() - s)).coerceAtLeast(1L))
            }
            playing.value = false
            previewAudio.stop()
        }
    }

    fun pause() {
        playJob?.cancel()
        if (playing.value) previewAudio.pause()
        playing.value = false
    }

    fun togglePlay() { if (playing.value) pause() else play() }

    fun seek(t: Float) {
        if (playing.value) pause()
        position.value = t.coerceIn(0f, _ui.value.cfg.durationSec)
        requestRender()
    }

    fun stepFrame(dir: Int) = seek(position.value + dir / 30f)

    // ---- background work ---------------------------------------------------------------------------------------------
    private fun scheduleAudio() {
        val c = _ui.value.cfg
        val key = "${c.musicSource}|${c.musicVolume}|${c.musicStartSec}|${c.musicEndSec}|${c.sfxEnabled}|${c.sfxVolume}|${c.templateId}|${c.speed}|${c.durationSec}"
        if (key == audioKey) return
        audioKey = key
        audioJob?.cancel()
        audioJob = viewModelScope.launch {
            delay(450)
            val pcm = withContext(Dispatchers.Default) { AudioMixer.mix(a, c) }
            val wasPlaying = playing.value
            previewAudio.setPcm(pcm)
            if (wasPlaying) previewAudio.play(position.value)
        }
    }

    private fun scheduleThumbs() {
        thumbJob?.cancel()
        thumbJob = viewModelScope.launch {
            delay(700)
            val cfg = _ui.value.cfg
            val list = withContext(renderDispatcher) {
                val ar = cfg.aspect
                val long = 200
                val w = if (ar.rw >= ar.rh) long else long * ar.rw / ar.rh
                val h = if (ar.rw >= ar.rh) long * ar.rh / ar.rw else long
                (0 until 8).map { i ->
                    val bmp = Bitmap.createBitmap(w.coerceAtLeast(2), h.coerceAtLeast(2), Bitmap.Config.ARGB_8888)
                    renderer.render(Canvas(bmp), bmp.width, bmp.height, cfg.durationSec * i / 7f * 0.97f, cfg, isPreview = true)
                    bmp.asImageBitmap()
                }
            }
            thumbs.value = list
        }
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(800)
            saveNow()
        }
    }

    suspend fun saveNow() {
        val s = _ui.value
        if (s.loaded && s.projectId != 0L) a.repository.saveProject(s.projectId, s.name, s.cfg)
    }

    /** Persist immediately (used when leaving the editor). */
    fun flush() {
        saveJob?.cancel()
        a.appScope.launch { saveNow() }
    }

    override fun onCleared() {
        playJob?.cancel()
        previewAudio.release()
        renderer.release()
        renderDispatcher.close()
    }
}
