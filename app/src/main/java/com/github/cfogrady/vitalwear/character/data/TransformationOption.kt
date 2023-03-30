package com.github.cfogrady.vitalwear.character.data

import android.graphics.Bitmap

class TransformationOption(
    val sprite: Bitmap,
    val slotId: Int,
    val requiredVitals: Int,
    val requiredPp: Int,
    val requiredBattles: Int,
    val requiredWinRatio: Int,
) {
}