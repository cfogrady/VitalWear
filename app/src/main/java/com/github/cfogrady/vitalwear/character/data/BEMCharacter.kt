package com.github.cfogrady.vitalwear.character.data

import android.util.Log
import com.github.cfogrady.vitalwear.character.VBCharacter
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

class BEMCharacter(
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

    companion object {
        const val TAG = "BEMCharacter"
        const val MAX_VITALS = 9999
        val DEFAULT_CHARACTER: BEMCharacter? = null
    }

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
        ): BEMCharacter {
        return BEMCharacter(
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
        return speciesStats.bp + characterStats.trainedBp.coerceAtMost(999)
    }
    override fun totalAp(): Int {
        return speciesStats.ap + characterStats.trainedAp.coerceAtMost(999)
    }
    override fun totalHp(): Int {
        return speciesStats.hp + characterStats.trainedHp.coerceAtMost(999)
    }
}