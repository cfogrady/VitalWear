package com.github.cfogrady.vitalwear.training

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager

class TrainingForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    lateinit var trainingService: TrainingService

    override fun onCreate() {
        super.onCreate()
        trainingService = (application as VitalWearApp).trainingService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, NotificationChannelManager.CHANNEL_ID).build()
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            ServiceCompat.startForeground(this, NotificationChannelManager.BACKGROUND_TRAINING, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            startForeground(NotificationChannelManager.BACKGROUND_TRAINING, notification)
        }
        doTraining()
        return super.onStartCommand(intent, flags, startId)
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