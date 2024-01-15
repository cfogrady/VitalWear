package com.github.cfogrady.vitalwear.battle.data

data class BattleCharacterInfo(
    val cardName: String,
    val characterId: Int,
    val bp: Int?,
    val hp: Int?,
    val ap: Int?,
    val attack: Int?,
    val critical: Int?,
    val battleBackground: Int?
) {
}