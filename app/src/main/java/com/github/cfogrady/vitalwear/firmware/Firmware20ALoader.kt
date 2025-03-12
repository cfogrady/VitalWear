package com.github.cfogrady.vitalwear.firmware

import com.github.cfogrady.vb.dim.sprite.BemSpriteReader
import com.github.cfogrady.vb.dim.sprite.SpriteData.Sprite
import com.github.cfogrady.vb.dim.util.RelativeByteOffsetInputStream
import com.github.cfogrady.vitalwear.firmware.components.AdventureBitmaps
import com.github.cfogrady.vitalwear.firmware.components.BattleBitmaps
import com.github.cfogrady.vitalwear.firmware.components.CharacterIconBitmaps
import com.github.cfogrady.vitalwear.firmware.components.EmoteBitmaps
import com.github.cfogrady.vitalwear.character.transformation.TransformationFirmwareSprites
import com.github.cfogrady.vitalwear.common.card.SpriteBitmapConverter
import com.github.cfogrady.vitalwear.firmware.components.MenuBitmaps
import com.github.cfogrady.vitalwear.training.TrainingFirmwareSprites
import com.google.common.collect.Lists
import timber.log.Timber
import java.io.InputStream

class Firmware20ALoader(private val bemSpriteReader: BemSpriteReader, private val spriteBitmapConverter: SpriteBitmapConverter) {
    companion object {
        const val SPRITE_DIMENSIONS_LOCATION = 0x9d62
        const val SPRITE_PACKAGE_LOCATION = 0x80000

        const val TIMER_ICON = 81
        const val INSERT_CARD_ICON = 38
        const val GREEN_BACKGROUND = 0
        const val BLACK_BACKGROUND = 1
        const val NEW_BACKGROUND_START_IDX = 2
        const val NEW_BACKGROUND_END_IDX = 5
        const val RAY_OF_LIGHT_BACKGROUND = 5
        const val ORANGE_BACKGROUND = 6
        const val BLUE_BACKGROUND = 7
        const val STEPS_ICON = 55
        const val VITALS_ICON = 54
        const val BATTLE_BACKGROUND = 345
        const val SMALL_ATTACK_START_IDX = 305
        const val SMALL_ATTACK_END_IDX = 345
        const val BIG_ATTACK_START_IDX = 283
        const val BIG_ATTACK_END_IDX = 305
        const val READY_IDX = 167
        const val GO_IDX = 168
        const val EMPTY_HP_IDX = 355
        const val PARTNER_HP_START_IDX = 356
        const val PARTNER_HP_END_IDX = 362
        const val OPPONENT_HP_START_IDX = 349
        const val OPPONENT_HP_END_IDX = 355
        const val HIT_START_IDX = 346
        const val HIT_END_IDX = 349
        const val HAPPY_EMOTE_START_IDX = 23 //also win
        const val HAPPY_EMOTE_END_IDX = 25
        const val LOSE_EMOTE_START_IDX = 25
        const val LOSE_EMOTE_END_IDX = 27
        const val SLEEP_EMOTE_START_IDX = 27
        const val SLEEP_EMOTE_END_IDX = 29
        const val SWEAT_EMOTE_IDX = 29
        const val INJURED_EMOTE_START_IDX = 30
        const val INJURED_EMOTE_END_IDX = 32
        const val SQUAT_TEXT_IDX = 182
        const val SQUAT_ICON_IDX = 247
        const val CRUNCH_TEXT_IDX = 181
        const val CRUNCH_ICON_IDX = 246
        const val PUNCH_TEXT_IDX = 180
        const val PUNCH_ICON_IDX = 245
        const val DASH_TEXT_IDX = 183
        const val DASH_ICON_IDX = 248
        const val TRAINING_STATE_START_IDX = 238
        const val TRAINING_STATE_END_IDX = 245
        const val CLEAR_IDX = 175
        const val MISSION_IDX = 205
        const val FAILED_IDX = 176
        const val GOOD_IDX = 184
        const val GREAT_IDX = 185
        const val SUPPORT_IDX = 403
        const val BP_IDX = 404
        const val HP_IDX = 405
        const val AP_IDX = 406
        const val PP_IDX = 407
        const val VITALS_RANGE_START_IDX = 276
        const val VITALS_RANGE_END_IDX = 283
        const val WEAK_PULSE = 65
        const val STRONG_PULSE = 64
        const val STAR = 159
        const val TRANSFORMATION_HOURGLASS = 81
        const val TRANSFORMATION_VITALS_ICON = 82
        const val TRANSFORMATION_BATTLES_ICON = 83
        const val TRANSFORMATION_WIN_RATIO_ICON = 84
        const val TRANSFORMATION_PP_ICON = 85
        const val TRANSFORMATION_SQUAT_ICON = 78
        const val TRANSFORMATION_LOCKED = 413
    }

