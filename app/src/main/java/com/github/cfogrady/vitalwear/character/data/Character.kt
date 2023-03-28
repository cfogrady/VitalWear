package com.github.cfogrady.vitalwear.character.data

import android.graphics.Bitmap
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageType
import com.github.cfogrady.vb.dim.character.CharacterStats

class Character(
    val sprites: List<Bitmap>,
    val characterStats: CharacterEntity,
    val speciesStats : CharacterStats.CharacterStatsEntry,
    var readyToTransform: Boolean = false) {
    var spriteIdx : Int = 1
}