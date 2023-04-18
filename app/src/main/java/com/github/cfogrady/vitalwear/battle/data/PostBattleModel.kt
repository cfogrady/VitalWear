package com.github.cfogrady.vitalwear.battle.data

import android.graphics.Bitmap

/**
 * This class is meant to model a single instance of a battle.
 */
class PostBattleModel(val partnerCharacter: BattleCharacter,
                      val opponent: BattleCharacter,
                      val battle: Battle,
                      val background: Bitmap,
                      val partnerRemainingHpSprites: List<Bitmap>,
                      val opponentRemainingHpSprites: List<Bitmap>) {


    fun opponentHpSprite(round: Int): Bitmap {
        val hp = battle.enmemyHpAfterRound(round)
        val hpPercent = hp.toFloat() / battle.startingEnemyHp
        val hpSpriteIdx = if(hpPercent == 0.0f) 0 else 1 + (hpPercent*5).toInt()
        return opponentRemainingHpSprites[hpSpriteIdx]
    }

    fun partnerHpSprite(round: Int): Bitmap {
        val hp = battle.partnerHpAfterRound(round)
        val hpPercent = hp.toFloat() / battle.startingPartnerHp
        val hpSpriteIdx = if(hpPercent == 0.0f) 0 else 1 + (hpPercent*5).toInt()
        return partnerRemainingHpSprites[hpSpriteIdx]
    }
}