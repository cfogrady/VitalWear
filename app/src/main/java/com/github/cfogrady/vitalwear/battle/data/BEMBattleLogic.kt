package com.github.cfogrady.vitalwear.battle.data

import com.github.cfogrady.vitalwear.character.data.Mood
import timber.log.Timber
import java.util.ArrayList
import java.util.Random

class BEMBattleLogic(private val random: Random) {
    fun performBattle(battle: PreBattleModel, preDeterminedHits: Array<Boolean> = emptyArray()): Battle {
        val partnerCharacter = battle.partnerCharacter
        val opponent = battle.opponent
        var playerHp = (partnerCharacter.hp() * getVitalBonusPercent(partnerCharacter.vitals())).toInt()
        val startingPartnerHp = playerHp
        val playerAp = (partnerCharacter.ap() * getMoodBonusPercent(partnerCharacter.mood())).toInt()
        val playerHitRateChance = (100 * partnerCharacter.bp() / (partnerCharacter.bp() + opponent.bp())) + getAttributeHitrateAdjustment(partnerCharacter.attribute(), opponent.attribute())
        var opponentHp = opponent.hp()
        val startingOpponentHp = opponentHp
        val opponentAp = (opponent.ap() * getMoodBonusPercent(opponent.mood())).toInt()
        var round = 0
        val playerRounds = ArrayList<BattleRound>(5)
        val opponentRounds = ArrayList<BattleRound>(5)
        var supportAttackRound: Int? = null
        while(round < 5 && playerHp > 0 && opponentHp > 0) {
            val hitRoll = random.nextInt(100)
            val playerHits = if(preDeterminedHits.size > round) preDeterminedHits[round] else hitRoll <= playerHitRateChance
            if(playerHits) {
                val critical = round == partnerCharacter.battleStats.type
                val damage = if(battle.supportCharacter != null &&
                    !critical &&
                    playerHp.toFloat()/startingPartnerHp.toFloat() <= 0.5f &&
                    supportAttackRound == null
                    ) {
                    supportAttackRound = round
                    (playerAp * 2) + (battle.supportCharacter.stats.totalAp * 2)
                }
                else if(critical)
                    playerAp * 2
                else playerAp
                Timber.d("Play hit on round $round. AP: $playerAp. Critical: $critical. OpponentHp Before $opponentHp and After ${opponentHp - damage}")
                opponentHp -= damage
                playerRounds.add(BattleRound(true, playerHp))
                opponentRounds.add(BattleRound(false, opponentHp))
            } else {
                val critical = round == opponent.battleStats.type
                val damage = if(critical) opponentAp * 2 else opponentAp
                Timber.d("Opponent hit on round $round. AP: $opponentAp. Critical: $critical. PlayerHp Before $playerHp and After ${playerHp - damage}")
                playerHp -= damage
                playerRounds.add(BattleRound(false, playerHp))
                opponentRounds.add(BattleRound(true, opponentHp))
            }
            round++
        }
        val result = if(playerHp >= opponentHp) BattleResult.WIN else BattleResult.LOSE
        return Battle(result, startingPartnerHp, startingOpponentHp, playerRounds, opponentRounds, supportAttackRound)
    }

    private fun getVitalBonusPercent(vitals: Int): Float {
        if(vitals > 7500) {
            return 1.1f
        } else if(vitals > 5000) {
            return 1.06f
        } else if (vitals > 2500) {
            return 1.03f
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

    private fun getAttributeHitrateAdjustment(playerAttribute: Int, opponentAttribute: Int): Float {
        return when(playerAttribute) {
            1 -> if(opponentAttribute == 2) 5f else if(opponentAttribute == 3) -5f else 0f
            2 -> if(opponentAttribute == 3) 5f else if(opponentAttribute == 1) -5f else 0f
            3 -> if(opponentAttribute == 1) 5f else if(opponentAttribute == 2) -5f else 0f
            else -> 0f
        }
    }
}