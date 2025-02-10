package com.github.cfogrady.vitalwear

import android.content.Context
import com.github.cfogrady.vitalwear.character.VBUpdater
import com.github.cfogrady.vitalwear.character.CharacterManagerImpl
import com.github.cfogrady.vitalwear.character.mood.MoodService
import com.github.cfogrady.vitalwear.complications.ComplicationRefreshService
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager
import com.github.cfogrady.vitalwear.steps.StepSensorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApplicationBootManager(private val characterManager: CharacterManagerImpl,
                             private val firmwareManager: FirmwareManager,
                             private val stepService: StepSensorService,
                             private val vbUpdater: VBUpdater,
                             private val moodService: MoodService,
                             private val notificationChannelManager: NotificationChannelManager,
                             private val complicationRefreshService: ComplicationRefreshService,
) {

    @Synchronized
    fun onStartup(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            // parallelize firmware manager, character manager, step service, and notificationChannelManager
            launch {
                firmwareManager.loadFirmware(context)
            }
            launch {
                characterManager.init(context, vbUpdater)
                moodService.initialize()
                complicationRefreshService.startupPartnerComplications()
            }
            launch {
                stepService.startup()
            }
            launch {
                notificationChannelManager.createNotificationChannel()
            }
        }
    }
}