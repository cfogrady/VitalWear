package com.github.cfogrady.vitalwear.data

import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.character.data.BEMCharacter

enum class GameState {
    IDLE,
    WALKING,
    RUNNING,
    TRAINING;

    fun bitmaps(character: BEMCharacter): List<Bitmap> {
        return when(this) {
            IDLE -> {
                character.characterSprites.sprites.subList(1, 3)
            }
            WALKING -> {
                character.characterSprites.sprites.subList(3, 5)
            }
            RUNNING -> {
                character.characterSprites.sprites.subList(5, 7)
            }
            TRAINING -> {
                character.characterSprites.sprites.subList(7, 9)
            }
        }
    }
}