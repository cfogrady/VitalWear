package com.github.cfogrady.vitalwear.battle.data

import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.character.data.Mood

class BattleStats(
    val bp: Int,
    val ap: Int,
    var hp: Int,
    val attribute: Int,
    val type: Int,
    val mood: Mood
)

class BattleSprites(
    val nameBitmap: Bitmap,
    val idleBitmaps: List<Bitmap>,
    val attackBitmap: Bitmap,
    val dodgeBitmap: Bitmap,
    val winBitmap: Bitmap,
    val loseBitmap: Bitmap,
    val splashBitmap: Bitmap,
    val projectileBitmap: Bitmap,
    val strongProjectileBitmap: Bitmap,
)

class BattleCharacter(
    val battleStats: BattleStats,
    val battleSprites: BattleSprites
) {
}