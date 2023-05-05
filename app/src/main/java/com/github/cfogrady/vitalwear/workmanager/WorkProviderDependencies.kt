package com.github.cfogrady.vitalwear.workmanager

import com.github.cfogrady.vitalwear.character.BEMUpdater
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.mood.BEMMoodUpdater
import com.github.cfogrady.vitalwear.steps.DailyStepHandler

class WorkProviderDependencies(
    val characterManager: CharacterManager,
    val dailyStepHandler: DailyStepHandler,
)