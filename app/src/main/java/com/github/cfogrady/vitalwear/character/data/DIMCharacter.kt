package com.github.cfogrady.vitalwear.character.data

import com.github.cfogrady.vitalwear.card.CardMeta
import com.github.cfogrady.vitalwear.character.StatType
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.character.transformation.ExpectedTransformation
import com.github.cfogrady.vitalwear.character.transformation.TransformationOption
import com.github.cfogrady.vitalwear.common.card.db.AttributeFusionEntity
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntity
import com.github.cfogrady.vitalwear.common.card.db.SpecificFusionEntity
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.settings.CharacterSettings
import com.github.cfogrady.vitalwear.training.BackgroundTrainingResults
import com.github.cfogrady.vitalwear.training.TrainingStatChanges
import com.github.cfogrady.vitalwear.training.TrainingType
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime

class DIMCharacter(
    cardMeta: CardMeta,
    characterSprites: CharacterSprites,
    characterStats: CharacterEntity,
    speciesStats : SpeciesEntity,
    transformationWaitTimeSeconds: Long,
    transformationOptions: List<TransformationOption>,
    attributeFusionEntity: AttributeFusionEntity?,
    specificFusionOptions: List<SpecificFusionEntity>,
    settings: CharacterSettings,
    readyToTransform: MutableStateFlow<ExpectedTransformation?> = MutableStateFlow(null),
    lastTransformationCheck: LocalDateTime = LocalDateTime.MIN,
    currentTimeProvider: ()->LocalDateTime = LocalDateTime::now
): VBCharacter(
    cardMeta,
    characterSprites,
    characterStats,
    speciesStats,
    transformationWaitTimeSeconds,
    transformationOptions,
    attributeFusionEntity,
    specificFusionOptions,
    settings,
    readyToTransform,
    lastTransformationCheck,
    currentTimeProvider
) {

    fun copy(
        cardMeta: CardMeta = this.cardMeta,
        characterSprites: CharacterSprites = this.characterSprites,
        characterStats: CharacterEntity = this.characterStats,
        speciesStats : SpeciesEntity = this.speciesStats,
        transformationWaitTimeSeconds: Long = this.transformationWaitTimeSeconds,
        transformationOptions: List<TransformationOption> = this.transformationOptions,
        attributeFusionEntity: AttributeFusionEntity? = this.attributeFusionEntity,
        specificFusionOptions: List<SpecificFusionEntity> = this.specificFusionOptions,
        settings: CharacterSettings = this.settings,
    ): DIMCharacter {
        return DIMCharacter(
            cardMeta,
            characterSprites,
            characterStats,
            speciesStats,
            transformationWaitTimeSeconds,
            transformationOptions,
            attributeFusionEntity,
            specificFusionOptions,
            settings,
            readyToTransform = _readyToTransform,
            lastTransformationCheck = lastTransformationCheck,
        )
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