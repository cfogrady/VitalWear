package com.github.cfogrady.vitalwear.firmware.components

import android.graphics.Bitmap
import com.github.cfogrady.vb.dim.sprite.SpriteData.Sprite
import com.github.cfogrady.vitalwear.common.card.SpriteBitmapConverter

class AdventureBitmaps(
    val advImage: Bitmap,
    val missionImage: Bitmap,
    val nextMissionImage: Bitmap,
    val stageImage: Bitmap,
    val flagImage: Bitmap,
    val hiddenImage: Bitmap,
    val underlineImage: Bitmap,
) {
    companion object {
        const val ADV_IMAGE_IDX = 206
        const val MISSION_IMAGE_IDX = 204
        const val NEXT_MISSION_IDX = 61
        const val STAGE_IDX = 170
        const val FLAG_IDX = 56
        const val HIDDEN_IDX = 58
        const val UNDERLINE_IDX = 88

        fun fromSprites(sprites: List<Sprite>, spriteToBitmapConverter: SpriteBitmapConverter): AdventureBitmaps {
            return AdventureBitmaps(
                spriteToBitmapConverter.getBitmap(sprites[ADV_IMAGE_IDX]),
                spriteToBitmapConverter.getBitmap(sprites[MISSION_IMAGE_IDX]),
                spriteToBitmapConverter.getBitmap(sprites[NEXT_MISSION_IDX]),
                spriteToBitmapConverter.getBitmap(sprites[STAGE_IDX]),
                spriteToBitmapConverter.getBitmap(sprites[FLAG_IDX]),
                spriteToBitmapConverter.getBitmap(sprites[HIDDEN_IDX]),
                spriteToBitmapConverter.getBitmap(sprites[UNDERLINE_IDX]),
            )
        }
    }
}