package com.github.cfogrady.vitalwear.character

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.min

class CharacterUpdateWorker  (val characterManager: CharacterManager, context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private var lastDelta : Int? = null
    override fun doWork(): Result {
        val maybeCharacter = characterManager.getActiveCharacter()
        if(!maybeCharacter.isPresent) {
            // no character to update
            return Result.success()
        }
        val character = maybeCharacter.get().value!!
        updateMood(character)
        val now = LocalDateTime.now()
        val elapsedTimeInSecondsSinceLastUpdate = Duration.between(character.characterStats.lastUpdate, now).seconds
        character.characterStats.timeUntilNextTransformation -= elapsedTimeInSecondsSinceLastUpdate
        updateTrainingTime(character, elapsedTimeInSecondsSinceLastUpdate)
        character.characterStats.lastUpdate = now
        return Result.success()
    }

    fun updateTrainingTime(character: BEMCharacter, elapsedTimeInSecondsSinceLastUpdate: Long) {
        if(character.characterStats.trainingTimeRemainingInSeconds > 0) {
            if(elapsedTimeInSecondsSinceLastUpdate > character.characterStats.trainingTimeRemainingInSeconds) {
                character.characterStats.trainingTimeRemainingInSeconds = 0
            } else {
                character.characterStats.trainingTimeRemainingInSeconds -= elapsedTimeInSecondsSinceLastUpdate
            }
        }
    }

    fun updateMood(character: BEMCharacter) {
        val current = currentHeartRate()
        val resting = restingHeartRate()
        val delta = current - resting
        if (delta > 40) {
            character.characterStats.mood += min(15, untilMaxMood(character))
        } else if (delta > 10) {
            character.characterStats.mood += min(10, untilMaxMood(character))
        } else if(lastDeltaWasType0(lastDelta) && character.characterStats.mood > 0) {
            character.characterStats.mood -= 1
        }
        lastDelta = delta
    }

    fun lastDeltaWasType0(lastDelta: Int?): Boolean {
        return lastDelta != null && lastDelta!! <= 10
    }

    fun untilMaxMood(character: BEMCharacter) : Int {
        return 100 - character.characterStats.mood
    }

    fun currentHeartRate(): Int {
        //TODO: Get actual
        return 80
    }

    fun restingHeartRate(): Int {
        //TODO: Get actual
        return 65
    }

}