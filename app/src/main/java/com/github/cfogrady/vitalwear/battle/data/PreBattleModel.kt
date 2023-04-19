package com.github.cfogrady.vitalwear.battle.data

import android.graphics.Bitmap

class PreBattleModel(
    val partnerCharacter: BattleCharacter,
    val opponent: BattleCharacter,
    val background: Bitmap,
    val readySprite: Bitmap,
    val goSprite: Bitmap,
) {
}