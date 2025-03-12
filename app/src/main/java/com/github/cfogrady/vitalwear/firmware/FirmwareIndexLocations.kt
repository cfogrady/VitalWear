package com.github.cfogrady.vitalwear.firmware

import com.github.cfogrady.vitalwear.firmware.components.AdventureSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.BattleSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.CharacterIconSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.MenuSpriteIndexes

data class FirmwareIndexLocations (
    val spritePackageLocation: Int,
    val spriteDimensionsLocation: Int,

    val characterIconSpriteIndexes: CharacterIconSpriteIndexes,
    val menuSpriteIndexes: MenuSpriteIndexes,
    val adventureSpriteIndexes: AdventureSpriteIndexes,
    val battleSpriteIndexes: BattleSpriteIndexes,

    val timerIcon: Int,
    val insertCardIcon: Int,
    val greenBackground: Int,
    val blackBackground: Int,
    val newBackgroundStartIdx: Int,
    val newBackgroundEndIdx: Int,
    val rayOfLightBackground: Int,
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
    val squatTextIdx: Int,
    val squatIconIdx: Int,
    val crunchTextIdx: Int,
    val crunchIconIdx: Int,
    val punchTextIdx: Int,
    val punchIconIdx: Int,
    val dashTextIdx: Int,
    val dashIconIdx: Int,
    val trainingStateStartIdx: Int,
    val trainingStateEndIdx: Int,
    val clearIdx: Int,
    val missionIdx: Int,
    val failedIdx: Int,
    val goodIdx: Int,
    val greatIdx: Int,
    val bpIdx: Int,
    val hpIdx: Int,
    val apIdx: Int,
    val ppIdx: Int,
    val weakPulse: Int,
    val strongPulse: Int,
    val star: Int,
    val transformationHourglass: Int,
    val transformationVitalsIcon: Int,
    val transformationBattlesIcon: Int,
    val transformationWinRatioIcon: Int,
    val transformationPpIcon: Int,
    val transformationSquatIcon: Int,
    val transformationLocked: Int,
)
