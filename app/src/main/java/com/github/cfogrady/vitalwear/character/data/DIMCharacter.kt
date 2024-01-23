package com.github.cfogrady.vitalwear.character.data

import com.github.cfogrady.vitalwear.character.StatType
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.character.transformation.ExpectedTransformation
import com.github.cfogrady.vitalwear.character.transformation.TransformationOption
import com.github.cfogrady.vitalwear.common.card.CardType
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

class DIMCharacter(
    cardMetaEntity: CardMetaEntity,
    characterSprites: CharacterSprites,
    characterStats: CharacterEntity,
    speciesStats : SpeciesEntity,
    transformationWaitTimeSeconds: Long,
    transformationOptions: List<TransformationOption>,
    attributeFusionEntity: AttributeFusionEntity?,
    specificFusionOptions: List<SpecificFusionEntity>,
    settings: CharacterSettingsEntity,
    _readyToTransform: MutableStateFlow<ExpectedTransformation?> = MutableStateFlow<ExpectedTransformation?>(null),
    activityIdx : Int = 1,
    lastTransformationCheck: LocalDateTime = LocalDateTime.MIN,
    currentTimeProvider: ()->LocalDateTime = LocalDateTime::now
): VBCharacter(
    cardMetaEntity,
    characterSprites,
    characterStats,
    speciesStats,
    transformationWaitTimeSeconds,
    transformationOptions,
    attributeFusionEntity,
    specificFusionOptions,
    settings,
    _readyToTransform,
    activityIdx,
    lastTransformationCheck,
    currentTimeProvider
) {

    fun copy(
        cardMetaEntity: CardMetaEntity = this.cardMetaEntity,
        characterSprites: CharacterSprites = this.characterSprites,
        characterStats: CharacterEntity = this.characterStats,
        speciesStats : SpeciesEntity = this.speciesStats,
        transformationWaitTimeSeconds: Long = this.transformationWaitTimeSeconds,
        transformationOptions: List<TransformationOption> = this.transformationOptions,
        attributeFusionEntity: AttributeFusionEntity? = this.attributeFusionEntity,
        specificFusionOptions: List<SpecificFusionEntity> = this.specificFusionOptions,
        settings: CharacterSettingsEntity = this.settings,
    ): DIMCharacter {
        return DIMCharacter(
            cardMetaEntity,
            characterSprites,
            characterStats,
            speciesStats,
            transformationWaitTimeSeconds,
            transformationOptions,
            attributeFusionEntity,
            specificFusionOptions,
            settings,
            _readyToTransform = _readyToTransform,
            activityIdx = activityIdx,
            lastTransformationCheck = lastTransformationCheck,
        )
    }

    override fun isBEM() : Boolean {
        return cardMetaEntity.cardType == CardType.BEM
    }

    override fun totalBp(): Int {
        return speciesStats.bp
    }
    override fun totalAp(): Int {
        return speciesStats.ap
    }
    override fun totalHp(): Int {
        return speciesStats.hp
    }

    override fun increaseStats(trainingType: TrainingType, great: Boolean): TrainingStatChanges {
        if(!canIncreaseStats()) {
            return TrainingStatChanges(StatType.PP, 0)
        }
        var statChange = 1
        if(great) {
            statChange *= 2
        }
        return applyIncreaseToStat(statChange, StatType.PP) //pp
    }

    override fun increaseStatsFromMultipleTrainings(backgroundTrainingResults: BackgroundTrainingResults): TrainingStatChanges {
        if(!canIncreaseStats()) {
            return TrainingStatChanges(StatType.PP, 0)
        }
        val standardStatIncrease = 1
        var statChange = standardStatIncrease * 2 * backgroundTrainingResults.great
        statChange += standardStatIncrease * backgroundTrainingResults.good
        return applyIncreaseToStat(statChange, StatType.PP)
    }
}