package com.github.cfogrady.vitalwear.data

import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.character.data.BEMCharacter

enum class GameState {
    IDLE,
    TRAINING,
    ADVENTURE;

    fun bitmaps(characterSprites: List<Bitmap>): List<Bitmap> {
        return when(this) {
            IDLE -> {
                characterSprites.subList(1, 3)
            }
            TRAINING -> {
                characterSprites.subList(7, 9)
            }
            ADVENTURE -> characterSprites.subList(3, 5)
        }
    }
}