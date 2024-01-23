package com.github.cfogrady.vitalwear.character.mood

import android.util.Log
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import com.github.cfogrady.vitalwear.steps.SensorStepService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

class BEMMoodUpdater(private val heartRateService: HeartRateService, private val stepService: SensorStepService) {
    private var lastLevel : Int = 0

    companion object {
        const val TAG = "BEMMoodUpdater"
    }

    fun updateMood(character: VBCharacter, now: LocalDateTime) {
        GlobalScope.launch {
            try {
                stepService.addStepsToVitals()
                val exerciseLevel = heartRateService.getExerciseLevel(lastLevel, now)
                updateFromExerciseLevel(character, exerciseLevel, now)
                lastLevel = exerciseLevel
                Log.i(TAG, "Mood updated successfully")
            } catch (ise: IllegalStateException) {
                // primarily caused in emulator by lack of step sensor
                Log.e(MoodBroadcastReceiver.TAG, "Failed to update mood", ise)
            }
        }
    }

    private fun updateFromExerciseLevel(character: VBCharacter, exerciseLevel: Int, now: LocalDateTime) {
        when(exerciseLevel) {
            0 -> character.characterStats.mood -= 1
            2 -> character.characterStats.mood += 10
            3 -> character.characterStats.mood += 15
        }
        if(character.characterStats.mood < 0) {
            character.characterStats.mood = 0
        } else if(character.characterStats.mood > 100) {
            character.characterStats.mood = 100
        }
        character.characterStats.updateTimeStamps(now)
    }
}