package com.github.cfogrady.vitalwear.character

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.character.data.Character
import kotlin.math.min

class BemMoodWorker  (val character: Character, context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private var lastDelta : Int? = null
    override fun doWork(): Result {
        val current = currentHeartRate()
        val resting = restingHeartRate()
        val delta = current - resting
        if (delta > 40) {
            character.characterStats.mood += min(15, untilMaxMood())
        } else if (delta > 10) {
            character.characterStats.mood += min(10, untilMaxMood())
        } else if(lastDeltaWasType0(lastDelta) && character.characterStats.mood > 0) {
            character.characterStats.mood -= 1
        }
        lastDelta = delta
        return Result.success()
    }

    fun lastDeltaWasType0(lastDelta: Int?): Boolean {
        return lastDelta != null && lastDelta!! <= 10
    }

    fun untilMaxMood() : Int {
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