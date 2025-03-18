package com.github.cfogrady.vitalwear.firmware.components

data class BattleSpriteIndexes(
    val attackStartIdx: Int,
    val attackEndIdx: Int,
    val criticalAttackStartIdx: Int,
    val criticalAttackEndIdx: Int,
    val battleBackgroundIdx: Int,
    val emptyHpIdx: Int,
    val partnerHpStartIdx: Int,
    val partnerHpEndIdx: Int,
    val opponentHpStartIdx: Int,
    val opponentHpEndIdx: Int,
    val hitStartIdx: Int,
    val hitEndIdx: Int,
    val vitalsRangeStartIdx: Int,
    val vitalsRangeEndIdx: Int,
) {
}