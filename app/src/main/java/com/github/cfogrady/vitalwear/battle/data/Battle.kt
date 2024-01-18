package com.github.cfogrady.vitalwear.battle.data

class Battle(
    val battleResult: BattleResult,
    val startingPartnerHp: Int,
    val startingEnemyHp: Int,
    private val partnerHits: List<BattleRound>,
    private val enemyHits: List<BattleRound>,
    val supportAttackRound: Int?
) {
    fun partnerHpAfterRound(round: Int): Int {
        return partnerHits[round].remainingHp.coerceAtLeast(0)
    }

    fun enmemyHpAfterRound(round: Int): Int {
        return enemyHits[round].remainingHp.coerceAtLeast(0)
    }

    fun partnerLandedHitOnRound(round: Int): Boolean {
        return partnerHits[round].landedHit
    }

    fun enemyLandedHitOnRound(round: Int): Boolean {
        return enemyHits[round].landedHit
    }

    fun finalRound(): Int {
        return partnerHits.size - 1
    }
}