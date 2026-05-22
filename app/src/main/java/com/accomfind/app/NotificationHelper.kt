package com.accomfind.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    private const val CHANNEL_ID = "accomfind_channel"
    private const val CHANNEL_NAME = "AccomFind Alerts"
    private var notifId = 1

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Accommodation match alerts"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun sendMatchNotification(context: Context, count: Int, location: String?) {
        createChannel(context)
        val locationText = if (!location.isNullOrEmpty()) " in $location" else ""
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("AccomFind - Listings Found!")
            .setContentText("$count listing(s) match your preferences$locationText")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("We found $count accommodation listing(s) matching your saved preferences$locationText. Tap to explore!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notifId++, builder.build())
        } catch (e: SecurityException) {
            // Notification permission not granted (Android 13+)
        }
    }
}
