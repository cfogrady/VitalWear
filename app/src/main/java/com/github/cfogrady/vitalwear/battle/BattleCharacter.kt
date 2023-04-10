package com.github.cfogrady.vitalwear.battle

import android.graphics.Bitmap

class BattleStats(
    val bp: Int,
    val ap: Int,
    var hp: Int,
    val attribute: Int,
    val mood: Int
)

class BattleCharacter(
    val battleStats: BattleStats,
    val idleBitmaps: List<Bitmap>,
    val attackBitmap: Bitmap,
    val dodgeBitmap: Bitmap,
    val projectileBitmap: Bitmap,
    val strongProjectileBitmap: Bitmap,
) {
}