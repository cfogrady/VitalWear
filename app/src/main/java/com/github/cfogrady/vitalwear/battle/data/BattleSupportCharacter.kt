package com.github.cfogrady.vitalwear.battle.data

import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.character.data.SupportCharacter

class BattleSupportCharacter(
    val stats: SupportCharacter,
    val idleBitmap: Bitmap,
    val attackBitmap: Bitmap,
    val splashBitmap: Bitmap,
    val strongProjectileBitmap: Bitmap,
) {
}