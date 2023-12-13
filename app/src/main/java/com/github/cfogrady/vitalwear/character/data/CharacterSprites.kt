package com.github.cfogrady.vitalwear.character.data

import android.graphics.Bitmap
import com.google.common.collect.Lists

data class CharacterSprites(
    val sprites: List<Bitmap>,
) {
    companion object {
        val EMPTY_CHARACTER_SPRITES = CharacterSprites(Lists.newArrayList())
        val NAME = 0
        val IDLE_1 = 1
        val IDLE_2 = 2
        val WALK_1 = 3
        val WALKT_2 = 4
        val RUN_1 = 5
        val RUN_2 = 6
        val TRAIN_1 = 7
        val TRAIN_2 = 8
        val WIN = 9
        val DOWN = 10
        val ATTACK = 11
        val DODGE = 12
        val SPLASH = 13
    }
}