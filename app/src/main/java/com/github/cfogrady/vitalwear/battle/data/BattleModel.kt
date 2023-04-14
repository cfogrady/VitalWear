package com.github.cfogrady.vitalwear.battle.data

import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.Mood
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.*

/**
 * This class is meant to model a single instance of a battle.
 */
class BattleModel(private val characterManager: CharacterManager,
                  val partnerCharacter: BEMCharacter,
                  val opponent: BattleCharacter,
                  val background: Bitmap,
                  val readySprite: Bitmap,
                  val goSprite: Bitmap,
                  val random: Random) {

    fun performBattle(): Battle {
        var playerHp = (partnerCharacter.totalHp() * getVitalBonusPercent(partnerCharacter.characterStats.vitals)).toInt()
        val playerAp = (partnerCharacter.totalAp() * getMoodBonusPercent(partnerCharacter.mood())).toInt()
        val playerHitRateChance = (100 * partnerCharacter.totalBp() / (partnerCharacter.totalBp() + opponent.battleStats.bp)) + getTypeHitrateAdjustment(partnerCharacter.speciesStats.attribute, opponent.battleStats.attribute)
        var opponentHp = opponent.battleStats.hp
        val opponentAp = (opponent.battleStats.ap * getMoodBonusPercent(opponent.battleStats.mood)).toInt()
        var round = 0
        val playerRounds = ArrayList<BattleRound>(5)
        val opponentRounds = ArrayList<BattleRound>(5)
        while(round < 5 && playerHp > 0 && opponentHp > 0) {
            val hitRoll = random.nextInt(100)
            if(hitRoll <= playerHitRateChance) {
                if(round == partnerCharacter.speciesStats.type) {
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
        partnerCharacter.characterStats.totalBattles++
        partnerCharacter.characterStats.currentPhaseBattles++
        if(result == BattleResult.WIN) {
            partnerCharacter.characterStats.totalWins++
            partnerCharacter.characterStats.currentPhaseWins++
        }
        GlobalScope.launch(Dispatchers.Default) {
            characterManager.updateCharacterStats(partnerCharacter.characterStats, LocalDateTime.now())
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