package com.nh.introstudio.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nh.introstudio.domain.AspectRatio
import com.nh.introstudio.domain.Codec
import com.nh.introstudio.domain.ExportSettings
import com.nh.introstudio.domain.Quality
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "nh_settings")

data class AppSettings(
    val quality: Quality = Quality.FHD,
    val aspect: AspectRatio = AspectRatio.R16_9,
    val fps: Int = 30,
    val codec: Codec = Codec.H264,
    val previewQuality: Int = 1 // 0 low, 1 medium, 2 high
) {
    fun toExportSettings() = ExportSettings(quality = quality, aspect = aspect, fps = fps, codec = codec)
}

class SettingsStore(private val context: Context) {
    private val kQuality = stringPreferencesKey("quality")
    private val kAspect = stringPreferencesKey("aspect")
    private val kFps = intPreferencesKey("fps")
    private val kCodec = stringPreferencesKey("codec")
    private val kPreview = intPreferencesKey("preview_quality")

    val flow: Flow<AppSettings> = context.dataStore.data.map { p -> read(p) }

    private fun read(p: Preferences): AppSettings {
        val d = AppSettings()
        return AppSettings(
            quality = runCatching { Quality.valueOf(p[kQuality] ?: d.quality.name) }.getOrDefault(d.quality),
            aspect = runCatching { AspectRatio.valueOf(p[kAspect] ?: d.aspect.name) }.getOrDefault(d.aspect),
            fps = p[kFps] ?: d.fps,
            codec = runCatching { Codec.valueOf(p[kCodec] ?: d.codec.name) }.getOrDefault(d.codec),
            previewQuality = p[kPreview] ?: d.previewQuality
        )
    }

    suspend fun update(s: AppSettings) {
        context.dataStore.edit {
            it[kQuality] = s.quality.name
            it[kAspect] = s.aspect.name
            it[kFps] = s.fps
            it[kCodec] = s.codec.name
            it[kPreview] = s.previewQuality
        }
    }

    suspend fun reset() {
        context.dataStore.edit { it.clear() }
    }
}
