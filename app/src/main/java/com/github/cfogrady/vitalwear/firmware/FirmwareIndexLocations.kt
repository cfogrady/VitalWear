package com.github.cfogrady.vitalwear.firmware

import com.github.cfogrady.vitalwear.firmware.components.AdventureSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.BattleSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.CharacterIconSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.MenuSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.TrainingSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.TransformationSpriteIndexes

data class FirmwareIndexLocations (
    val spritePackageLocation: Int,
    val spriteDimensionsLocation: Int,

    val characterIconSpriteIndexes: CharacterIconSpriteIndexes,
    val menuSpriteIndexes: MenuSpriteIndexes,
    val adventureSpriteIndexes: AdventureSpriteIndexes,
    val battleSpriteIndexes: BattleSpriteIndexes,
    val trainingSpriteIndexes: TrainingSpriteIndexes,
    val transformationSpriteIndexes: TransformationSpriteIndexes,

    val timerIcon: Int,
    val insertCardIcon: Int,
    val greenBackground: Int,
    val blackBackground: Int,
    val orangeBackground: Int,
    val blueBackground: Int,

    val readyIdx: Int,
    val goIdx: Int,

    val happyEmoteStartIdx: Int,
    val happyEmoteEndIdx: Int, // AlsoWin
    val loseEmoteStartIdx: Int,
    val loseEmoteEndIdx: Int,
    val sleepEmoteStartIdx: Int,
    val sleepEmoteEndIdx: Int,
    val sweatEmoteIdx: Int,
    val injuredEmoteStartIdx: Int,
    val injuredEmoteEndIdx: Int,
    val clearIdx: Int,
    val missionIdx: Int,
    val failedIdx: Int,
)
