package com.example.digitaldetox.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.digitaldetox.MainActivity

class DailyAnalyticsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        showNotification(applicationContext)
        return Result.success()
    }

    private fun showNotification(context: Context) {
        val channelId = "analytics_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Analytics",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminder to view your digital detox analytics."
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Deep link intent to navigate directly to the analytics screen
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("detto://analytics"),
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            // Note: Replace with your actual drawable resource if available, fallback to launcher icon for now
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Your Daily Analytics")
            .setContentText("Tap to view your tree growth and distraction stats for today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
