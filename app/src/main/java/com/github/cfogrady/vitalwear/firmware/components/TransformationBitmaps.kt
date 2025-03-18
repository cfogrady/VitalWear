package com.github.cfogrady.vitalwear.firmware.components

import android.graphics.Bitmap

data class TransformationBitmaps(
    val blackBackground: Bitmap,
    val weakPulse: Bitmap,
    val strongPulse: Bitmap,
    val newBackgrounds: List<Bitmap>,
    val rayOfLightBackground: Bitmap,
    val star: Bitmap,
    val hourglass: Bitmap,
    val vitalsIcon: Bitmap,
    val battlesIcon: Bitmap,
    val winRatioIcon: Bitmap,
    val ppIcon: Bitmap,
    val squatIcon: Bitmap,
    val locked: Bitmap,
)
