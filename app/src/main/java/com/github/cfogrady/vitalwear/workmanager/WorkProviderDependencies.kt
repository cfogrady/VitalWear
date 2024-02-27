package com.github.cfogrady.vitalwear.workmanager

import android.content.SharedPreferences
import com.github.cfogrady.vitalwear.SaveService
import com.github.cfogrady.vitalwear.character.VBUpdater
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager

class WorkProviderDependencies(
    val characterManager: CharacterManager,
    val notificationChannelManager: NotificationChannelManager,
    val bemUpdater: VBUpdater,
    val saveService: SaveService,
    val sharedPreferences: SharedPreferences,
)