package com.github.cfogrady.vitalwear.firmware

import com.github.cfogrady.vitalwear.firmware.components.AdventureSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.BattleSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.CharacterIconSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.EmoteSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.MenuSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.TrainingSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.TransformationSpriteIndexes

data class FirmwareSpriteIndexes (
    val spritePackageLocation: Int,
    val spriteDimensionsLocation: Int,

    val characterIconSpriteIndexes: CharacterIconSpriteIndexes,
    val menuSpriteIndexes: MenuSpriteIndexes,
    val adventureSpriteIndexes: AdventureSpriteIndexes,
    val battleSpriteIndexes: BattleSpriteIndexes,
    val trainingSpriteIndexes: TrainingSpriteIndexes,
    val transformationSpriteIndexes: TransformationSpriteIndexes,
    val emoteSpriteIndexes: EmoteSpriteIndexes,

    val insertCardIcon: Int,
    val greenBackground: Int,
    val orangeBackground: Int,
    val blueBackground: Int,

    val readyIdx: Int,
    val goIdx: Int,
)
