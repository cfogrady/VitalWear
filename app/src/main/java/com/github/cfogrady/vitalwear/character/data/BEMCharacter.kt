package com.github.cfogrady.vitalwear.character.data

import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.card.CardType
import com.github.cfogrady.vitalwear.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.card.db.SpeciesEntity
import java.time.LocalDateTime
import java.util.*

class BEMCharacter(
    val cardMetaEntity: CardMetaEntity,
    val characterSprites: CharacterSprites,
    val characterStats: CharacterEntity,
    val speciesStats : SpeciesEntity,
    val transformationWaitTimeSeconds: Long,
    val transformationOptions: List<TransformationOption>,
    var readyToTransform: Optional<TransformationOption> = Optional.empty()) {
    var activityIdx : Int = 1

    companion object {
        const val MAX_VITALS = 9999
        private val DEFAULT_STATS = CharacterEntity(-1, CharacterState.SYNCED, "NONE", 0, LocalDateTime.MIN, 0, 0, false, 0, 0, 0, 0, 0, false, 0, 0, 0, 0, 0, 0, 0, false)
        val DEFAULT_CHARACTER = BEMCharacter(CardMetaEntity.EMPTY_CARD_META, CharacterSprites.EMPTY_CHARACTER_SPRITES, DEFAULT_STATS, SpeciesEntity.EMPTY_SPECIES_ENTITY, 0, emptyList())
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

    var lastTransformationCheck = LocalDateTime.MIN

    fun prepCharacterTransformation() {
        lastTransformationCheck = LocalDateTime.now()
        val characterStats = characterStats
        val transformationOption = hasValidTransformation()
        if(transformationOption.isPresent) {
            readyToTransform = transformationOption
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