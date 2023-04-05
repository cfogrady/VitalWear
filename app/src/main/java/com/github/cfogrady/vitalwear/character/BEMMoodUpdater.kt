package com.github.cfogrady.vitalwear.character

import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import kotlin.math.min

class BEMMoodUpdater {
    private var lastDelta : Int? = null

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