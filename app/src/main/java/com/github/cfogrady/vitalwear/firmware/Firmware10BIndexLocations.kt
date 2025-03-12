package com.github.cfogrady.vitalwear.firmware

import com.github.cfogrady.vitalwear.firmware.components.AdventureSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.CharacterIconSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.MenuSpriteIndexes

class Firmware10BIndexLocations() {
    companion object {

        val characterIconSpriteIndexesInstance = CharacterIconSpriteIndexes(
            stepsIconIdx = 55,
            vitalsIconIdx = 54,
            supportIconIdx = 408
        )

        val menuSpriteIndexesInstance = MenuSpriteIndexes(
            characterSelectorIcon = 267,
            trainingMenuIcon = 265,
            adventureMenuIcon = 266,
            statsIconIdx = 264,
            connectIcon = 268,
            stopIcon = 44,
            stopText = 169,
            settingsMenuIcon = 269,
            sleep = 76,
            wakeup = 75,
        )

        val adventureSpriteIndexes = AdventureSpriteIndexes(
            advImageIdx = 206,
            missionImageIdx = 204,
            nextMissionIdx = 61,
            stageIdx = 170,
            flagIdx = 56,
            hiddenIdx = 58,
            underlineIdx = 88,
        )

        val instance = FirmwareIndexLocations(
            spritePackageLocation = 0x80000,
            spriteDimensionsLocation = 0x90a4,
            characterIconSpriteIndexes = characterIconSpriteIndexesInstance,
            menuSpriteIndexes = menuSpriteIndexesInstance,
            adventureSpriteIndexes = adventureSpriteIndexes,
        )
    }
} {


    override val timerIcon = 81
    override val insertCardIcon = 38
    override val greenBackground = 0
    override val blackBackground = 1
    override val newBackgroundStartIdx = 2
    override val newBackgroundEndIdx = 5
    override val rayOfLightBackground = 5
    override val orangeBackground = 6
    override val blueBackground = 7
    override val battleBackground = 350
    override val smallAttackStartIdx = 310
    override val smallAttackEndIdx = 350
    override val bigAttackStartIdx = 288
    override val bigAttackEndIdx = 310
    override val readyIdx = 167
    override val goIdx = 168
    override val emptyHpIdx = 360
    override val partnerHpStartIdx = 361
    override val partnerHpEndIdx = 367
    override val opponentHpStartIdx = 354
    override val opponentHpEndIdx = 360
    override val hitStartIdx = 351
    override val hitEndIdx = 354
    override val happyEmoteStartIdx = 23
    override val happyEmoteEndIdx = 25
    override val loseEmoteStartIdx = 25
    override val loseEmoteEndIdx = 27
    override val sleepEmoteStartIdx = 27
    override val sleepEmoteEndIdx = 29
    override val sweatEmoteIdx = 29
    override val injuredEmoteStartIdx = 30
    override val injuredEmoteEndIdx = 32
    override val squatTextIdx = 182
    override val squatIconIdx = 247
    override val crunchTextIdx = 181
    override val crunchIconIdx = 246
    override val punchTextIdx = 180
    override val punchIconIdx = 245
    override val dashTextIdx = 183
    override val dashIconIdx = 249
    override val trainingStateStartIdx = 238
    override val trainingStateEndIdx = 245
    override val clearIdx = 175
    override val missionIdx = 205
    override val failedIdx = 176
    override val goodIdx = 184
    override val greatIdx = 185
    override val bpIdx = 409
    override val hpIdx = 410
    override val apIdx = 411
    override val ppIdx = 412
    override val vitalsRangeStartIdx = 281
    override val vitalsRangeEndIdx = 288
    override val weakPulse = 65
    override val strongPulse = 64
    override val star = 159
    override val transformationHourglass = 81
    override val transformationVitalsIcon = 82
    override val transformationBattlesIcon = 83
    override val transformationWinRatioIcon = 84
    override val transformationPpIcon = 85
    override val transformationSquatIcon = 78
    override val transformationLocked = 418

}