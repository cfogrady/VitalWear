package com.github.cfogrady.vitalwear.character

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.min

class CharacterUpdateWorker  (val characterManager: CharacterManager, context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    val bemMoodUpdater = BEMMoodUpdater()
    override fun doWork(): Result {
        val liveCharacter = characterManager.getActiveCharacter()
        if(liveCharacter.value == null) {
            // no character to update
            return Result.success()
        }
        val character = liveCharacter.value!!
        bemMoodUpdater.updateMood(character)
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

}