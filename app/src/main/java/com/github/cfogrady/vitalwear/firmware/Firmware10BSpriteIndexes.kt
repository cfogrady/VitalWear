package com.github.cfogrady.vitalwear.firmware

import com.github.cfogrady.vitalwear.firmware.components.AdventureSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.BattleSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.CharacterIconSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.EmoteSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.MenuSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.TrainingSpriteIndexes
import com.github.cfogrady.vitalwear.firmware.components.TransformationSpriteIndexes

class Firmware10BSpriteIndexes {
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

        val battleSpriteIndexes = BattleSpriteIndexes(
            attackStartIdx = 310,
            attackEndIdx = 350,
            criticalAttackStartIdx = 288,
            criticalAttackEndIdx = 310,
            battleBackgroundIdx = 350,
            emptyHpIdx = 360,
            partnerHpStartIdx = 361,
            partnerHpEndIdx = 367,
            opponentHpStartIdx = 354,
            opponentHpEndIdx = 360,
            hitStartIdx = 351,
            hitEndIdx = 354,
            vitalsRangeStartIdx = 281,
            vitalsRangeEndIdx = 288,
        )

        val trainingSpriteIndexes = TrainingSpriteIndexes(
            squatTextIdx = 182,
            squatIconIdx = 247,
            crunchTextIdx = 181,
            crunchIconIdx = 246,
            punchTextIdx = 180,
            punchIconIdx = 245,
            dashTextIdx = 183,
            dashIconIdx = 249,
            trainingStateStartIdx = 238,
            trainingStateEndIdx = 245,
            goodIdx = 184,
            greatIdx = 185,
            bpIdx = 409,
            hpIdx = 410,
            apIdx = 411,
            ppIdx = 412,
            mission = 205,
            clear = 175,
            failed = 176,
        )

        val transformationSpriteIndexes = TransformationSpriteIndexes(
            blackBackground = 1,
            weakPulse = 65,
            strongPulse = 64,
            newBackgroundStartIdx = 2,
            newBackgroundEndIdx = 5,
            rayOfLightBackground = 5,
            star = 159,
            hourglass = 81,
            vitalsIcon = 82,
            battlesIcon = 83,
            winRatioIcon = 84,
            ppIcon = 85,
            squatIcon = 78,
            locked = 418,
        )

        val emoteSpriteIndexes = EmoteSpriteIndexes(
            happyEmoteStartIdx = 23,
            happyEmoteEndIdx = 25,
            loseEmoteStartIdx = 25,
            loseEmoteEndIdx = 27,
            sweatEmoteIdx = 29,
            injuredEmoteStartIdx = 30,
            injuredEmoteEndIdx = 32,
            sleepEmoteStartIdx = 27,
            sleepEmoteEndIdx = 29
        )

        val instance = FirmwareSpriteIndexes(
            spritePackageLocation = 0x80000,
            spriteDimensionsLocation = 0x90a4,
            characterIconSpriteIndexes = characterIconSpriteIndexesInstance,
            menuSpriteIndexes = menuSpriteIndexesInstance,
            adventureSpriteIndexes = adventureSpriteIndexes,
            battleSpriteIndexes = battleSpriteIndexes,
            trainingSpriteIndexes = trainingSpriteIndexes,
            transformationSpriteIndexes = transformationSpriteIndexes,
            emoteSpriteIndexes = emoteSpriteIndexes,
            insertCardIcon = 38,
            greenBackground = 0,
            orangeBackground = 6,
            blueBackground = 7,
            readyIdx = 167,
            goIdx = 168
        )
    }
}