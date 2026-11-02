package com.nh.introstudio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nh.introstudio.data.AppSettings
import com.nh.introstudio.domain.BrandConfig
import com.nh.introstudio.domain.ExportSettings
import com.nh.introstudio.exporter.ExportCompat
import com.nh.introstudio.exporter.ExportManager
import com.nh.introstudio.exporter.ExportRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExportViewModel(app: Application) : AndroidViewModel(app) {
    val settings = MutableStateFlow(ExportSettings())
    private val _compat = MutableStateFlow<ExportCompat.Result?>(null)
    val compat: StateFlow<ExportCompat.Result?> = _compat
    private var initialised = false

    init {
        viewModelScope.launch {
            settings.collectLatest { s ->
                _compat.value = null
                _compat.value = withContext(Dispatchers.Default) { ExportCompat.check(s) }
            }
        }
    }

    fun initFrom(defaults: AppSettings, aspect: com.nh.introstudio.domain.AspectRatio) {
        if (!initialised) {
            initialised = true
            settings.value = defaults.toExportSettings().copy(aspect = aspect)
        } else {
            settings.value = settings.value.copy(aspect = aspect)
        }
    }

    fun update(block: (ExportSettings) -> ExportSettings) { settings.value = block(settings.value) }

    fun applySuggestion() {
        _compat.value?.suggestion?.let { settings.value = it }
    }

    fun start(projectId: Long, name: String, cfg: BrandConfig) {
        ExportManager.reset()
        ExportManager.start(getApplication(), ExportRequest(projectId, name, cfg, settings.value.copy(aspect = cfg.aspect)))
    }
}
