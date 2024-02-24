package com.github.cfogrady.vitalwear.data

import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.character.data.BEMCharacter

enum class GameState {
    IDLE,
    TRAINING,
    ADVENTURE;

    fun bitmaps(character: VBCharacter): List<Bitmap> {
        return when(this) {
            IDLE -> {
                character.characterSprites.sprites.subList(1, 3)
            }
            TRAINING -> {
                character.characterSprites.sprites.subList(7, 9)
            }
            ADVENTURE -> character.characterSprites.sprites.subList(3, 5)
        }
    }
}