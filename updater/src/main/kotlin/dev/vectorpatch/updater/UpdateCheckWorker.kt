package dev.vectorpatch.updater

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.os.Build

class UpdateCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val checker = GitHubUpdateChecker(context, "realheckerrr-bit", "VectorPatch")
        val release = checker.getLatestRelease() ?: return Result.success()

        if (release.isNewer) {
            showUpdateNotification(release)
        }
        return Result.success()
    }

    private fun showUpdateNotification(release: ReleaseInfo) {
        val channelId = "vectorpatch_updates"
        val nm        = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "VectorPatch Updates",
                    NotificationManager.IMPORTANCE_DEFAULT)
            )
        }

        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
        val pi = PendingIntent.getActivity(context, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("VectorPatch update available")
            .setContentText("Version ${release.tagName} is ready to download")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        nm.notify(42, notification)
    }
}
