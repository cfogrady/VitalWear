package com.github.cfogrady.vitalwear.character

import android.util.Log
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.CharacterEntity
import com.github.cfogrady.vitalwear.character.data.Mood
import com.github.cfogrady.vitalwear.character.data.SupportCharacter
import com.github.cfogrady.vitalwear.character.transformation.ExpectedTransformation
import com.github.cfogrady.vitalwear.character.transformation.FusionTransformation
import com.github.cfogrady.vitalwear.character.transformation.TransformationOption
import com.github.cfogrady.vitalwear.common.card.db.AttributeFusionEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntity
import com.github.cfogrady.vitalwear.common.card.db.SpecificFusionEntity
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.settings.CharacterSettingsEntity
import com.github.cfogrady.vitalwear.training.BackgroundTrainingResults
import com.github.cfogrady.vitalwear.training.TrainingStatChanges
import com.github.cfogrady.vitalwear.training.TrainingType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

abstract class VBCharacter(
    val cardMetaEntity: CardMetaEntity,
    val characterSprites: CharacterSprites,
    val characterStats: CharacterEntity,
    val speciesStats : SpeciesEntity,
    val transformationWaitTimeSeconds: Long,
    val transformationOptions: List<TransformationOption>,
    internal val attributeFusionEntity: AttributeFusionEntity?,
    internal val specificFusionOptions: List<SpecificFusionEntity>,
    val settings: CharacterSettingsEntity,
    internal val _readyToTransform: MutableStateFlow<ExpectedTransformation?> = MutableStateFlow<ExpectedTransformation?>(null),
    var activityIdx : Int = 1,
    internal var lastTransformationCheck: LocalDateTime = LocalDateTime.MIN,
    internal val currentTimeProvider: ()->LocalDateTime = LocalDateTime::now
    ) {
    val readyToTransform : StateFlow<ExpectedTransformation?> = _readyToTransform

    companion object {
        const val TAG = "VBCharacter"
    }

    abstract fun isBEM(): Boolean

    fun cardName(): String {
        return cardMetaEntity.cardName
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

    fun popTransformationOption(): ExpectedTransformation? {
        characterStats.timeUntilNextTransformation = transformationWaitTimeSeconds
        val option = readyToTransform.value
        _readyToTransform.tryEmit(null)
        return option
    }

    fun hasPotentialTransformations(): Boolean {
        return transformationOptions.isNotEmpty() ||
                specificFusionOptions.isNotEmpty() ||
                (attributeFusionEntity?.hasPossibleResults() ?: false)
    }

    fun hasValidTransformation(): TransformationOption? {
        for(transformationOption in transformationOptions) {
            if((transformationOption.requiredAdventureCompleted ?: -1) > (cardMetaEntity.maxAdventureCompletion
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

    private fun checkFusion(support: SupportCharacter?) : ExpectedTransformation? {
        if(support == null) {
            return null
        }
        for(fusionOption in specificFusionOptions) {
            if(specificFusionMatch(fusionOption, support)) {
                return FusionTransformation(fusionOption.toCharacterId, support.idleSprite, support.idle2Sprite, support.attackSprite)
            }
        }
        if(support.phase == speciesStats.phase) {
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

    fun prepCharacterTransformation(support: SupportCharacter?) {
        lastTransformationCheck = LocalDateTime.now()
        val characterStats = characterStats
        Log.i(TAG, "Checking transformations")
        checkFusion(support)?.let {
            Log.i(TAG, "Fusion Option Available")
            _readyToTransform.tryEmit(it)
            return
        }
        val transformationOption = hasValidTransformation()
        if(transformationOption != null) {
            _readyToTransform.tryEmit(transformationOption.toExpectedTransformation())
        } else {
            characterStats.timeUntilNextTransformation = transformationWaitTimeSeconds
        }
    }

    abstract fun totalBp(): Int
    abstract fun totalAp(): Int
    abstract fun totalHp(): Int

    fun addVitals(vitalChange: Int) {
        characterStats.vitals += vitalChange
        if(characterStats.vitals > BEMCharacter.MAX_VITALS) {
            characterStats.vitals = BEMCharacter.MAX_VITALS
        } else if(characterStats.vitals < 0) {
            characterStats.vitals = 0
        }
    }

    open fun increaseStats(trainingType: TrainingType, great: Boolean): TrainingStatChanges {
        if(!canIncreaseStats()) {
            return TrainingStatChanges(trainingType.affectedStat, 0)
        }
        var statChange = trainingType.standardTrainingIncrease
        if(great) {
            statChange *= 2
        }
        return applyIncreaseToStat(statChange, trainingType.affectedStat)
    }

    open fun increaseStatsFromMultipleTrainings(backgroundTrainingResults: BackgroundTrainingResults): TrainingStatChanges {
        if(!canIncreaseStats()) {
            TrainingStatChanges(backgroundTrainingResults.trainingType.affectedStat, 0)
        }
        val trainingType = backgroundTrainingResults.trainingType
        val standardStatIncrease = trainingType.standardTrainingIncrease
        var statChange = standardStatIncrease * 2 * backgroundTrainingResults.great
        statChange += standardStatIncrease * backgroundTrainingResults.good
        return applyIncreaseToStat(statChange, backgroundTrainingResults.trainingType.affectedStat)
    }

    internal fun applyIncreaseToStat(trainedIncrease: Int, statType: StatType): TrainingStatChanges {
        var actualIncrease = trainedIncrease
        when(statType) {
            StatType.PP -> {
                actualIncrease = actualIncrease.coerceAtMost(99 - characterStats.trainedPP)
                characterStats.trainedPP = characterStats.trainedPP + actualIncrease
            }
            StatType.HP -> {
                actualIncrease = actualIncrease.coerceAtMost(999 - characterStats.trainedHp)
                characterStats.trainedHp = characterStats.trainedHp + actualIncrease
            }
            StatType.AP -> {
                actualIncrease = actualIncrease.coerceAtMost(999 - characterStats.trainedAp)
                characterStats.trainedAp = characterStats.trainedAp + actualIncrease
            }
            StatType.BP -> {
                actualIncrease = actualIncrease.coerceAtMost(999 - characterStats.trainedBp)
                characterStats.trainedBp = characterStats.trainedBp + actualIncrease
            }
        }
        return TrainingStatChanges(statType, actualIncrease)
    }
}