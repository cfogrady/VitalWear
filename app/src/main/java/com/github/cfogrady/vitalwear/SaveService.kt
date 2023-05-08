package com.github.cfogrady.vitalwear

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.github.cfogrady.vitalwear.character.CharacterManagerImpl
import com.github.cfogrady.vitalwear.steps.SensorStepService
import kotlinx.coroutines.*
import java.time.LocalDateTime

class SaveService(private val characterManager: CharacterManagerImpl, private val stepService: SensorStepService, private val sharedPreferences: SharedPreferences) {
    fun saveAsync(preferencesEditor: Editor = sharedPreferences.edit()) {
        GlobalScope.launch(Dispatchers.IO) {
            internalSave(preferencesEditor)
        }
    }

    fun saveBlocking(preferencesEditor: Editor = sharedPreferences.edit()) {
        runBlocking(Dispatchers.IO) {
            internalSave(preferencesEditor)
        }
    }

    suspend fun save(preferencesEditor: Editor = sharedPreferences.edit()) {
        withContext(Dispatchers.IO) {
            internalSave(preferencesEditor)
        }
    }

    private suspend fun internalSave(preferencesEditor: Editor) {
        val now = LocalDateTime.now()
        stepService.addStepsToVitals()
        stepService.stepPreferenceUpdates(now.toLocalDate(), preferencesEditor).commit()
        characterManager.updateActiveCharacter(now)
    }
}