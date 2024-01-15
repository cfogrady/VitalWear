package com.github.cfogrady.vitalwear.battle

import android.content.Context
import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO

class CardBattleSpriteLoader(private val context: Context, private val cardSpritesIO: CardSpritesIO, private val cardName: String, private val backgroundId: Int? = null) :  BattleSpriteLoader {
    override fun getBackground(): Bitmap {
        return cardSpritesIO.loadIndexedSprite(context, cardName, CardSpritesIO.BACKGROUNDS, backgroundId ?: 1)
    }

    override fun getReadyIcon(): Bitmap {
        return cardSpritesIO.loadCardSprite(context, cardName, CardSpritesIO.READY)
    }

    override fun getGoIcon(): Bitmap {
        return cardSpritesIO.loadCardSprite(context, cardName, CardSpritesIO.GO)
    }
}