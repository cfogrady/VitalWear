package com.github.cfogrady.vitalwear.character.data

import com.github.cfogrady.vitalwear.common.card.CardType
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntity
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
    val settings: CharacterSettingsEntity
) {
    private var _readyToTransform = MutableStateFlow<Optional<TransformationOption>>(Optional.empty())
    var readyToTransform : StateFlow<Optional<TransformationOption>> = _readyToTransform
    var activityIdx : Int = 1

    companion object {
        const val MAX_VITALS = 9999
        val DEFAULT_CHARACTER: BEMCharacter? = null
    }

    fun isBEM() : Boolean {
        return cardMetaEntity.cardType == CardType.BEM
    }

    fun cardName(): String {
        return cardMetaEntity.cardName
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

    fun popTransformationOption(): Optional<TransformationOption> {
        characterStats.timeUntilNextTransformation = transformationWaitTimeSeconds
        val option = readyToTransform.value
        _readyToTransform.tryEmit(Optional.empty())
        return option
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

    private var lastTransformationCheck = LocalDateTime.MIN

    fun prepCharacterTransformation() {
        lastTransformationCheck = LocalDateTime.now()
        val characterStats = characterStats
        val transformationOption = hasValidTransformation()
        if(transformationOption.isPresent) {
            _readyToTransform.tryEmit(transformationOption)
        } else {
            characterStats.timeUntilNextTransformation = transformationWaitTimeSeconds
        }
    }

    fun totalBp(): Int {
        return speciesStats.bp + characterStats.trainedBp
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

    fun addVitals(vitalChange: Int) {
        characterStats.vitals += vitalChange
        if(characterStats.vitals > MAX_VITALS) {
            characterStats.vitals = MAX_VITALS
        } else if(characterStats.vitals < 0) {
            characterStats.vitals = 0
        }
    }
}