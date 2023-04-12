package com.github.cfogrady.vitalwear.battle

import android.graphics.Bitmap
import android.util.Log
import com.github.cfogrady.vb.dim.card.BemCard
import com.github.cfogrady.vb.dim.card.Card
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vb.dim.character.BemCharacterStats
import com.github.cfogrady.vb.dim.character.CharacterStats.CharacterStatsEntry
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.Mood
import com.github.cfogrady.vitalwear.data.CardLoader
import com.github.cfogrady.vitalwear.data.FirmwareManager
import com.github.cfogrady.vitalwear.data.SpriteBitmapConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.*

class BattleManager(val cardLoader: CardLoader,
                    val characterManager: CharacterManager,
                    val firmwareManager: FirmwareManager) {
    val random = Random()

    fun loadBattleCharacter(slotId: Int, card: Card<*, *, *, *, *, *>): BattleCharacter {
        val speciesStats = card.characterStats.characterEntries[slotId]
        val battleStats = loadBattleStats(speciesStats)
        val battleSprites = loadBattleSprites(speciesStats, slotId, card)
        return BattleCharacter(battleStats, battleSprites)
    }

    fun loadBattleStats(characterStats: CharacterStatsEntry, mood: Mood = Mood.NORMAL): BattleStats {
        return BattleStats(characterStats.dp, characterStats.ap, characterStats.hp, characterStats.attribute, characterStats.type, mood)
    }

    fun loadBattleSprites(characterStats: CharacterStatsEntry, slotId: Int, card: Card<*, *, *, *, *, *>): BattleSprites {
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

    fun loadRandomTarget(card: Card<*, *, *, *, *, *>): BattleCharacter {
        val character = characterManager.getActiveCharacter().value!!
        val opponentSpeciesIdx = assignRandomTargetFromCard(character.speciesStats.stage, card)
        return loadBattleCharacter(opponentSpeciesIdx, card)
    }

    fun getBackground(card: Card<*, *, *, *, *, *>): Bitmap {
        if(card is BemCard) {
            return cardLoader.bitmapFromCardByIndex(card, CardLoader.BEM_BATTLE_BACKGROUND_IDX)
        }
        return firmwareManager.getFirmware().value!!.battleBackground
    }

    private fun assignRandomTargetFromCard(activeStage: Int, card: Card<*, *, *, *, *, *>): Int {
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

    fun performBattle(opponent: BattleCharacter): Battle {
        val playerCharacter = characterManager.getActiveCharacter().value!!
        var playerHp = (playerCharacter.totalHp() * getVitalBonusPercent(playerCharacter.characterStats.vitals)).toInt()
        val playerAp = (playerCharacter.totalAp() * getMoodBonusPercent(playerCharacter.mood())).toInt()
        val playerHitRateChance = (100 * playerCharacter.totalBp() / (playerCharacter.totalBp() + opponent.battleStats.bp)) + getTypeHitrateAdjustment(playerCharacter.speciesStats.attribute, opponent.battleStats.attribute)
        var opponentHp = opponent.battleStats.hp
        val opponentAp = (opponent.battleStats.ap * getMoodBonusPercent(opponent.battleStats.mood)).toInt()
        var round = 0
        val playerRounds = ArrayList<BattleRound>(5)
        val opponentRounds = ArrayList<BattleRound>(5)
        while(round < 5 && playerHp > 0 && opponentHp > 0) {
            val hitRoll = random.nextInt(100)
            if(hitRoll <= playerHitRateChance) {
                if(round == playerCharacter.speciesStats.type) {
                    opponentHp -= (playerAp*2)
                } else {
                    opponentHp -= playerAp
                }
                playerRounds.add(BattleRound(true, playerHp))
                opponentRounds.add(BattleRound(false, opponentHp))
            } else {
                if(round == opponent.battleStats.type) {
                    playerHp -= (opponentAp * 2)
                } else {
                    playerHp -= opponentAp
                }
                playerRounds.add(BattleRound(false, playerHp))
                opponentRounds.add(BattleRound(true, opponentHp))
            }
        }
        val result = if(playerHp >= opponentHp) BattleResult.WIN else BattleResult.LOSE
        playerCharacter.characterStats.totalBattles++
        playerCharacter.characterStats.currentPhaseBattles++
        if(result == BattleResult.WIN) {
            playerCharacter.characterStats.totalWins++
            playerCharacter.characterStats.currentPhaseWins++
        }
        GlobalScope.launch(Dispatchers.Default) {
            characterManager.updateCharacterStats(playerCharacter.characterStats, LocalDateTime.now())
        }
        return Battle(result, playerRounds, opponentRounds)
    }

    private fun getVitalBonusPercent(vitals: Int): Float {
        if(vitals > 2500) {
            return 1.03f
        } else if(vitals > 5000) {
            return 1.06f
        } else if (vitals > 7500) {
            return 1.1f
        }
        return 1.0f
    }

    private fun getMoodBonusPercent(mood: Mood): Float {
        return when(mood) {
            Mood.NORMAL -> 1.0f
            Mood.GOOD -> 1.05f
            Mood.BAD -> 0.95f
        }
    }

    private fun getTypeHitrateAdjustment(playerAttribute: Int, opponentAttribute: Int): Float {
        return when(playerAttribute) {
            1 -> if(opponentAttribute == 2) 5f else if(opponentAttribute == 3) -5f else 0f
            2 -> if(opponentAttribute == 3) 5f else if(opponentAttribute == 1) -5f else 0f
            3 -> if(opponentAttribute == 1) 5f else if(opponentAttribute == 2) -5f else 0f
            else -> 0f
        }
    }
}