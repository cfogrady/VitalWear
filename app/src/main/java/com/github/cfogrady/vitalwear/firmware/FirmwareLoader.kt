package com.github.cfogrady.vitalwear.firmware

import com.github.cfogrady.vb.dim.sprite.BemSpriteReader
import com.github.cfogrady.vb.dim.sprite.SpriteData.Sprite
import com.github.cfogrady.vb.dim.util.RelativeByteOffsetInputStream
import com.github.cfogrady.vitalwear.firmware.components.AdventureBitmaps
import com.github.cfogrady.vitalwear.firmware.components.BattleBitmaps
import com.github.cfogrady.vitalwear.firmware.components.CharacterIconBitmaps
import com.github.cfogrady.vitalwear.firmware.components.EmoteBitmaps
import com.github.cfogrady.vitalwear.firmware.components.MenuBitmaps
import com.github.cfogrady.vitalwear.firmware.components.TrainingBitmaps
import com.github.cfogrady.vitalwear.firmware.components.TransformationBitmaps
import com.github.cfogrady.vitalwear.common.card.SpriteBitmapConverter
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

        val emoteBitmaps = buildEmoteBitmaps(firmwareIndexLocations, sprites)

        return Firmware(
            CharacterIconBitmaps.build(firmwareIndexLocations.characterIconSpriteIndexes, emoteBitmaps, sprites, spriteBitmapConverter),
            MenuBitmaps.build(firmwareIndexLocations.menuSpriteIndexes, sprites, spriteBitmapConverter),
            AdventureBitmaps.fromSprites(sprites, spriteBitmapConverter),
            buildBattleBitmaps(firmwareIndexLocations.battleSpriteIndexes, sprites),
            buildTrainingBitmaps(firmwareIndexLocations.trainingSpriteIndexes, sprites),
            buildTransformationBitmaps(firmwareIndexLocations.transformationSpriteIndexes, sprites),
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

    private fun buildEmoteBitmaps(firmwareIndexLocations: FirmwareIndexLocations, sprites: List<Sprite>) : EmoteBitmaps {
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
        return EmoteBitmaps(happyEmote, loseEmote, sweatEmote, injuredEmote, sleepEmote)
    }

    private fun buildBattleBitmaps(battleSpriteIndexes: BattleSpriteIndexes, sprites: List<Sprite>): BattleBitmaps {
        val battleBackground = spriteBitmapConverter.getBitmap(sprites[battleSpriteIndexes.battleBackground])
        val attackSprites = spriteBitmapConverter.getBitmaps(sprites.subList(
            battleSpriteIndexes.smallAttackStartIdx, battleSpriteIndexes.smallAttackEndIdx
        ))
        val largeAttackSprites= spriteBitmapConverter.getBitmaps(sprites.subList(
            battleSpriteIndexes.bigAttackStartIdx, battleSpriteIndexes.bigAttackEndIdx
        ))
        val emptyHP = spriteBitmapConverter.getBitmap(sprites[battleSpriteIndexes.emptyHpIdx])
        val partnerHPIcons = Lists.newArrayList(emptyHP)
        partnerHPIcons.addAll(spriteBitmapConverter.getBitmaps(sprites.subList(
            battleSpriteIndexes.partnerHpStartIdx, battleSpriteIndexes.partnerHpEndIdx
        )))
        val opponentHPIcons = Lists.newArrayList(emptyHP)
        opponentHPIcons.addAll(spriteBitmapConverter.getBitmaps(sprites.subList(
            battleSpriteIndexes.opponentHpStartIdx, battleSpriteIndexes.opponentHpEndIdx
        )))
        val hitSprites = spriteBitmapConverter.getBitmaps(sprites.subList(battleSpriteIndexes.hitStartIdx, battleSpriteIndexes.hitEndIdx))
        val vitalRangeSprites = spriteBitmapConverter.getBitmaps(sprites.subList(
            battleSpriteIndexes.vitalsRangeStartIdx, battleSpriteIndexes.vitalsRangeEndIdx
        ))
        return BattleBitmaps(attackSprites, largeAttackSprites, battleBackground, partnerHPIcons, opponentHPIcons, hitSprites, vitalRangeSprites)
    }

    private fun buildTransformationBitmaps(transformationSpriteIndexes: TransformationSpriteIndexes, sprites: List<Sprite>): TransformationBitmaps {
        val blackBackground = spriteBitmapConverter.getBitmap(sprites[transformationSpriteIndexes.blackBackground])
        val weakPulse = spriteBitmapConverter.getBitmap(sprites[transformationSpriteIndexes.weakPulse])
        val strongPulse = spriteBitmapConverter.getBitmap(sprites[transformationSpriteIndexes.strongPulse])
        val newBackgrounds = spriteBitmapConverter.getBitmaps(sprites.subList(
            transformationSpriteIndexes.newBackgroundStartIdx, transformationSpriteIndexes.newBackgroundEndIdx
        ))
        val rayOfLightBackground = spriteBitmapConverter.getBitmap(sprites[transformationSpriteIndexes.rayOfLightBackground])
        return TransformationBitmaps(
            blackBackground,
            weakPulse,
            strongPulse,
            newBackgrounds,
            rayOfLightBackground,
            spriteBitmapConverter.getBitmap(sprites[transformationSpriteIndexes.star]),
            spriteBitmapConverter.getBitmap(sprites[transformationSpriteIndexes.hourglass]),
            spriteBitmapConverter.getBitmap(sprites[transformationSpriteIndexes.vitalsIcon]),
            spriteBitmapConverter.getBitmap(sprites[transformationSpriteIndexes.battlesIcon]),
            spriteBitmapConverter.getBitmap(sprites[transformationSpriteIndexes.winRatioIcon]),
            spriteBitmapConverter.getBitmap(sprites[transformationSpriteIndexes.ppIcon]),
            spriteBitmapConverter.getBitmap(sprites[transformationSpriteIndexes.squatIcon]),
            spriteBitmapConverter.getBitmap(sprites[transformationSpriteIndexes.locked])
        )
    }

    private fun buildTrainingBitmaps(trainingSpriteIndexes: TrainingSpriteIndexes, sprites: List<Sprite>): TrainingBitmaps {
        return TrainingBitmaps(
            spriteBitmapConverter.getBitmap(sprites[trainingSpriteIndexes.squatTextIdx]),
            spriteBitmapConverter.getBitmap(sprites[trainingSpriteIndexes.squatIconIdx]),
            spriteBitmapConverter.getBitmap(sprites[trainingSpriteIndexes.crunchTextIdx]),
            spriteBitmapConverter.getBitmap(sprites[trainingSpriteIndexes.crunchIconIdx]),
            spriteBitmapConverter.getBitmap(sprites[trainingSpriteIndexes.punchTextIdx]),
            spriteBitmapConverter.getBitmap(sprites[trainingSpriteIndexes.punchIconIdx]),
            spriteBitmapConverter.getBitmap(sprites[trainingSpriteIndexes.dashTextIdx]),
            spriteBitmapConverter.getBitmap(sprites[trainingSpriteIndexes.dashIconIdx]),
            spriteBitmapConverter.getBitmaps(sprites.subList(trainingSpriteIndexes.trainingStateStartIdx, trainingSpriteIndexes.trainingStateEndIdx)),
            spriteBitmapConverter.getBitmap(sprites[trainingSpriteIndexes.goodIdx]),
            spriteBitmapConverter.getBitmap(sprites[trainingSpriteIndexes.greatIdx]),
            spriteBitmapConverter.getBitmap(sprites[trainingSpriteIndexes.bpIdx]),
            spriteBitmapConverter.getBitmap(sprites[trainingSpriteIndexes.hpIdx]),
            spriteBitmapConverter.getBitmap(sprites[trainingSpriteIndexes.apIdx]),
            spriteBitmapConverter.getBitmap(sprites[trainingSpriteIndexes.ppIdx])
        )
    }
}