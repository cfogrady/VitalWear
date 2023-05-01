package com.github.cfogrady.vitalwear.character.mood

import android.util.Log
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

class BEMMoodUpdater(private val heartRateService: HeartRateService) {
    private var lastLevel : Int = 0

    fun updateMood(character: BEMCharacter, now: LocalDateTime) : CompletableFuture<Void> {
        val currentExerciseLevelFuture = heartRateService.getExerciseLevel(lastLevel)
        return currentExerciseLevelFuture.thenAccept { level: Int ->
            updateFromExerciseLevel(character, level, now)
            lastLevel = level
            Log.i(MoodUpdateWorker.TAG, "Mood updated successfully")
        }
    }

    private fun updateFromExerciseLevel(character: BEMCharacter, exerciseLevel: Int, now: LocalDateTime) {
        //TODO: Double Check mechanics when I have internet
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