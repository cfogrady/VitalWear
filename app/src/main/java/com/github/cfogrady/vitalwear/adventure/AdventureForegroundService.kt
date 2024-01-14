package com.github.cfogrady.vitalwear.adventure

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager

class AdventureForegroundService : Service() {

    companion object {
        const val TAG = "AdventureService"
    }

    private lateinit var adventureService: AdventureService
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        adventureService = (application as VitalWearApp).adventureService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationBuilder = NotificationCompat.Builder(this, NotificationChannelManager.CHANNEL_ID)
            .setContentTitle("VitalWear Adventure")
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            notificationBuilder.setCategory(Notification.CATEGORY_WORKOUT)
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            ServiceCompat.startForeground(this, NotificationChannelManager.BACKGROUND_ADVENTURE, notificationBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            notificationBuilder.setCategory(Notification.CATEGORY_SERVICE)
            startForeground(NotificationChannelManager.BACKGROUND_ADVENTURE, notificationBuilder.build())
        }
        // we need this lock so our service prevents Doze mode from taking affect
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$TAG:lock").apply {
                    acquire()
                }
            }
        Log.i(TAG, "Start foreground adventure")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Remove Wakelock")
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }
}