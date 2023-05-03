package com.github.cfogrady.vitalwear.character.data

import android.graphics.Bitmap
import com.github.cfogrady.vb.dim.character.BemCharacterStats
import com.github.cfogrady.vb.dim.character.CharacterStats
import com.github.cfogrady.vb.dim.character.CharacterStats.CharacterStatsEntry
import java.time.LocalDateTime
import java.util.*

class BEMCharacter(
    val sprites: List<Bitmap>,
    val characterStats: CharacterEntity,
    val speciesStats : CharacterStatsEntry,
    private val transformationWaitTimeSeconds: Long,
    val transformationOptions: List<TransformationOption>,
    var readyToTransform: Optional<TransformationOption> = Optional.empty()) {
    var activityIdx : Int = 1

    companion object {
        private val DEFAULT_STATS = CharacterEntity(-1, CharacterState.SYNCED, "NONE", 0, LocalDateTime.MIN, 0, 0, false, 0, 0, 0, 0, 0, false, 0, 0, 0, 0, 0, 0, 0, false)
        val DEFAULT_CHARACTER = BEMCharacter(emptyList(), DEFAULT_STATS, CharacterStatsEntry.builder().build(), 0, emptyList())
    }

    fun isBEM() : Boolean {
        return speciesStats is BemCharacterStats.BemCharacterStatEntry
    }

    fun debug(): List<Pair<String, String>> {
        return listOf(
            Pair("MoodVal", "${characterStats.mood}"),
            Pair("Mood", mood().name),
            Pair("Vitals", "${characterStats.vitals}"),
        )
    }

    fun hasValidTransformation(): Optional<TransformationOption> {
        for(transformationOption in transformationOptions) {
            if(transformationOption.requiredVitals > characterStats.vitals) {
                continue
            }
            if(transformationOption.requiredPp > characterStats.trainedPP) {
                continue
            }
            if(transformationOption.requiredBattles > characterStats.currentPhaseBattles) {
                continue
            }
            if(transformationOption.requiredWinRatio > characterStats.currentPhaseWinRatio()) {
                continue
            }
            return Optional.of(transformationOption)
        }
        return Optional.empty()
    }

    fun prepCharacterTransformation() {
        val characterStats = characterStats
        val transformationOption = hasValidTransformation()
        if(transformationOption.isPresent) {
            readyToTransform = transformationOption
        } else {
            characterStats.timeUntilNextTransformation = transformationWaitTimeSeconds
        }
    }

    fun totalBp(): Int {
        return speciesStats.dp + characterStats.trainedBp
    }
    fun totalAp(): Int {
        return speciesStats.ap + characterStats.trainedAp
    }
    fun totalHp(): Int {
        return speciesStats.hp + characterStats.trainedHp
    }

    fun mood(): Mood {
        val mood = characterStats.mood
        return if(mood > 70) {
            Mood.GOOD
        } else if(mood > 20) {
            Mood.NORMAL
        } else {
            Mood.BAD
        }
    }
}