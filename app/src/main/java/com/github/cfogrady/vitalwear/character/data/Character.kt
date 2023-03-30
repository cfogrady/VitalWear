package com.github.cfogrady.vitalwear.character.data

import android.graphics.Bitmap
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageType
import com.github.cfogrady.vb.dim.character.BemCharacterStats
import com.github.cfogrady.vb.dim.character.CharacterStats
import java.time.LocalDateTime
import java.util.*

class Character(
    val sprites: List<Bitmap>,
    val characterStats: CharacterEntity,
    val speciesStats : CharacterStats.CharacterStatsEntry,
    val transformationWaitTimeSeconds: Long,
    val transformationOptions: List<TransformationOption>,
    var readyToTransform: Optional<TransformationOption> = Optional.empty()) {
    var spriteIdx : Int = 1

    fun isBEM() : Boolean {
        return speciesStats is BemCharacterStats.BemCharacterStatEntry
    }
}