package com.github.cfogrady.vitalwear.character

import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.card.CardMeta
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.CharacterEntity
import com.github.cfogrady.vitalwear.firmware.components.EmoteBitmaps
import com.github.cfogrady.vitalwear.character.data.Mood
import com.github.cfogrady.vitalwear.character.data.SupportCharacter
import com.github.cfogrady.vitalwear.character.transformation.ExpectedTransformation
import com.github.cfogrady.vitalwear.character.transformation.FusionTransformation
import com.github.cfogrady.vitalwear.character.transformation.TransformationOption
import com.github.cfogrady.vitalwear.common.card.CardType
import com.github.cfogrady.vitalwear.common.card.db.AttributeFusionEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntity
import com.github.cfogrady.vitalwear.common.card.db.SpecificFusionEntity
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.settings.CharacterSettings
import com.github.cfogrady.vitalwear.training.BackgroundTrainingResults
import com.github.cfogrady.vitalwear.training.TrainingStatChanges
import com.github.cfogrady.vitalwear.training.TrainingType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.time.LocalDateTime

abstract class VBCharacter(
    val cardMeta: CardMeta,
    val characterSprites: CharacterSprites,
    val characterStats: CharacterEntity,
    val speciesStats : SpeciesEntity,
    val transformationWaitTimeSeconds: Long,
    val transformationOptions: List<TransformationOption>,
    internal val attributeFusionEntity: AttributeFusionEntity?,
    internal val specificFusionOptions: List<SpecificFusionEntity>,
    val settings: CharacterSettings,
    internal val _readyToTransform: MutableStateFlow<ExpectedTransformation?> = MutableStateFlow<ExpectedTransformation?>(null),
    internal var lastTransformationCheck: LocalDateTime = LocalDateTime.MIN,
    internal val currentTimeProvider: ()->LocalDateTime = LocalDateTime::now
    ) {
    val readyToTransform : StateFlow<ExpectedTransformation?> = _readyToTransform

    fun isBEM() : Boolean {
        return cardMeta.cardType == CardType.BEM
    }

    open fun otherCardNeedsStatConversion(otherCard: CardMetaEntity): Boolean {
        return (isBEM() || settings.assumedFranchise != null) && otherCard.franchise == 0
    }

    fun cardName(): String {
        return cardMeta.cardName
    }

    fun canIncreaseStats(): Boolean {
        val trainingEndTime = characterStats.lastUpdate.plusSeconds(characterStats.trainingTimeRemainingInSeconds)
        return trainingEndTime.isAfter(currentTimeProvider.invoke())
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
            if((transformationOption.requiredAdventureCompleted ?: -1) > (cardMeta.maxAdventureCompletion
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
        Timber.i("Checking transformations")
        support?.let {
            if(it.franchiseId == getFranchise()) {
                checkFusion(support)?.let {
                    Timber.i("Fusion Option Available")
                    _readyToTransform.tryEmit(it)
                    return
                }
            }
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

    fun getFranchise(): Int {
        return settings.assumedFranchise ?: cardMeta.franchise
    }

    fun getNormalBitmaps(recentSteps: Boolean, exerciseLevel: Int): List<Bitmap> {
        return if(characterStats.sleeping) {
            characterSprites.sprites.subList(CharacterSprites.DOWN, CharacterSprites.DOWN+1)
        } else if(exerciseLevel == 3 && recentSteps) {
            characterSprites.sprites.subList(CharacterSprites.RUN_1, CharacterSprites.RUN_2+1)
        } else if(exerciseLevel == 3) {
            characterSprites.sprites.subList(CharacterSprites.TRAIN_1, CharacterSprites.TRAIN_2+1)
        } else if(recentSteps) {
            characterSprites.sprites.subList(CharacterSprites.WALK_1, CharacterSprites.WALK_2+1)
        } else {
            characterSprites.sprites.subList(CharacterSprites.IDLE_1, CharacterSprites.IDLE_2+1)
        }
    }

    // getEmoteBitmaps gets the list of bitmaps corresponding to the current emote if any.
    // Returns an list of optional bitmap. Optional because a null value indicates an emote which
    // flashes on then off.
    fun getEmoteBitmaps(firmware: EmoteBitmaps, exerciseLevel: Int): List<Bitmap?> {
        return if(characterStats.sleeping) {
            firmware.sleepEmote
        } else if(exerciseLevel == 3) {
            listOf(firmware.sweatEmote, null)
        } else if (mood() == Mood.GOOD) {
            firmware.happyEmote
        } else if (mood() == Mood.BAD) {
            firmware.loseEmote
        } else {
            emptyList()
        }
    }
}