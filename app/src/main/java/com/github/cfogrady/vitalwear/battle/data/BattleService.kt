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
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.Mood
import com.github.cfogrady.vitalwear.data.CardLoader
import com.github.cfogrady.vitalwear.data.Firmware
import com.github.cfogrady.vitalwear.data.FirmwareManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.*

/**
 * This class is used to construct BattleModels for each instance of a battle
 */
class BattleService(private val cardLoader: CardLoader,
                    private val characterManager: CharacterManager,
                    private val firmwareManager: FirmwareManager,
                    private val battleLogic: BEMBattleLogic,
                    private val random: Random,
) {
    companion object {
        const val CARD_HIT_START_IDX = 15
        const val CARD_HIT_END_IDX = 18
    }

    fun createBattleModel(): PreBattleModel {
        val partnerCharacter = characterManager.getActiveCharacter().value!!
        val card = cardLoader.loadCard(partnerCharacter.characterStats.cardFile)
        val firmware = firmwareManager.getFirmware().value!!
        val partnerBattleCharacter = battleCharacterFromBemCharacter(card, partnerCharacter)
        val randomOpponent = loadRandomTarget(card)
        return PreBattleModel(
            partnerBattleCharacter,
            randomOpponent,
            getBackground(card, firmware),
            getReadyIcon(card, firmware),
            getGoIcon(card, firmware),
        )
    }

    fun performBattle(preBattleModel: PreBattleModel): PostBattleModel {
        val partnerCharacter = characterManager.getActiveCharacter().value!!
        val firmware = firmwareManager.getFirmware().value!!
        val battle = battleLogic.performBattle(preBattleModel)
        partnerCharacter.characterStats.totalBattles++
        partnerCharacter.characterStats.currentPhaseBattles++
        if(battle.battleResult == BattleResult.WIN) {
            partnerCharacter.characterStats.totalWins++
            partnerCharacter.characterStats.currentPhaseWins++
        }
        GlobalScope.launch(Dispatchers.Default) {
            characterManager.updateCharacterStats(partnerCharacter.characterStats, LocalDateTime.now())
        }
        return PostBattleModel(
            preBattleModel.partnerCharacter,
            preBattleModel.opponent,
            battle,
            preBattleModel.background,
            firmware.partnerHpIcons,
            firmware.opponentHpIcons,
        )
    }

    private fun loadBattleCharacter(card: Card<*, *, *, *, *, *>, slotId: Int): BattleCharacter {
        val speciesStats = card.characterStats.characterEntries[slotId]
        val battleStats = loadBattleStats(speciesStats)
        val battleSprites = loadBattleSprites(card, speciesStats, slotId)
        return BattleCharacter(battleStats, battleSprites)
    }

    private fun loadBattleStats(characterStats: CharacterStats.CharacterStatsEntry, mood: Mood = Mood.NORMAL): BattleStats {
        return BattleStats(characterStats.dp, characterStats.ap, characterStats.hp, 0, characterStats.attribute, characterStats.type, mood)
    }

    private fun loadBattleSprites(card: Card<*, *, *, *, *, *>, characterStats: CharacterStats.CharacterStatsEntry, slotId: Int): BattleSprites {
        val characterSprites = cardLoader.bitmapsFromCard(card, slotId)
        val firmware = firmwareManager.getFirmware().value!!
        val smallAttackId = characterStats.smallAttackId
        val largeAttackId = characterStats.bigAttackId
        val projectileSprite = getSmallAttackSprite(card, firmware, smallAttackId)
        val largeProjectileSprite = getLargeAttackSprite(card, firmware, largeAttackId)
        return BattleSprites(
            characterSprites[0],
            characterSprites[1],
            characterSprites[11],
            characterSprites[12],
            characterSprites[9],
            characterSprites[10],
            characterSprites[13],
            projectileSprite,
            largeProjectileSprite,
            getHitSprite(card, firmware)
        )
    }

    private fun battleCharacterFromBemCharacter(card: Card<*, *, *, *, *, *>, character: BEMCharacter): BattleCharacter {
        val firmware = firmwareManager.getFirmware().value!!
        val smallAttackId = character.speciesStats.smallAttackId
        val largeAttackId = character.speciesStats.bigAttackId
        val projectileSprite = getSmallAttackSprite(card, firmware, smallAttackId)
        val largeProjectileSprite = getLargeAttackSprite(card, firmware, largeAttackId)
        val battleStats = BattleStats(
            character.totalBp(),
            character.totalAp(),
            character.totalHp(),
            character.characterStats.vitals,
            character.speciesStats.attribute,
            character.speciesStats.type,
            character.mood(),
        )
        val battleSprites = BattleSprites(
            character.sprites[0],
            character.sprites[1],
            character.sprites[11],
            character.sprites[12],
            character.sprites[9],
            character.sprites[10],
            character.sprites[13],
            projectileSprite,
            largeProjectileSprite,
            getHitSprite(card, firmware)
        )
        return BattleCharacter(battleStats, battleSprites)
    }

    private fun getSmallAttackSprite(card: Card<*, *, *, *, *, *>, firmware: Firmware, smallAttackId: Int) : Bitmap {
        if(smallAttackId < 40) {
            return firmware.attackSprites[smallAttackId]
        }
        val cardSpriteIdx = (smallAttackId - 40) + 34
        return cardLoader.bitmapFromCardByIndex(card, cardSpriteIdx)
    }

    private fun getLargeAttackSprite(card: Card<*, *, *, *, *, *>, firmware: Firmware, largeAttackId: Int) : Bitmap {
        if(largeAttackId < 22) {
            return firmware.largeAttackSprites[largeAttackId]
        }
        val cardSpriteIdx = (largeAttackId - 22) + 44
        return cardLoader.bitmapFromCardByIndex(card, cardSpriteIdx)
    }

    private fun getHitSprite(card: Card<*, *, *, *, *, *>, firmware: Firmware): List<Bitmap> {
        if(card is BemCard) {
            return cardLoader.bitmapsFromCardByIndexes(card, CARD_HIT_START_IDX, CARD_HIT_END_IDX)
        }
        return firmware.hitIcons
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