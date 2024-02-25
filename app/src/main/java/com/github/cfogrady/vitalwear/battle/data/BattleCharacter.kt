package com.github.cfogrady.vitalwear.battle.data

import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.Mood
import com.github.cfogrady.vitalwear.common.card.CardType

class BattleStats(
    val bp: Int,
    val ap: Int,
    val hp: Int,
    val vitals: Int,
    val attribute: Int,
    val type: Int,
    val stage: Int,
    val mood: Mood
)

class BattleSprites(
    val nameBitmap: Bitmap,
    val idleBitmap: Bitmap,
    val attackBitmap: Bitmap,
    val dodgeBitmap: Bitmap,
    val winBitmap: Bitmap,
    val loseBitmap: Bitmap,
    val splashBitmap: Bitmap,
    val projectileBitmap: Bitmap,
    val strongProjectileBitmap: Bitmap,
    val hits: List<Bitmap>,
)

class BattleCharacter(
    val cardName: String,
    val cardType: CardType,
    val battleStats: BattleStats,
    val battleSprites: BattleSprites
) {
    fun bp(): Int {
        return battleStats.bp
    }

    fun ap(): Int {
        return battleStats.ap
    }

    fun hp(): Int {
        return battleStats.hp
    }

    fun vitals(): Int {
        return battleStats.vitals
    }

    fun mood(): Mood {
        return battleStats.mood
    }

    fun attribute(): Int {
        return battleStats.attribute
    }
}
