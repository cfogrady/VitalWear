package com.github.cfogrady.vitalwear

import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.steps.SensorStepService
import java.time.LocalDateTime

class ShutdownManager(private val stepService: SensorStepService, private val characterManager: CharacterManager) {
    fun shutdown(today: LocalDateTime) {
        stepService.handleShutdown(today.toLocalDate())
        characterManager.updateActiveCharacter(today)
    }
}