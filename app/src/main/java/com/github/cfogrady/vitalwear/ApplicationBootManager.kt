package com.github.cfogrady.vitalwear

import android.content.Context
import com.github.cfogrady.vitalwear.character.VBUpdater
import com.github.cfogrady.vitalwear.character.CharacterManagerImpl
import com.github.cfogrady.vitalwear.character.mood.MoodService
import com.github.cfogrady.vitalwear.complications.ComplicationRefreshService
import com.github.cfogrady.vitalwear.debug.ExceptionService
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager
import com.github.cfogrady.vitalwear.steps.StepSensorService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ApplicationBootManager(private val characterManager: CharacterManagerImpl,
                             private val stepService: StepSensorService,
                             private val vbUpdater: VBUpdater,
                             private val moodService: MoodService,
                             private val saveService: SaveService,
                             private val notificationChannelManager: NotificationChannelManager,
                             private val complicationRefreshService: ComplicationRefreshService,
) {

    @Synchronized
    fun onStartup(context: Context) {
        GlobalScope.launch {
            try {
                // characterManager init will load WorkManager configuration
                characterManager.init(context, vbUpdater)
                moodService.initialize()
                stepService.startup()
            } catch (e: java.lang.Exception) {
                ExceptionService.instance!!.logException(e)
            }
            notificationChannelManager.createNotificationChannel()
            complicationRefreshService.startupPartnerComplications()

        }
    }
}