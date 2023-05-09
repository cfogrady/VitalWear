package com.github.cfogrady.vitalwear.workmanager

import android.content.SharedPreferences
import com.github.cfogrady.vitalwear.SaveService
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.steps.DailyStepHandler

class WorkProviderDependencies(
    val characterManager: CharacterManager,
    val dailyStepHandler: DailyStepHandler,
    val saveService: SaveService,
    val sharedPreferences: SharedPreferences,
)