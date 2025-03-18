package com.github.cfogrady.vitalwear.battle

import android.content.Context
import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.firmware.Firmware
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DimBattleSpriteLoader(private val context: Context, private val firmware: Firmware, private val cardSpritesIO: CardSpritesIO, private val cardName: String, private val backgroundId: Int? = null) : BattleSpriteLoader {
    override suspend fun getBackground(): Bitmap {
        return if(backgroundId == null) {
            firmware.battleBitmaps.battleBackground
        } else {
            withContext(Dispatchers.IO) {
                cardSpritesIO.loadIndexedSprite(context, cardName, CardSpritesIO.BACKGROUNDS, backgroundId)
            }
        }
    }

    override suspend fun getReadyIcon(): Bitmap {
        return firmware.readyIcon
    }

    override suspend fun getGoIcon(): Bitmap {
        return firmware.goIcon
    }
}