package com.github.cfogrady.vitalwear.adventure

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager
import timber.log.Timber

class AdventureForegroundService : Service() {

    companion object {
        const val CARD_NAME = "CARD_NAME"
        const val STARTING_ADVENTURE = "STARTING_ADVENTURE"
    }

    private lateinit var adventureService: AdventureService
    // private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        adventureService = (application as VitalWearApp).adventureService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent == null) {
            Timber.e("Attempted to start AdventureForegroundService with null intent!")
            return super.onStartCommand(null, flags, startId)
        }
        val cardName = intent.extras?.getString(CARD_NAME)!!
        val startingAdventure = intent.extras?.getInt(STARTING_ADVENTURE)!!
        adventureService.startAdventure(this, cardName, startingAdventure)
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

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
//        Timber.i("Remove Wakelock")
//        wakeLock?.let {
//            if (it.isHeld) {
//                it.release()
//            }
//        }
    }
}