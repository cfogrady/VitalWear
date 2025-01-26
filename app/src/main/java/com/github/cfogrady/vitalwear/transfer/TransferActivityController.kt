package com.github.cfogrady.vitalwear.transfer

import com.github.cfogrady.vitalwear.adventure.AdventureService
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.character.data.CharacterEntity
import com.github.cfogrady.vitalwear.character.data.CharacterState
import com.github.cfogrady.vitalwear.character.transformation.history.TransformationHistoryEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntity
import com.github.cfogrady.vitalwear.protos.Character
import com.github.cfogrady.vitalwear.settings.CharacterSettings
import java.time.LocalDateTime

class TransferActivityController(private val characterManager: CharacterManager, private val adventureService: AdventureService, private val cardMetaEntityDao: CardMetaEntityDao) {
    suspend fun getActiveCharacterProto(): Character? {
        val activeCharacter = characterManager.getCurrentCharacter()
        activeCharacter?.let {
            val maxAdventureIdxCompletedByCard = adventureService.getMaxAdventureIdxByCardCompletedForCharacter(it.characterStats.id)
            val transformationHistory = characterManager.getTransformationHistory(it.characterStats.id)
            return activeCharacter.toProto(transformationHistory, maxAdventureIdxCompletedByCard)
        }
        return null
    }

    fun getActiveCharacter(): VBCharacter? {
        return characterManager.getCurrentCharacter()
    }

    fun deleteActiveCharacter() {
        characterManager.deleteCurrentCharacter()
    }

    suspend fun receiveCharacter(character: Character): Boolean {
        val cardMetaEntity = cardMetaEntityDao.getByName(character.cardName)
        if(cardMetaEntity == null || cardMetaEntity.cardId != character.cardId) {
            // we can't handle this character
            return false
        }
        val characterId = characterManager.addCharacter(
            character.cardName,
            character.characterStats.toCharacterEntity(character.cardName),
            character.settings.toCharacterSettings(),
            character.transformationHistoryList.toTransformationHistoryEntities()
        )
        adventureService.addCharacterAdventures(characterId, character.maxAdventureCompletedByCardMap)
        return true
    }
}

fun VBCharacter.toProto(transformationHistory: List<TransformationHistoryEntity>, maxAdventureCompletedByCard: Map<String, Int>): Character {
    return Character.newBuilder()
        .setCardId(this.cardMeta.cardId)
        .setCardName(this.cardName())
        .setCharacterStats(this.characterStats.toProto())
        .setSettings(this.settings.toProto())
        .addAllTransformationHistory(transformationHistory.toProtoList())
        .putAllMaxAdventureCompletedByCard(maxAdventureCompletedByCard)
        .build()
}

fun List<TransformationHistoryEntity>.toProtoList(): List<Character.TransformationEvent> {
    val transformations = mutableListOf<Character.TransformationEvent>()
    for(transformation in this) {
        transformations.add(transformation.toProto())
    }
    return transformations
}

fun TransformationHistoryEntity.toProto(): Character.TransformationEvent {
    return Character.TransformationEvent.newBuilder()
        .setCardName(this.cardName)
        .setSlotId(this.speciesId)
        .setPhase(this.phase)
        .build()
}

fun List<Character.TransformationEvent>.toTransformationHistoryEntities(): List<TransformationHistoryEntity> {
    val transformations = mutableListOf<TransformationHistoryEntity>()
    for(transformation in this) {
        transformations.add(transformation.toTransformationHistoryEntitiy())
    }
    return transformations
}

fun Character.TransformationEvent.toTransformationHistoryEntitiy(): TransformationHistoryEntity {
    return TransformationHistoryEntity(
        characterId = 0,
        phase = this.phase,
        cardName = this.cardName,
        speciesId = this.slotId
    )
}

fun CharacterEntity.toProto(): Character.CharacterStats {
    return Character.CharacterStats.newBuilder()
        .setMood(this.mood)
        .setVitals(this.vitals)
        .setInjured(this.injured)
        .setSlotId(this.slotId)
        .setTotalWins(this.totalWins)
        .setAccumulatedDailyInjuries(this.accumulatedDailyInjuries)
        .setCurrentPhaseBattles(this.currentPhaseBattles)
        .setCurrentPhaseWins(this.currentPhaseWins)
        .setTimeUntilNextTransformation(this.timeUntilNextTransformation)
        .setTotalBattles(this.totalBattles)
        .setTrainedAp(this.trainedAp)
        .setTrainedBp(this.trainedBp)
        .setTrainedHp(this.trainedHp)
        .setTrainedPp(this.trainedPP)
        .build()
}

fun CharacterSettings.toProto(): Character.Settings {
    var builder = Character.Settings.newBuilder()
        .setTrainingInBackground(this.trainInBackground)
        .setAllowedBattlesValue(this.allowedBattles.ordinal)
    if(this.assumedFranchise != null) {
        builder = builder.setAssumedFranchise(this.assumedFranchise)
    }
    return builder.build()
}

fun Character.CharacterStats.toCharacterEntity(cardName: String): CharacterEntity {
    return CharacterEntity(
        id = 0,
        state = CharacterState.STORED,
        cardFile = cardName,
        slotId = this.slotId,
        lastUpdate = LocalDateTime.now(),
        vitals = this.vitals,
        trainingTimeRemainingInSeconds = this.trainingTimeRemainingInSeconds,
        hasTransformations = this.timeUntilNextTransformation > 0,
        timeUntilNextTransformation = this.timeUntilNextTransformation,
        trainedBp = this.trainedBp,
        trainedHp = this.trainedHp,
        trainedAp = this.trainedAp,
        trainedPP = this.trainedPp,
        injured = this.injured,
        lostBattlesInjured = 0,
        accumulatedDailyInjuries = this.accumulatedDailyInjuries,
        totalBattles = this.totalBattles,
        currentPhaseBattles = this.currentPhaseBattles,
        totalWins = this.totalWins,
        currentPhaseWins = this.currentPhaseWins,
        mood = this.mood,
        sleeping = false,
        dead = false,
    )
}

fun Character.Settings.toCharacterSettings(): CharacterSettings {
    return CharacterSettings(
        characterId = 0,
        trainInBackground = this.trainingInBackground,
        allowedBattles = CharacterSettings.AllowedBattles.entries[this.allowedBattlesValue],
        if(this.hasAssumedFranchise()) this.assumedFranchise else null
    )
}