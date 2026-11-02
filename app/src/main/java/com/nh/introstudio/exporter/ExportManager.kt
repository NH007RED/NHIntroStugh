package com.nh.introstudio.exporter

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.nh.introstudio.domain.BrandConfig
import com.nh.introstudio.domain.ExportSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ExportRequest(val projectId: Long, val projectName: String, val cfg: BrandConfig, val settings: ExportSettings)

sealed interface ExportState {
    data object Idle : ExportState
    data class Running(val progress: Float, val stage: String, val projectName: String, val startedAt: Long) : ExportState
    data class Done(val output: ExportOutput, val videoId: Long) : ExportState
    data class Failed(val message: String) : ExportState
    data object Cancelled : ExportState
}

/** Process wide export status shared by the foreground service and the UI. */
object ExportManager {
    private val _state = MutableStateFlow<ExportState>(ExportState.Idle)
    val state: StateFlow<ExportState> get() = _state

    @Volatile var pending: ExportRequest? = null
    @Volatile var job: Job? = null

    val isRunning: Boolean get() = _state.value is ExportState.Running

    fun start(context: Context, req: ExportRequest) {
        if (isRunning) return
        pending = req
        _state.value = ExportState.Running(0f, "Preparing", req.projectName, System.currentTimeMillis())
        ContextCompat.startForegroundService(context, Intent(context, ExportService::class.java))
    }

    fun cancel() {
        val j = job
        if (j != null) j.cancel() else if (isRunning) { pending = null; _state.value = ExportState.Cancelled }
    }

    fun reset() {
        if (!isRunning) _state.value = ExportState.Idle
    }

    internal fun update(progress: Float, stage: String) {
        val s = _state.value
        if (s is ExportState.Running) _state.value = s.copy(progress = progress, stage = stage)
    }

    internal fun finish(output: ExportOutput, videoId: Long) { _state.value = ExportState.Done(output, videoId) }
    internal fun fail(message: String) { _state.value = ExportState.Failed(message) }
    internal fun cancelled() { _state.value = ExportState.Cancelled }
}
