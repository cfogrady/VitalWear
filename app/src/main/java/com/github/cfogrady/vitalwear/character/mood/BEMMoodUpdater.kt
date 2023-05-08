package com.github.cfogrady.vitalwear.character.mood

import android.util.Log
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

    fun updateMood(character: BEMCharacter, now: LocalDateTime) {
        val addStepsFuture = CompletableFuture<Void>()
        GlobalScope.launch {
            stepService.addStepsToVitals()
            addStepsFuture.complete(null)
        }
        // get the exercise level
        val exerciseLevelFuture = heartRateService.getExerciseLevel(lastLevel)
        // once we have both of those, update the mood.
        addStepsFuture.thenAcceptBoth(exerciseLevelFuture) { _, level: Int ->
            updateFromExerciseLevel(character, level, now)
            lastLevel = level
            Log.i(TAG, "Mood updated successfully")
        }
    }

    private fun updateFromExerciseLevel(character: BEMCharacter, exerciseLevel: Int, now: LocalDateTime) {
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