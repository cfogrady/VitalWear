package com.github.cfogrady.vitalwear.battle.data

import android.graphics.Bitmap
import android.util.Log
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
                  val partnerAttack: Bitmap,
                  val partnerLargeAttack: Bitmap,
                  val partnerHits: List<Bitmap>,
                  val opponent: BattleCharacter,
                  val background: Bitmap,
                  val readySprite: Bitmap,
                  val goSprite: Bitmap,
                  val partnerRemainingHpSprites: List<Bitmap>,
                  val opponentRemainingHpSprites: List<Bitmap>,
                  val random: Random) {

    companion object {
        const val TAG = "BattleModel"
    }

    lateinit var battle: Battle
    var startingPartnerHp = 0
    var startingOpponentHp = 0

    // This honestly probably shouldn't be in the battle model...
    // There should probably be a PreBattleModel and a PostBattleModel with the BattleModelFactory
    // or new BattleService performing the actual battle and converstion
    fun performBattle() {
        if(this::battle.isInitialized) {
            return
        }
        var playerHp = (partnerCharacter.totalHp() * getVitalBonusPercent(partnerCharacter.characterStats.vitals)).toInt()
        startingPartnerHp = playerHp
        val playerAp = (partnerCharacter.totalAp() * getMoodBonusPercent(partnerCharacter.mood())).toInt()
        val playerHitRateChance = (100 * partnerCharacter.totalBp() / (partnerCharacter.totalBp() + opponent.battleStats.bp)) + getTypeHitrateAdjustment(partnerCharacter.speciesStats.attribute, opponent.battleStats.attribute)
        var opponentHp = opponent.battleStats.hp
        startingOpponentHp = opponentHp
        val opponentAp = (opponent.battleStats.ap * getMoodBonusPercent(opponent.battleStats.mood)).toInt()
        var round = 0
        val playerRounds = ArrayList<BattleRound>(5)
        val opponentRounds = ArrayList<BattleRound>(5)
        while(round < 5 && playerHp > 0 && opponentHp > 0) {
            val hitRoll = random.nextInt(100)
            if(hitRoll <= playerHitRateChance) {
                val critical = round == partnerCharacter.speciesStats.type
                val damage = if(critical) playerAp * 2 else playerAp
                Log.d(TAG, "Play hit on round $round. AP: $playerAp. Critical: $critical. OpponentHp Before $opponentHp and After ${opponentHp - damage}")
                opponentHp -= damage
                playerRounds.add(BattleRound(true, playerHp))
                opponentRounds.add(BattleRound(false, opponentHp))
            } else {
                val critical = round == opponent.battleStats.type
                val damage = if(critical) opponentAp * 2 else opponentAp
                Log.d(TAG, "Opponent hit on round $round. AP: $opponentAp. Critical: $critical. OpponentHp Before $playerHp and After ${playerHp - damage}")
                playerHp -= damage
                playerRounds.add(BattleRound(false, playerHp))
                opponentRounds.add(BattleRound(true, opponentHp))
            }
            round++
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
        battle = Battle(result, playerRounds, opponentRounds)
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

    fun opponentHpSprite(round: Int): Bitmap {
        val hp = battle.enmemyHpAfterRound(round)
        val hpPercent = hp.toFloat() / startingOpponentHp
        val hpSpriteIdx = if(hpPercent == 0.0f) 0 else 1 + (hpPercent*5).toInt()
        return opponentRemainingHpSprites[hpSpriteIdx]
    }

    fun partnerHpSprite(round: Int): Bitmap {
        val hp = battle.partnerHpAfterRound(round)
        val hpPercent = hp.toFloat() / startingPartnerHp
        val hpSpriteIdx = if(hpPercent == 0.0f) 0 else 1 + (hpPercent*5).toInt()
        return partnerRemainingHpSprites[hpSpriteIdx]
    }
}