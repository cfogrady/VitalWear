package com.github.cfogrady.vitalwear.firmware.components

import android.graphics.Bitmap

class BattleBitmaps(
    val attackSprites: List<Bitmap>,
    val largeAttackSprites: List<Bitmap>,
    val battleBackground: Bitmap,
    val partnerHpIcons: List<Bitmap>,
    val opponentHpIcons: List<Bitmap>,
    val hitIcons: List<Bitmap>,
    val vitalsRangeIcons: List<Bitmap>,
)