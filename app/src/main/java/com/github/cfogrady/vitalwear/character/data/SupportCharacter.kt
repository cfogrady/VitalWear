package com.github.cfogrady.vitalwear.character.data

import android.graphics.Bitmap

class SupportCharacter(
    val cardName: String,
    val cardId: Int,
    val franchiseId: Int,
    val slotId: Int,
    val attribute: Int,
    val phase: Int,
    val totalBp: Int,
    val totalAp: Int,
    val totalHp: Int,
    val criticalAttackId: Int,
    val spriteDirName: String,
    val idleSprite: Bitmap,
    val idle2Sprite: Bitmap,
    val attackSprite: Bitmap) {
}