    fun load20AFirmware(fileInput: InputStream): Firmware {
        val startFirmwareRead = System.currentTimeMillis()
        val input = RelativeByteOffsetInputStream(fileInput)
        input.readToOffset(Firmware10BLoader.SPRITE_DIMENSIONS_LOCATION)
        val dimensionsList =
            bemSpriteReader.readSpriteDimensions(input)
        input.readToOffset(Firmware10BLoader.SPRITE_PACKAGE_LOCATION)
        val sprites = bemSpriteReader.getSpriteData(input, dimensionsList).sprites
        Timber.i("Time to initialize firmware: ${System.currentTimeMillis() - startFirmwareRead}")
        val loadingIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.TIMER_ICON])
        val insertCardIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.INSERT_CARD_ICON])
        val greenBackground = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.GREEN_BACKGROUND])
        val orangeBackground = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.ORANGE_BACKGROUND])
        val blueBackground = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.BLUE_BACKGROUND])
        val battleBackground = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.BATTLE_BACKGROUND])


        val readyIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.READY_IDX])
        val goIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.GO_IDX])

        val clearIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.CLEAR_IDX])
        val missionIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.MISSION_IDX])
        val failedIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.FAILED_IDX])

        return Firmware(
            characterFirmwareSprites(sprites),
            MenuBitmaps.menuFirmwareSprites(spriteBitmapConverter, sprites),
            AdventureBitmaps.fromSprites(sprites, spriteBitmapConverter),
            battleFirmwareSprites(sprites),
            trainingFirmwareSprites(sprites),
            transformationFirmwareSprites(sprites),
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

    private fun characterFirmwareSprites(sprites: List<Sprite>): CharacterIconBitmaps {
        val stepsIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.STEPS_ICON])
        val vitalsIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.VITALS_ICON])
        val supportIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.SUPPORT_IDX])
        return CharacterIconBitmaps(stepsIcon, vitalsIcon, supportIcon, emoteFirmwareSprites(sprites))
    }

    private fun emoteFirmwareSprites(sprites: List<Sprite>) : EmoteBitmaps {
        val happyEmote = spriteBitmapConverter.getBitmaps(sprites.subList(
            Firmware10BLoader.HAPPY_EMOTE_START_IDX, Firmware10BLoader.HAPPY_EMOTE_END_IDX
        ))
        val loseEmote = spriteBitmapConverter.getBitmaps(sprites.subList(
            Firmware10BLoader.LOSE_EMOTE_START_IDX, Firmware10BLoader.LOSE_EMOTE_END_IDX
        ))
        val sweatEmote = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.SWEAT_EMOTE_IDX])
        val injuredEmote = spriteBitmapConverter.getBitmaps(sprites.subList(
            Firmware10BLoader.INJURED_EMOTE_START_IDX, Firmware10BLoader.INJURED_EMOTE_END_IDX
        ))
        val sleepEmote = spriteBitmapConverter.getBitmaps(sprites.subList(
            Firmware10BLoader.SLEEP_EMOTE_START_IDX,
            Firmware10BLoader.SLEEP_EMOTE_END_IDX
        ))
        return EmoteBitmaps(happyEmote, loseEmote, sweatEmote, injuredEmote, sleepEmote)
    }

    private fun battleFirmwareSprites(sprites: List<Sprite>): BattleBitmaps {
        val battleBackground = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.BATTLE_BACKGROUND])
        val attackSprites = spriteBitmapConverter.getBitmaps(sprites.subList(
            Firmware10BLoader.SMALL_ATTACK_START_IDX, Firmware10BLoader.SMALL_ATTACK_END_IDX
        ))
        val largeAttackSprites= spriteBitmapConverter.getBitmaps(sprites.subList(
            Firmware10BLoader.BIG_ATTACK_START_IDX, Firmware10BLoader.BIG_ATTACK_END_IDX
        ))
        val emptyHP = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.EMPTY_HP_IDX])
        val partnerHPIcons = Lists.newArrayList(emptyHP)
        partnerHPIcons.addAll(spriteBitmapConverter.getBitmaps(sprites.subList(
            Firmware10BLoader.PARTNER_HP_START_IDX, Firmware10BLoader.PARTNER_HP_END_IDX
        )))
        val opponentHPIcons = Lists.newArrayList(emptyHP)
        opponentHPIcons.addAll(spriteBitmapConverter.getBitmaps(sprites.subList(
            Firmware10BLoader.OPPONENT_HP_START_IDX, Firmware10BLoader.OPPONENT_HP_END_IDX
        )))
        val hitSprites = spriteBitmapConverter.getBitmaps(sprites.subList(
            Firmware10BLoader.HIT_START_IDX,
            Firmware10BLoader.HIT_END_IDX
        ))
        val vitalRangeSprites = spriteBitmapConverter.getBitmaps(sprites.subList(
            Firmware10BLoader.VITALS_RANGE_START_IDX, Firmware10BLoader.VITALS_RANGE_END_IDX
        ))
        return BattleBitmaps(attackSprites, largeAttackSprites, battleBackground, partnerHPIcons, opponentHPIcons, hitSprites, vitalRangeSprites)
    }

    private fun transformationFirmwareSprites(sprites: List<Sprite>): TransformationFirmwareSprites {
        val blackBackground = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.BLACK_BACKGROUND])
        val weakPulse = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.WEAK_PULSE])
        val strongPulse = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.STRONG_PULSE])
        val newBackgrounds = spriteBitmapConverter.getBitmaps(sprites.subList(
            Firmware10BLoader.NEW_BACKGROUND_START_IDX, Firmware10BLoader.NEW_BACKGROUND_END_IDX
        ))
        val rayOfLightBackground = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.RAY_OF_LIGHT_BACKGROUND])
        return TransformationFirmwareSprites(
            blackBackground,
            weakPulse,
            strongPulse,
            newBackgrounds,
            rayOfLightBackground,
            spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.STAR]),
            spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.TRANSFORMATION_HOURGLASS]),
            spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.TRANSFORMATION_VITALS_ICON]),
            spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.TRANSFORMATION_BATTLES_ICON]),
            spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.TRANSFORMATION_WIN_RATIO_ICON]),
            spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.TRANSFORMATION_PP_ICON]),
            spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.TRANSFORMATION_SQUAT_ICON]),
            spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.TRANSFORMATION_LOCKED]),
        )
    }

    private fun trainingFirmwareSprites(sprites: List<Sprite>) : TrainingFirmwareSprites {
        val squatText = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.SQUAT_TEXT_IDX])
        val squatIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.SQUAT_ICON_IDX])
        val crunchText = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.CRUNCH_TEXT_IDX])
        val crunchIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.CRUNCH_ICON_IDX])
        val punchText = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.PUNCH_TEXT_IDX])
        val punchIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.PUNCH_ICON_IDX])
        val dashText = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.DASH_TEXT_IDX])
        val dashIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.DASH_ICON_IDX])
        val trainingStateIcons = spriteBitmapConverter.getBitmaps(sprites.subList(
            Firmware10BLoader.TRAINING_STATE_START_IDX, Firmware10BLoader.TRAINING_STATE_END_IDX
        ))
        val goodIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.GOOD_IDX])
        val greatIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.GREAT_IDX])
        val bpIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.BP_IDX])
        val hpIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.HP_IDX])
        val apIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.AP_IDX])
        val ppIcon = spriteBitmapConverter.getBitmap(sprites[Firmware10BLoader.PP_IDX])
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