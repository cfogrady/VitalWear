package com.github.cfogrady.vitalwear.menu

import android.graphics.Bitmap
import com.github.cfogrady.vb.dim.sprite.SpriteData
import com.github.cfogrady.vitalwear.common.card.SpriteBitmapConverter

class MenuFirmwareSprites(
    val statsIcon: Bitmap,
    val characterSelectorIcon: Bitmap,
    val trainingIcon: Bitmap,
    val stopIcon: Bitmap,
    val connectIcon: Bitmap,
)
{
    companion object {
        const val CHARACTER_SELECTOR_ICON = 267
        const val TRAINING_MENU_ICON = 265
        const val STATS_ICON_IDX = 264
        const val CONNECT_ICON = 268
        const val STOP_ICON = 44

        fun menuFirmwareSprites(spriteBitmapConverter: SpriteBitmapConverter, firmwareSprites: List<SpriteData.Sprite>): MenuFirmwareSprites {
            val statsMenuIcon = spriteBitmapConverter.getBitmap(firmwareSprites[STATS_ICON_IDX])
            val characterSelectorIcon = spriteBitmapConverter.getBitmap(firmwareSprites[CHARACTER_SELECTOR_ICON])
            val trainingMenuIcon = spriteBitmapConverter.getBitmap(firmwareSprites[TRAINING_MENU_ICON])
            val connectMenuIcon = spriteBitmapConverter.getBitmap(firmwareSprites[CONNECT_ICON])
            val stopIcon = spriteBitmapConverter.getBitmap(firmwareSprites[STOP_ICON])
            return MenuFirmwareSprites(statsMenuIcon, characterSelectorIcon, trainingMenuIcon, stopIcon, connectMenuIcon)
        }
    }
}