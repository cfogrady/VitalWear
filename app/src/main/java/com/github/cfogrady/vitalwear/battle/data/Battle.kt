package com.github.cfogrady.vitalwear.battle.data

class Battle(
    val battleResult: BattleResult,
    val playerHits: List<BattleRound>,
    val enemyHits: List<BattleRound>
)