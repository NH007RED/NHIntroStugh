package com.nh.introstudio.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Copies user selected media into app private storage so projects keep working offline and after the picker permission expires. */
class MediaImporter(private val context: Context) {

    data class Imported(val path: String, val displayName: String)

    private fun displayName(uri: Uri): String {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            } ?: "Imported file"
        } catch (e: Exception) {
            "Imported file"
        }
    }

    private suspend fun copy(uri: Uri, folder: String, fallbackExt: String): Imported? = withContext(Dispatchers.IO) {
        try {
            val name = displayName(uri)
            val ext = name.substringAfterLast('.', fallbackExt).lowercase().take(5).ifBlank { fallbackExt }
            val dir = File(context.filesDir, "media/$folder").apply { mkdirs() }
            val out = File(dir, "${System.currentTimeMillis()}.$ext")
            context.contentResolver.openInputStream(uri)?.use { input ->
                out.outputStream().use { input.copyTo(it) }
            } ?: return@withContext null
            if (out.length() == 0L) { out.delete(); null } else Imported(out.absolutePath, name)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun importLogo(uri: Uri) = copy(uri, "logos", "png")
    suspend fun importBackgroundImage(uri: Uri) = copy(uri, "backgrounds", "jpg")
    suspend fun importBackgroundVideo(uri: Uri) = copy(uri, "videos", "mp4")
    suspend fun importAudio(uri: Uri) = copy(uri, "audio", "mp3")
}
