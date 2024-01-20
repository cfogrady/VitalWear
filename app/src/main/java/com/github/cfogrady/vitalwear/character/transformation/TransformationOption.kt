package com.github.cfogrady.vitalwear.character.transformation

import android.graphics.Bitmap

class TransformationOption(
    val sprite: Bitmap,
    val slotId: Int,
    val requiredVitals: Int,
    val requiredPp: Int,
    val requiredBattles: Int,
    val requiredWinRatio: Int,
    val requiredAdventureCompleted: Int?,
    val isSecret: Boolean,
) {
    fun toExpectedTransformation(): ExpectedTransformation {
        return ExpectedTransformation(slotId)
    }
}