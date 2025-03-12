package com.github.cfogrady.vitalwear.firmware.components

import com.github.cfogrady.vb.dim.sprite.SpriteData.Sprite
import com.github.cfogrady.vitalwear.common.card.SpriteBitmapConverter

data class EmoteSpriteIndexes(

) {
    fun getEmoteFirmwareSprites(spriteBitmapConverter: SpriteBitmapConverter, allFirmwareSprites: List<Sprite>): EmoteBitmaps {
        val stepsIcon = spriteBitmapConverter.getBitmap(allFirmwareSprites[stepsIconIdx])
        val vitalsIcon = spriteBitmapConverter.getBitmap(allFirmwareSprites[vitalsIconIdx])
        val supportIcon = spriteBitmapConverter.getBitmap(allFirmwareSprites[supportIconIdx])
        return CharacterIconBitmaps(stepsIcon, vitalsIcon, supportIcon, emoteFirmwareSprites(firmwareIndexLocations, sprites))
    }
}