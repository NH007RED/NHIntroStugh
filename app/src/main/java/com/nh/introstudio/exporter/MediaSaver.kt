package com.nh.introstudio.exporter

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

/** Saves finished videos to Movies/NHIntroStudio (MediaStore on Android 10+). */
object MediaSaver {

    class Saved(val uri: Uri, val sizeBytes: Long)

    fun save(context: Context, src: File, displayName: String): Saved {
        val size = src.length()
        if (Build.VERSION.SDK_INT >= 29) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/NHIntroStudio")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw ExportException("Could not create the video in Movies/NHIntroStudio.")
            try {
                resolver.openOutputStream(uri)?.use { out -> src.inputStream().use { it.copyTo(out) } }
                    ?: throw ExportException("Could not write the video file.")
                val done = ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }
                resolver.update(uri, done, null, null)
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw if (e is ExportException) e else ExportException("Saving failed: ${e.message}")
            }
            return Saved(uri, size)
        }
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "NHIntroStudio").apply { mkdirs() }
            val dest = File(dir, displayName)
            src.copyTo(dest, overwrite = true)
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Video.Media.TITLE, displayName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                @Suppress("DEPRECATION")
                put(MediaStore.Video.Media.DATA, dest.absolutePath)
                put(MediaStore.Video.Media.SIZE, dest.length())
            }
            val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                ?: FileProvider.getUriForFile(context, context.packageName + ".fileprovider", dest)
            return Saved(uri, size)
        }
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
        val dest = File(dir, displayName)
        src.copyTo(dest, overwrite = true)
        return Saved(FileProvider.getUriForFile(context, context.packageName + ".fileprovider", dest), size)
    }
}
