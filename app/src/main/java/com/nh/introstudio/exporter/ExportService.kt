package com.nh.introstudio.exporter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.nh.introstudio.MainActivity
import com.nh.introstudio.NHApp
import com.nh.introstudio.R
import com.nh.introstudio.database.VideoEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/** Foreground service so long renders keep running when the app is in the background. */
class ExportService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val req = ExportManager.pending
        if (req == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        ensureChannel()
        val first = notification("Rendering ${req.projectName}", 0f, true)
        if (Build.VERSION.SDK_INT >= 29) startForeground(NOTIF_ID, first, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        else startForeground(NOTIF_ID, first)

        val wl = (getSystemService(POWER_SERVICE) as PowerManager).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "nhintro:export")
        wl.acquire(45 * 60 * 1000L)
        val app = application as NHApp
        val dispatcher = Executors.newSingleThreadExecutor { r -> Thread(null, r, "nh-export", 0L) }.asCoroutineDispatcher()
        ExportManager.job = app.appScope.launch(dispatcher) {
            try {
                val self = coroutineContext[Job]!!
                val out = VideoExporter(app).run(req.projectName, req.cfg, req.settings, { self.isActive }) { p, stage ->
                    ExportManager.update(p, stage)
                    notify(notification("${req.projectName}: $stage", p, true))
                }
                val id = app.repository.addVideo(
                    VideoEntity(
                        name = out.displayName, uri = out.uri.toString(), width = out.width, height = out.height,
                        fps = out.fps, durationSec = out.durationSec, sizeBytes = out.sizeBytes,
                        templateId = req.cfg.templateId, createdAt = System.currentTimeMillis()
                    )
                )
                ExportManager.finish(out, id)
                notify(notification("Export complete: ${out.displayName}", 1f, false))
            } catch (e: ExportCancelled) {
                ExportManager.cancelled()
            } catch (e: CancellationException) {
                ExportManager.cancelled()
            } catch (e: ExportException) {
                ExportManager.fail(e.message ?: "Export failed.")
                notify(notification("Export failed", 0f, false))
            } catch (e: OutOfMemoryError) {
                ExportManager.fail("The device ran out of memory. Choose a lower resolution and try again.")
            } catch (e: Throwable) {
                ExportManager.fail("Rendering failed: ${e.message ?: e.javaClass.simpleName}")
            } finally {
                ExportManager.pending = null
                ExportManager.job = null
                try { if (wl.isHeld) wl.release() } catch (_: Exception) {}
                dispatcher.close()
                stopForeground(STOP_FOREGROUND_DETACH)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun notify(n: Notification) {
        try { (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIF_ID, n) } catch (_: Exception) {}
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(NotificationChannel(CHANNEL, "Video export", NotificationManager.IMPORTANCE_LOW))
        }
    }

    private fun notification(text: String, progress: Float, ongoing: Boolean): Notification {
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val b = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setContentIntent(open)
            .setOngoing(ongoing)
            .setOnlyAlertOnce(true)
        if (ongoing) b.setProgress(100, (progress * 100).toInt(), false)
        return b.build()
    }

    companion object {
        private const val CHANNEL = "nh_export"
        private const val NOTIF_ID = 4021
    }
}
