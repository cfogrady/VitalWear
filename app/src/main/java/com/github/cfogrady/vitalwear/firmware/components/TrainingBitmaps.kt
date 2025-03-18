package com.github.cfogrady.vitalwear.firmware.components

import android.graphics.Bitmap

data class TrainingBitmaps(
    val squatText: Bitmap,
    val squatIcon: Bitmap,
    val crunchText: Bitmap,
    val crunchIcon: Bitmap,
    val punchText: Bitmap,
    val punchIcon: Bitmap,
    val dashText: Bitmap,
    val dashIcon: Bitmap,
    val trainingState: List<Bitmap>,
    val goodIcon: Bitmap,
    val greatIcon: Bitmap,
    val bpIcon: Bitmap,
    val hpIcon: Bitmap,
    val apIcon: Bitmap,
    val ppIcon: Bitmap,
    val mission: Bitmap,
    val clear: Bitmap,
    val failed: Bitmap,
)
