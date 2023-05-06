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
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
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
        val partnerCharacter = characterManager.getLiveCharacter().value!!
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
        val partnerCharacter = characterManager.getLiveCharacter().value!!
        val firmware = firmwareManager.getFirmware().value!!
        val battle = battleLogic.performBattle(preBattleModel)
        partnerCharacter.characterStats.totalBattles++
        partnerCharacter.characterStats.currentPhaseBattles++
        if(battle.battleResult == BattleResult.WIN) {
            partnerCharacter.characterStats.totalWins++
            partnerCharacter.characterStats.currentPhaseWins++
            partnerCharacter.characterStats.mood += 10
            if(partnerCharacter.characterStats.mood > 100) {
                partnerCharacter.characterStats.mood = 100
            }
        } else {
            partnerCharacter.characterStats.mood -= 10
            if(partnerCharacter.characterStats.mood < 0) {
                partnerCharacter.characterStats.mood = 0
            }
        }
        val vitalChange = vitalsForResult(partnerCharacter.speciesStats.stage, preBattleModel.opponent.battleStats.stage, battle.battleResult == BattleResult.WIN)
        partnerCharacter.addVitals(vitalChange)
        GlobalScope.launch(Dispatchers.Default) {
            characterManager.updateCharacterStats(partnerCharacter.characterStats, LocalDateTime.now())
        }
        return PostBattleModel(
            preBattleModel.partnerCharacter,
            preBattleModel.opponent,
            battle,
            preBattleModel.background,
            firmware.battleFirmwareSprites.partnerHpIcons,
            firmware.battleFirmwareSprites.opponentHpIcons,
            vitalChange
        )
    }

    private val vitalWinTable = arrayOf(
        intArrayOf(200, 300, 600, 1200, 1800, 2400), //phase 3 (index 2)
        intArrayOf(100, 300, 450, 700, 1400, 2100), //phase 4 (index 3)
        intArrayOf(20, 150, 400, 600, 800, 1600), //phase 5 (index 4)
        intArrayOf(20, 20, 400, 500, 750, 900), //phase 6 (index 5)
        intArrayOf(20, 20, 20, 500, 600, 800), //phase 7 (index 6)
        intArrayOf(20, 20, 20, 20, 600, 700), //phase 8 (index 7)
    )

    private val vitalLossTable = arrayOf(
        intArrayOf(-160, -100, -20, -20, -20, -20), //phase 3 (index 2)
        intArrayOf(-300, -240, -150, -20, -20, -20), //phase 4 (index 3)
        intArrayOf(-600, -450, -320, -400, -20, -20), //phase 5 (index 4)
        intArrayOf(-1200, -700, -600, -400, -500, -20), //phase 6 (index 5)
        intArrayOf(-1800, -1400, -800, -750, -480, -600), //phase 7 (index 6)
        intArrayOf(-2400, -2100, -1600, -900, -800, -560), //phase 8 (index 7)
    )

    fun vitalsForResult(partnerLevel: Int, opponentLevel: Int, win: Boolean): Int {
        return if(win) {
            vitalWinTable[partnerLevel-2][opponentLevel-2]
        } else {
            vitalLossTable[partnerLevel-2][opponentLevel-2]
        }
    }

    private fun loadBattleCharacter(card: Card<*, *, *, *, *, *>, slotId: Int): BattleCharacter {
        val speciesStats = card.characterStats.characterEntries[slotId]
        val battleStats = loadBattleStats(speciesStats)
        val battleSprites = loadBattleSprites(card, speciesStats, slotId)
        return BattleCharacter(battleStats, battleSprites)
    }

    private fun loadBattleStats(characterStats: CharacterStats.CharacterStatsEntry, mood: Mood = Mood.NORMAL): BattleStats {
        return BattleStats(characterStats.dp, characterStats.ap, characterStats.hp, 0, characterStats.attribute, characterStats.type, characterStats.stage, mood)
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
            character.speciesStats.stage,
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
            return firmware.battleFirmwareSprites.attackSprites[smallAttackId]
        }
        val cardSpriteIdx = (smallAttackId - 40) + 34
        return cardLoader.bitmapFromCardByIndex(card, cardSpriteIdx)
    }

    private fun getLargeAttackSprite(card: Card<*, *, *, *, *, *>, firmware: Firmware, largeAttackId: Int) : Bitmap {
        if(largeAttackId < 22) {
            return firmware.battleFirmwareSprites.largeAttackSprites[largeAttackId]
        }
        val cardSpriteIdx = (largeAttackId - 22) + 44
        return cardLoader.bitmapFromCardByIndex(card, cardSpriteIdx)
    }

    private fun getHitSprite(card: Card<*, *, *, *, *, *>, firmware: Firmware): List<Bitmap> {
        if(card is BemCard) {
            return cardLoader.bitmapsFromCardByIndexes(card, CARD_HIT_START_IDX, CARD_HIT_END_IDX)
        }
        return firmware.battleFirmwareSprites.hitIcons
    }

    private fun loadRandomTarget(card: Card<*, *, *, *, *, *>): BattleCharacter {
        val character = characterManager.getLiveCharacter().value!!
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
        return firmware.battleFirmwareSprites.battleBackground
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