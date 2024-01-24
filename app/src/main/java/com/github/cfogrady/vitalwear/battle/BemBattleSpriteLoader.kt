package com.github.cfogrady.vitalwear.battle

import android.content.Context
import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BemBattleSpriteLoader(private val context: Context, private val cardSpritesIO: CardSpritesIO, private val cardName: String, private val backgroundId: Int? = null) :  BattleSpriteLoader {
    override suspend fun getBackground(): Bitmap {
        return withContext(Dispatchers.IO) {
            cardSpritesIO.loadIndexedSprite(context, cardName, CardSpritesIO.BACKGROUNDS, backgroundId ?: 1)
        }
    }

    override suspend fun getReadyIcon(): Bitmap {
        return withContext(Dispatchers.IO) {
            cardSpritesIO.loadCardSprite(context, cardName, CardSpritesIO.READY)
        }
    }

    override suspend fun getGoIcon(): Bitmap {
        return withContext(Dispatchers.IO) {
            cardSpritesIO.loadCardSprite(context, cardName, CardSpritesIO.GO)
        }
    }
}