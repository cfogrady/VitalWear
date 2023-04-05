package com.github.cfogrady.vitalwear.character.data

import android.graphics.Bitmap
import com.github.cfogrady.vb.dim.character.BemCharacterStats
import com.github.cfogrady.vb.dim.character.CharacterStats
import java.util.*

class BEMCharacter(
    val sprites: List<Bitmap>,
    val characterStats: CharacterEntity,
    val speciesStats : CharacterStats.CharacterStatsEntry,
    val transformationWaitTimeSeconds: Long,
    val transformationOptions: List<TransformationOption>,
    var readyToTransform: Optional<TransformationOption> = Optional.empty()) {
    var activityIdx : Int = 1

    fun isBEM() : Boolean {
        return speciesStats is BemCharacterStats.BemCharacterStatEntry
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
}