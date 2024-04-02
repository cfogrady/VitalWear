package com.github.cfogrady.vitalwear

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.github.cfogrady.vitalwear.character.CharacterManagerImpl
import com.github.cfogrady.vitalwear.steps.StepIOService
import kotlinx.coroutines.*
import timber.log.Timber
import java.time.LocalDateTime

class SaveService(private val characterManager: CharacterManagerImpl, private val stepIOService: StepIOService, private val sharedPreferences: SharedPreferences) {

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
            stepIOService.editStepPreferenceUpdates(now.toLocalDate(), preferencesEditor).commit()
            characterManager.updateActiveCharacter(now)
        } catch (ise: IllegalStateException) {
            // primarily caused in emulator by lack of step sensor
            Timber.e("Failed to save steps...", ise)
        }
    }
}