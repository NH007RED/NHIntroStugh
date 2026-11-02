package com.nh.introstudio.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object Share {
    fun video(context: Context, uriString: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(uriString))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share intro video").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            Toast.makeText(context, "No app available to share this video.", Toast.LENGTH_SHORT).show()
        }
    }
}
