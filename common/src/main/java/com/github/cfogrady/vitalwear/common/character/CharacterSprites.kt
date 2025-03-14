package com.github.cfogrady.vitalwear.common.character

import android.graphics.Bitmap

data class CharacterSprites(
    val sprites: List<Bitmap>,
) {
    companion object {
        const val NAME = 0
        const val IDLE_1 = 1
        const val IDLE_2 = 2
        const val WALK_1 = 3
        const val WALK_2 = 4
        const val RUN_1 = 5
        const val RUN_2 = 6
        const val TRAIN_1 = 7
        const val TRAIN_2 = 8
        const val WIN = 9
        const val DOWN = 10
        const val ATTACK = 11
        const val DODGE = 12
        const val SPLASH = 13
    }
}