package com.github.cfogrady.vitalwear

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.util.Log
import com.github.cfogrady.vitalwear.character.CharacterManagerImpl
import com.github.cfogrady.vitalwear.steps.SensorStepService
import kotlinx.coroutines.*
import java.time.LocalDateTime

class SaveService(private val characterManager: CharacterManagerImpl, private val stepService: SensorStepService, private val sharedPreferences: SharedPreferences) {
    companion object {
        const val TAG = "SaveService"
    }

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

    private fun internalSave(preferencesEditor: Editor) {
        val now = LocalDateTime.now()
        try {
            stepService.stepPreferenceUpdates(now.toLocalDate(), preferencesEditor).commit()
            characterManager.updateActiveCharacter(now)
        } catch (ise: IllegalStateException) {
            // primarily caused in emulator by lack of step sensor
            Log.e(TAG, "Failed to save steps...", ise)
        }
    }
}