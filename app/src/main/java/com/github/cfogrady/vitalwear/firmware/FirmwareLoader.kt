package com.github.cfogrady.vitalwear.firmware

import com.github.cfogrady.vb.dim.sprite.BemSpriteReader
import com.github.cfogrady.vb.dim.sprite.SpriteData
import com.github.cfogrady.vb.dim.sprite.SpriteData.Sprite
import com.github.cfogrady.vb.dim.util.RelativeByteOffsetInputStream
import com.github.cfogrady.vitalwear.adventure.firmware.AdventureFirmwareSprites
import com.github.cfogrady.vitalwear.battle.data.BattleFirmwareSprites
import com.github.cfogrady.vitalwear.character.data.CharacterFirmwareSprites
import com.github.cfogrady.vitalwear.character.data.EmoteFirmwareSprites
import com.github.cfogrady.vitalwear.character.transformation.TransformationFirmwareSprites
import com.github.cfogrady.vitalwear.common.card.SpriteBitmapConverter
import com.github.cfogrady.vitalwear.main.MenuFirmwareSprites
import com.github.cfogrady.vitalwear.main.MenuFirmwareSprites.Companion.ADVENTURE_MENU_ICON
import com.github.cfogrady.vitalwear.main.MenuFirmwareSprites.Companion.CHARACTER_SELECTOR_ICON
import com.github.cfogrady.vitalwear.main.MenuFirmwareSprites.Companion.CONNECT_ICON
import com.github.cfogrady.vitalwear.main.MenuFirmwareSprites.Companion.SETTINGS_MENU_ICON
import com.github.cfogrady.vitalwear.main.MenuFirmwareSprites.Companion.SLEEP
import com.github.cfogrady.vitalwear.main.MenuFirmwareSprites.Companion.STATS_ICON_IDX
import com.github.cfogrady.vitalwear.main.MenuFirmwareSprites.Companion.STOP_ICON
import com.github.cfogrady.vitalwear.main.MenuFirmwareSprites.Companion.STOP_TEXT
import com.github.cfogrady.vitalwear.main.MenuFirmwareSprites.Companion.TRAINING_MENU_ICON
import com.github.cfogrady.vitalwear.main.MenuFirmwareSprites.Companion.WAKEUP
import com.github.cfogrady.vitalwear.training.TrainingFirmwareSprites
import com.google.common.collect.Lists
import timber.log.Timber
import java.io.InputStream

class FirmwareLoader(private val bemSpriteReader: BemSpriteReader, private val spriteBitmapConverter: SpriteBitmapConverter) {

