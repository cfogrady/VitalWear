package com.github.cfogrady.vitalwear.battle.data

import android.graphics.Bitmap
import android.util.Log
import com.github.cfogrady.vb.dim.card.BemCard
import com.github.cfogrady.vb.dim.card.Card
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vb.dim.character.BemCharacterStats
import com.github.cfogrady.vb.dim.character.CharacterStats
import com.github.cfogrady.vitalwear.battle.BattleActivity
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.Mood
import com.github.cfogrady.vitalwear.data.CardLoader
import com.github.cfogrady.vitalwear.data.Firmware
import com.github.cfogrady.vitalwear.data.FirmwareManager
import java.util.*

/**
 * This class is used to construct BattleModels for each instance of a battle
 */
class BattleModelFactory(private val cardLoader: CardLoader,
                         private val characterManager: CharacterManager,
                         private val firmwareManager: FirmwareManager
) {
    val random = Random()

    fun createBattleModel(): BattleModel {
        val partnerCharacter = characterManager.getActiveCharacter().value!!
        val card = cardLoader.loadCard(partnerCharacter.characterStats.cardFile)
        val firmware = firmwareManager.getFirmware().value!!
        val randomOpponent = loadRandomTarget(card)
        return BattleModel(
            characterManager,
            partnerCharacter,
            randomOpponent,
            getBackground(card, firmware),
            getReadyIcon(card, firmware),
            getGoIcon(card, firmware),
            firmware.partnerHpIcons,
            firmware.opponentHpIcons,
            random
        )
    }

    private fun loadBattleCharacter(card: Card<*, *, *, *, *, *>, slotId: Int): BattleCharacter {
        val speciesStats = card.characterStats.characterEntries[slotId]
        val battleStats = loadBattleStats(speciesStats)
        val battleSprites = loadBattleSprites(card, speciesStats, slotId)
        return BattleCharacter(battleStats, battleSprites)
    }

    private fun loadBattleStats(characterStats: CharacterStats.CharacterStatsEntry, mood: Mood = Mood.NORMAL): BattleStats {
        return BattleStats(characterStats.dp, characterStats.ap, characterStats.hp, characterStats.attribute, characterStats.type, mood)
    }

    private fun loadBattleSprites(card: Card<*, *, *, *, *, *>, characterStats: CharacterStats.CharacterStatsEntry, slotId: Int): BattleSprites {
        val characterSprites = cardLoader.bitmapsFromCard(card, slotId)
        val firmware = firmwareManager.getFirmware().value!!
        val smallAttackId = characterStats.smallAttackId
        val largeAttackId = characterStats.bigAttackId
        val projectileSprite = firmware.attackSprites[smallAttackId]
        val largeProjectileSprite = firmware.largeAttackSprites[largeAttackId]
        return BattleSprites(
            characterSprites[0],
            characterSprites.subList(1, 3),
            characterSprites[11],
            characterSprites[12],
            characterSprites[9],
            characterSprites[10],
            characterSprites[13],
            projectileSprite,
            largeProjectileSprite)
    }

    private fun loadRandomTarget(card: Card<*, *, *, *, *, *>): BattleCharacter {
        val character = characterManager.getActiveCharacter().value!!
        val opponentSpeciesIdx = assignRandomTargetFromCard(card, character.speciesStats.stage)
        return loadBattleCharacter(card, opponentSpeciesIdx)
    }

    private fun assignRandomTargetFromCard(card: Card<*, *, *, *, *, *>, activeStage: Int): Int {
        var roll = random.nextInt(100)
        while(roll >= 0) {
            for((index, species) in card.characterStats.characterEntries.withIndex()) {
                if(activeStage < 4) {
                    if(species.firstPoolBattleChance != DimReader.NONE_VALUE) {
                        roll -= species.firstPoolBattleChance
                    }
                } else if(activeStage < 6 || !(species is BemCharacterStats.BemCharacterStatEntry)) {
                    if(species.secondPoolBattleChance != DimReader.NONE_VALUE) {
                        roll -= species.secondPoolBattleChance
                    }
                } else {
                    if(species.thirdPoolBattleChance != DimReader.NONE_VALUE) {
                        roll -= species.thirdPoolBattleChance
                    }
                }
                if(roll < 0) {
                    return index
                }
            }
            Log.w(
                BattleActivity.TAG, "Rolled for an invalid species... This is a bad card image." +
                        "Running through the options until our roll is reduced to 0.")
        }
        throw java.lang.IllegalStateException("If we get here then we somehow failed to return the species that brought out role under 0.")
    }

    private fun getBackground(card: Card<*, *, *, *, *, *>, firmware: Firmware): Bitmap {
        if(card is BemCard) {
            return cardLoader.bitmapFromCardByIndex(card, CardLoader.BEM_BATTLE_BACKGROUND_IDX)
        }
        return firmware.battleBackground
    }

    private fun getReadyIcon(card: Card<*, *, *, *, *, *>, firmware: Firmware): Bitmap {
        if(card is BemCard) {
            return cardLoader.bitmapFromCardByIndex(card, CardLoader.BEM_READY_ICON_IDX)
        }
        return firmware.readyIcon
    }

    private fun getGoIcon(card: Card<*, *, *, *, *, *>, firmware: Firmware): Bitmap {
        if(card is BemCard) {
            return cardLoader.bitmapFromCardByIndex(card, CardLoader.BEM_GO_ICON_IDX)
        }
        return firmware.goIcon
    }
}