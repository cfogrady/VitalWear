package com.github.cfogrady.vitalwear.battle.data

class Battle(
    val battleResult: BattleResult,
    private val partnerHits: List<BattleRound>,
    private val enemyHits: List<BattleRound>
) {
    fun partnerHpAfterRound(round: Int): Int {
        return partnerHits[round].remainingHp
    }

    fun enmemyHpAfterRound(round: Int): Int {
        return enemyHits[round].remainingHp
    }

    fun partnerHitOnRound(round: Int): Boolean {
        return partnerHits[round].landedHit
    }

    fun enemyHitOnRound(round: Int): Boolean {
        return enemyHits[round].landedHit
    }
}