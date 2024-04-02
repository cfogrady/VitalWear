package com.github.cfogrady.vitalwear.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.github.cfogrady.vitalwear.R
import com.github.cfogrady.vitalwear.activity.MainActivity
import timber.log.Timber

class NotificationChannelManager(private val notificationManager: NotificationManager) {
    companion object {
        const val NOTIFICATION_CHANNEL = "Vital Wear"
        const val CHANNEL_ID = "VitalWearMainChannel"
        const val TRANSFORMATION_READY_ID = 0
        const val BACKGROUND_TRAINING = 1
        const val BACKGROUND_ADVENTURE = 2
        const val ADVENTURE_BOSS = 3
    }

    private var genericNotificationId = 100

    fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, NOTIFICATION_CHANNEL, importance).apply {
            description = NOTIFICATION_CHANNEL
        }
        // Register the channel with the system
        notificationManager.createNotificationChannel(channel)
    }

    fun sendGenericNotification(context: Context, title: String, content: String, notificationId: Int = genericNotificationId++): Int {
        Timber.i("Sending notification: $title")
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.complication_preview)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        notificationManager.notify(notificationId, builder.build())
        return notificationId
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}