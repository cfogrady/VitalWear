package com.github.cfogrady.vitalwear.training

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager
import timber.log.Timber

class TrainingForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private lateinit var trainingService: TrainingService
    private var wakeLock: PowerManager.WakeLock? = null


    override fun onCreate() {
        super.onCreate()
        trainingService = (application as VitalWearApp).trainingService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationBuilder = NotificationCompat.Builder(this, NotificationChannelManager.CHANNEL_ID)
            .setContentTitle("VitalWear Training")
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            notificationBuilder.setCategory(Notification.CATEGORY_WORKOUT)
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            ServiceCompat.startForeground(this, NotificationChannelManager.BACKGROUND_TRAINING, notificationBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            notificationBuilder.setCategory(Notification.CATEGORY_SERVICE)
            startForeground(NotificationChannelManager.BACKGROUND_TRAINING, notificationBuilder.build())
        }
        // we need this lock so our service prevents Doze mode from taking affect
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TrainingForegroundService:lock").apply {
                    acquire()
                }
            }
        Timber.i("Start foreground training")
        doTraining()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("Remove Wakelock")
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    private fun doTraining() {
        val trainingProgressTracker = trainingService.backgroundTrainingProgressTracker
        if(trainingProgressTracker != null) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                trainingProgressTracker.finishRep()
                doTraining()
            }, trainingProgressTracker.trainingType.durationSeconds * 1000L)
        }
    }
}