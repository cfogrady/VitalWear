package com.github.cfogrady.vitalwear.firmware.components

import android.graphics.Bitmap
import com.github.cfogrady.vb.dim.sprite.SpriteData.Sprite
import com.github.cfogrady.vitalwear.common.card.SpriteBitmapConverter

class MenuBitmaps(
    val statsIcon: Bitmap,
    val characterSelectorIcon: Bitmap,
    val trainingIcon: Bitmap,
    val adventureIcon: Bitmap,
    val stopText: Bitmap,
    val stopIcon: Bitmap,
    val connectIcon: Bitmap,
    val settingsIcon: Bitmap,
    val sleepIcon: Bitmap,
    val wakeIcon: Bitmap,
) {
    companion object {
        fun build(menuSpriteIndexes: MenuSpriteIndexes, firmwareSprites: List<Sprite>, spriteBitmapConverter: SpriteBitmapConverter): MenuBitmaps {
            val statsMenuIcon = spriteBitmapConverter.getBitmap(firmwareSprites[menuSpriteIndexes.statsIconIdx])
            val characterSelectorIcon = spriteBitmapConverter.getBitmap(firmwareSprites[menuSpriteIndexes.characterSelectorIcon])
            val trainingMenuIcon = spriteBitmapConverter.getBitmap(firmwareSprites[menuSpriteIndexes.trainingMenuIcon])
            val adventureIcon = spriteBitmapConverter.getBitmap(firmwareSprites[menuSpriteIndexes.adventureMenuIcon])
            val connectMenuIcon = spriteBitmapConverter.getBitmap(firmwareSprites[menuSpriteIndexes.connectIcon])
            val stopText = spriteBitmapConverter.getBitmap(firmwareSprites[menuSpriteIndexes.stopText])
            val stopIcon = spriteBitmapConverter.getBitmap(firmwareSprites[menuSpriteIndexes.stopIcon])
            val settingsIcon = spriteBitmapConverter.getBitmap(firmwareSprites[menuSpriteIndexes.settingsMenuIcon])
            val sleepIcon = spriteBitmapConverter.getBitmap(firmwareSprites[menuSpriteIndexes.sleep])
            val wakeIcon = spriteBitmapConverter.getBitmap(firmwareSprites[menuSpriteIndexes.wakeup])
            return MenuBitmaps(statsMenuIcon, characterSelectorIcon, trainingMenuIcon, adventureIcon, stopText, stopIcon, connectMenuIcon, settingsIcon, sleepIcon, wakeIcon)
        }
    }
}
