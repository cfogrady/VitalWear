package com.github.cfogrady.vitalwear.character.data

import android.util.Log
import com.github.cfogrady.vitalwear.character.transformation.ExpectedTransformation
import com.github.cfogrady.vitalwear.character.transformation.FusionTransformation
import com.github.cfogrady.vitalwear.character.transformation.TransformationOption
import com.github.cfogrady.vitalwear.common.card.CardType
import com.github.cfogrady.vitalwear.common.card.db.AttributeFusionEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntity
import com.github.cfogrady.vitalwear.common.card.db.SpecificFusionEntity
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.settings.CharacterSettingsEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import java.util.*

class BEMCharacter(
    val cardMetaEntity: CardMetaEntity,
    val characterSprites: CharacterSprites,
    val characterStats: CharacterEntity,
    val speciesStats : SpeciesEntity,
    val transformationWaitTimeSeconds: Long,
    val transformationOptions: List<TransformationOption>,
    val attributeFusionEntity: AttributeFusionEntity?,
    val specificFusionOptions: List<SpecificFusionEntity>,
    val settings: CharacterSettingsEntity,
    private val currentTimeProvider: ()->LocalDateTime = LocalDateTime::now
) {
    private var _readyToTransform = MutableStateFlow<ExpectedTransformation?>(null)
    var readyToTransform : StateFlow<ExpectedTransformation?> = _readyToTransform
    var activityIdx : Int = 1

    companion object {
        const val TAG = "BEMCharacter"
        const val MAX_VITALS = 9999
        val DEFAULT_CHARACTER: BEMCharacter? = null
    }

    fun isBEM() : Boolean {
        return cardMetaEntity.cardType == CardType.BEM
    }

    fun cardName(): String {
        return cardMetaEntity.cardName
    }

    fun hasPotentialTransformations(): Boolean {
        return transformationOptions.isNotEmpty() ||
                specificFusionOptions.isNotEmpty() ||
                (attributeFusionEntity?.hasPossibleResults() ?: false)
    }

    fun canIncreaseStats(): Boolean {
        val trainingEndTime = characterStats.lastUpdate.plusSeconds(characterStats.trainingTimeRemainingInSeconds)
        return trainingEndTime.isAfter(currentTimeProvider.invoke())
    }

    fun debug(): List<Pair<String, String>> {
        return listOf(
            Pair("MoodVal", "${characterStats.mood}"),
            Pair("Mood", mood().name),
            Pair("Vitals", "${characterStats.vitals}"),
            Pair("TimeUntilEvolveSeconds", "${characterStats.timeUntilNextTransformation}"),
            Pair("TimeUntilEvolveMinutes", "${characterStats.timeUntilNextTransformation/60}"),
            Pair("TimeUntilEvolveHours", "${characterStats.timeUntilNextTransformation/(60*60)}"),
            Pair("Last Transformation Check", if(lastTransformationCheck == LocalDateTime.MIN) "NONE" else "$lastTransformationCheck"),
        )
    }

    fun popTransformationOption(): ExpectedTransformation? {
        characterStats.timeUntilNextTransformation = transformationWaitTimeSeconds
        val option = readyToTransform.value
        _readyToTransform.tryEmit(null)
        return option
    }

    private fun hasValidTransformation(highestAdventureCompleted: Int?): TransformationOption? {
        for(transformationOption in transformationOptions) {
            if((transformationOption.requiredAdventureCompleted ?: -1) > (highestAdventureCompleted
                    ?: -1)
            ) {
                continue
            }
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
            return transformationOption
        }
        return null
    }

    private var lastTransformationCheck = LocalDateTime.MIN

    fun prepCharacterTransformation(support: SupportCharacter?, highestAdventureCompleted: Int?) {
        lastTransformationCheck = LocalDateTime.now()
        val characterStats = characterStats
        Log.i(TAG, "Checking transformations")
        checkFusion(support)?.let {
            Log.i(TAG, "Fusion Option Available")
            _readyToTransform.tryEmit(it)
            return
        }
        val transformationOption = hasValidTransformation(highestAdventureCompleted)
        if(transformationOption != null) {
            _readyToTransform.tryEmit(transformationOption.toExpectedTransformation())
        } else {
            characterStats.timeUntilNextTransformation = transformationWaitTimeSeconds
        }
    }

    private fun checkFusion(support: SupportCharacter?) : ExpectedTransformation? {
        if(support == null) {
            return null
        }
        for(fusionOption in specificFusionOptions) {
            if(specificFusionMatch(fusionOption, support)) {
                return FusionTransformation(fusionOption.toCharacterId, support.idleSprite, support.idle2Sprite, support.attackSprite)
            }
        }
        if(support.phase != speciesStats.phase) {
            attributeFusionEntity?.let {
                val result = attributeFusionEntity.getResultForAttribute(support.attribute)
                result?.let {
                    return FusionTransformation(result, support.idleSprite, support.idle2Sprite, support.attackSprite)
                }
            }
        }
        return null
    }

    private fun specificFusionMatch(specificFusionEntity: SpecificFusionEntity, support: SupportCharacter): Boolean {
        if (specificFusionEntity.supportCardId != support.cardId) {
            return false
        }
        return specificFusionEntity.supportCharacterId == support.slotId
    }

    fun totalBp(): Int {
        return speciesStats.bp + characterStats.trainedBp.coerceAtMost(999)
    }
    fun totalAp(): Int {
        return speciesStats.ap + characterStats.trainedAp.coerceAtMost(999)
    }
    fun totalHp(): Int {
        return speciesStats.hp + characterStats.trainedHp.coerceAtMost(999)
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

    fun addVitals(vitalChange: Int) {
        characterStats.vitals += vitalChange
        if(characterStats.vitals > MAX_VITALS) {
            characterStats.vitals = MAX_VITALS
        } else if(characterStats.vitals < 0) {
            characterStats.vitals = 0
        }
    }
}