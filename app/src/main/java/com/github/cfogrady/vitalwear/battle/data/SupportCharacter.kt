package com.github.cfogrady.vitalwear.battle.data

import android.graphics.Bitmap

class SupportCharacter(
    val bp: Int,
    val ap: Int,
    val hp: Int,
    val idleBitmap: Bitmap,
    val attackBitmap: Bitmap,
    val splashBitmap: Bitmap,
    val strongProjectileBitmap: Bitmap,
) {
}