package com.github.cfogrady.vitalwear.character.data

import com.github.cfogrady.vitalwear.card.CardMeta
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.character.transformation.ExpectedTransformation
import com.github.cfogrady.vitalwear.character.transformation.TransformationOption
import com.github.cfogrady.vitalwear.common.card.db.AttributeFusionEntity
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntity
import com.github.cfogrady.vitalwear.common.card.db.SpecificFusionEntity
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.settings.CharacterSettings
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime

class BEMCharacter(
    cardMeta: CardMeta,
    characterSprites: CharacterSprites,
    characterStats: CharacterEntity,
    speciesStats : SpeciesEntity,
    transformationWaitTimeSeconds: Long,
    transformationOptions: List<TransformationOption>,
    attributeFusionEntity: AttributeFusionEntity?,
    specificFusionOptions: List<SpecificFusionEntity>,
    settings: CharacterSettings,
    readyToTransform: MutableStateFlow<ExpectedTransformation?> = MutableStateFlow<ExpectedTransformation?>(null),
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

    companion object {
        const val TAG = "BEMCharacter"
        const val MAX_VITALS = 9999
        val DEFAULT_CHARACTER: BEMCharacter? = null
    }

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
        ): BEMCharacter {
        return BEMCharacter(
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
        return speciesStats.bp + characterStats.trainedBp.coerceAtMost(999)
    }
    override fun totalAp(): Int {
        return speciesStats.ap + characterStats.trainedAp.coerceAtMost(999)
    }
    override fun totalHp(): Int {
        return speciesStats.hp + characterStats.trainedHp.coerceAtMost(999)
    }
}