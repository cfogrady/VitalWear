package com.github.cfogrady.vitalwear.battle.data

import android.graphics.Bitmap

class BattleFirmwareSprites(
    val attackSprites: List<Bitmap>,
    val largeAttackSprites: List<Bitmap>,
    val battleBackground: Bitmap,
    val battleScreen: Bitmap,
    val partnerHpIcons: List<Bitmap>,
    val opponentHpIcons: List<Bitmap>,
    val hitIcons: List<Bitmap>,
)