    fun loadFirmware(firmwareIndexLocations: FirmwareIndexLocations, fileInput: InputStream): Firmware {
        val startFirmwareRead = System.currentTimeMillis()
        val input = RelativeByteOffsetInputStream(fileInput)
        input.readToOffset(firmwareIndexLocations.spriteDimensionsLocation)
        val dimensionsList = bemSpriteReader.readSpriteDimensions(input)
        input.readToOffset(firmwareIndexLocations.spritePackageLocation)
        val sprites = bemSpriteReader.getSpriteData(input, dimensionsList).sprites
        Timber.i("Time to initialize firmware: ${System.currentTimeMillis() - startFirmwareRead}")
        val loadingIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.timerIcon])
        val insertCardIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.insertCardIcon])
        val greenBackground = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.greenBackground])
        val orangeBackground = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.orangeBackground])
        val blueBackground = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.blueBackground])
        val battleBackground = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.battleBackground])

        val readyIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.readyIdx])
        val goIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.goIdx])

        val clearIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.clearIdx])
        val missionIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.missionIdx])
        val failedIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.failedIdx])

        return Firmware(
            characterFirmwareSprites(firmwareIndexLocations, sprites),
            MenuFirmwareSprites.menuFirmwareSprites(spriteBitmapConverter, sprites),
            AdventureFirmwareSprites.fromSprites(sprites, spriteBitmapConverter),
            battleFirmwareSprites(firmwareIndexLocations, sprites),
            trainingFirmwareSprites(firmwareIndexLocations, sprites),
            transformationFirmwareSprites(firmwareIndexLocations, sprites),
            loadingIcon,
            insertCardIcon,
            arrayListOf(greenBackground, orangeBackground, blueBackground, battleBackground),
            readyIcon,
            goIcon,
            missionIcon,
            clearIcon,
            failedIcon,
        )
    }

    private fun characterFirmwareSprites(firmwareIndexLocations: FirmwareIndexLocations, sprites: List<Sprite>): CharacterFirmwareSprites {
        val stepsIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.stepsIcon])
        val vitalsIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.vitalsIcon])
        val supportIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.supportIdx])
        return CharacterFirmwareSprites(stepsIcon, vitalsIcon, supportIcon, emoteFirmwareSprites(firmwareIndexLocations, sprites))
    }

    private fun menuFirmwareSprites(firmwareIndexLocations: FirmwareIndexLocations, firmwareSprites: List<Sprite>): MenuFirmwareSprites {
        val statsMenuIcon = spriteBitmapConverter.getBitmap(firmwareSprites[STATS_ICON_IDX])
        val characterSelectorIcon = spriteBitmapConverter.getBitmap(firmwareSprites[CHARACTER_SELECTOR_ICON])
        val trainingMenuIcon = spriteBitmapConverter.getBitmap(firmwareSprites[TRAINING_MENU_ICON])
        val adventureIcon = spriteBitmapConverter.getBitmap(firmwareSprites[ADVENTURE_MENU_ICON])
        val connectMenuIcon = spriteBitmapConverter.getBitmap(firmwareSprites[CONNECT_ICON])
        val stopText = spriteBitmapConverter.getBitmap(firmwareSprites[STOP_TEXT])
        val stopIcon = spriteBitmapConverter.getBitmap(firmwareSprites[STOP_ICON])
        val settingsIcon = spriteBitmapConverter.getBitmap(firmwareSprites[SETTINGS_MENU_ICON])
        val sleepIcon = spriteBitmapConverter.getBitmap(firmwareSprites[SLEEP])
        val wakeIcon = spriteBitmapConverter.getBitmap(firmwareSprites[WAKEUP])
        return MenuFirmwareSprites(statsMenuIcon, characterSelectorIcon, trainingMenuIcon, adventureIcon, stopText, stopIcon, connectMenuIcon, settingsIcon, sleepIcon, wakeIcon)
    }

    private fun emoteFirmwareSprites(firmwareIndexLocations: FirmwareIndexLocations, sprites: List<Sprite>) : EmoteFirmwareSprites {
        val happyEmote = spriteBitmapConverter.getBitmaps(sprites.subList(
            firmwareIndexLocations.happyEmoteStartIdx, firmwareIndexLocations.happyEmoteEndIdx
        ))
        val loseEmote = spriteBitmapConverter.getBitmaps(sprites.subList(
            firmwareIndexLocations.loseEmoteStartIdx, firmwareIndexLocations.loseEmoteEndIdx
        ))
        val sweatEmote = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.sweatEmoteIdx])
        val injuredEmote = spriteBitmapConverter.getBitmaps(sprites.subList(
            firmwareIndexLocations.injuredEmoteStartIdx, firmwareIndexLocations.injuredEmoteEndIdx
        ))
        val sleepEmote = spriteBitmapConverter.getBitmaps(sprites.subList(firmwareIndexLocations.sleepEmoteStartIdx, firmwareIndexLocations.sleepEmoteEndIdx))
        return EmoteFirmwareSprites(happyEmote, loseEmote, sweatEmote, injuredEmote, sleepEmote)
    }

    private fun battleFirmwareSprites(firmwareIndexLocations: FirmwareIndexLocations, sprites: List<Sprite>): BattleFirmwareSprites {
        val battleBackground = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.battleBackground])
        val attackSprites = spriteBitmapConverter.getBitmaps(sprites.subList(
            firmwareIndexLocations.smallAttackStartIdx, firmwareIndexLocations.smallAttackEndIdx
        ))
        val largeAttackSprites= spriteBitmapConverter.getBitmaps(sprites.subList(
            firmwareIndexLocations.bigAttackStartIdx, firmwareIndexLocations.bigAttackEndIdx
        ))
        val emptyHP = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.emptyHpIdx])
        val partnerHPIcons = Lists.newArrayList(emptyHP)
        partnerHPIcons.addAll(spriteBitmapConverter.getBitmaps(sprites.subList(
            firmwareIndexLocations.partnerHpStartIdx, firmwareIndexLocations.partnerHpEndIdx
        )))
        val opponentHPIcons = Lists.newArrayList(emptyHP)
        opponentHPIcons.addAll(spriteBitmapConverter.getBitmaps(sprites.subList(
            firmwareIndexLocations.opponentHpStartIdx, firmwareIndexLocations.opponentHpEndIdx
        )))
        val hitSprites = spriteBitmapConverter.getBitmaps(sprites.subList(firmwareIndexLocations.hitStartIdx, firmwareIndexLocations.hitEndIdx))
        val vitalRangeSprites = spriteBitmapConverter.getBitmaps(sprites.subList(
            firmwareIndexLocations.vitalsRangeStartIdx, firmwareIndexLocations.vitalsRangeEndIdx
        ))
        return BattleFirmwareSprites(attackSprites, largeAttackSprites, battleBackground, partnerHPIcons, opponentHPIcons, hitSprites, vitalRangeSprites)
    }

    private fun transformationFirmwareSprites(firmwareIndexLocations: FirmwareIndexLocations, sprites: List<Sprite>): TransformationFirmwareSprites {
        val blackBackground = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.blackBackground])
        val weakPulse = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.weakPulse])
        val strongPulse = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.strongPulse])
        val newBackgrounds = spriteBitmapConverter.getBitmaps(sprites.subList(
            firmwareIndexLocations.newBackgroundStartIdx, firmwareIndexLocations.newBackgroundEndIdx
        ))
        val rayOfLightBackground = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.rayOfLightBackground])
        return TransformationFirmwareSprites(
            blackBackground,
            weakPulse,
            strongPulse,
            newBackgrounds,
            rayOfLightBackground,
            spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.star]),
            spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.transformationHourglass]),
            spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.transformationVitalsIcon]),
            spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.transformationBattlesIcon]),
            spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.transformationWinRatioIcon]),
            spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.transformationPpIcon]),
            spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.transformationSquatIcon]),
            spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.transformationLocked]),
        )
    }

    private fun trainingFirmwareSprites(firmwareIndexLocations: FirmwareIndexLocations, sprites: List<Sprite>): TrainingFirmwareSprites {
        val squatText = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.squatTextIdx])
        val squatIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.squatIconIdx])
        val crunchText = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.crunchTextIdx])
        val crunchIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.crunchIconIdx])
        val punchText = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.punchTextIdx])
        val punchIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.punchIconIdx])
        val dashText = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.dashTextIdx])
        val dashIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.dashIconIdx])
        val trainingStateIcons = spriteBitmapConverter.getBitmaps(sprites.subList(
            firmwareIndexLocations.trainingStateStartIdx, firmwareIndexLocations.trainingStateEndIdx
        ))
        val goodIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.goodIdx])
        val greatIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.greatIdx])
        val bpIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.bpIdx])
        val hpIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.hpIdx])
        val apIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.apIdx])
        val ppIcon = spriteBitmapConverter.getBitmap(sprites[firmwareIndexLocations.ppIdx])
        return TrainingFirmwareSprites(
            squatText,
            squatIcon,
            crunchText,
            crunchIcon,
            punchText,
            punchIcon,
            dashText,
            dashIcon,
            trainingStateIcons,
            goodIcon,
            greatIcon,
            bpIcon,
            hpIcon,
            apIcon,
            ppIcon,
        )
    }
}