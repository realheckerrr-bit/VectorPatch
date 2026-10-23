package dev.vectorpatch.updater

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class UpdateDownloadService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(43, buildNotification())
        // TODO: download APK from release URL and trigger install
        return START_NOT_STICKY
    }

    private fun buildNotification() = NotificationCompat
        .Builder(this, "vectorpatch_updates")
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setContentTitle("Downloading VectorPatch update…")
        .setProgress(0, 0, true)
        .build()
}
