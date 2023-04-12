package com.github.cfogrady.vitalwear.battle

class Battle(
    val battleResult: BattleResult,
    val playerHits: List<BattleRound>,
    val enemyHits: List<BattleRound>
)