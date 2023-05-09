package com.github.cfogrady.vitalwear

import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.steps.SensorStepService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class ShutdownManager(private val saveService: SaveService) {
    fun shutdown() {
        // TODO: Replace with runBlocking once saveService no longer requires the main thread to be free.
        GlobalScope.launch {
            saveService.save()
        }
    }
}