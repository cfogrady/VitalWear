package com.github.cfogrady.vitalwear.firmware.components

import android.graphics.Bitmap
import com.github.cfogrady.vb.dim.sprite.SpriteData.Sprite
import com.github.cfogrady.vitalwear.common.card.SpriteBitmapConverter

class CharacterIconBitmaps(
    val stepsIcon: Bitmap,
    val vitalsIcon: Bitmap,
    val supportIcon: Bitmap,
    val emoteBitmaps: EmoteBitmaps,
) {
    companion object {
        fun build(indexLocations: CharacterIconSpriteIndexes, emoteBitmaps: EmoteBitmaps, sprites: List<Sprite>, spriteBitmapConverter: SpriteBitmapConverter): CharacterIconBitmaps {
            val stepsIcon = spriteBitmapConverter.getBitmap(sprites[indexLocations.stepsIconIdx])
            val vitalsIcon = spriteBitmapConverter.getBitmap(sprites[indexLocations.vitalsIconIdx])
            val supportIcon = spriteBitmapConverter.getBitmap(sprites[indexLocations.supportIconIdx])
            return CharacterIconBitmaps(stepsIcon, vitalsIcon, supportIcon, emoteBitmaps)
        }
    }
}
