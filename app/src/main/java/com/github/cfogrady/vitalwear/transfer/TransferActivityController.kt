package com.github.cfogrady.vitalwear.transfer

import com.github.cfogrady.vitalwear.adventure.AdventureService
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.character.data.CharacterEntity
import com.github.cfogrady.vitalwear.protos.Character
import com.github.cfogrady.vitalwear.settings.CharacterSettings

class TransferActivityController(private val characterManager: CharacterManager, private val adventureService: AdventureService) {
    suspend fun getActiveCharacterProto(): Character? {
        val activeCharacter = characterManager.getCurrentCharacter()
        activeCharacter?.let {
            val maxAdventureIdxCompletedByCard = adventureService.getMaxAdventureIdxByCardCompletedForCharacter(it.characterStats.id)
            return activeCharacter.toProto(maxAdventureIdxCompletedByCard)
        }
        return null
    }

    fun getActiveCharacter(): VBCharacter? {
        return characterManager.getCurrentCharacter()
    }

    fun deleteActiveCharacter() {
        characterManager.deleteCurrentCharacter()
    }
}

fun VBCharacter.toProto(maxAdventureCompletedByCard: Map<String, Int>): Character {
    return Character.newBuilder()
        .setCardId(this.cardMeta.cardId)
        .setCardName(this.cardName())
        .setCharacterStats(this.characterStats.toProto())
        .setSettings(this.settings.toProto())
        .putAllMaxAdventureCompletedByCard(maxAdventureCompletedByCard)
        .build()
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