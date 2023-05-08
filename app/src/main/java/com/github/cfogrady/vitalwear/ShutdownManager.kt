package com.github.cfogrady.vitalwear

import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.steps.SensorStepService
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class ShutdownManager(private val saveService: SaveService) {
    fun shutdown() {
        runBlocking {
            saveService.save()
        }
    }
}