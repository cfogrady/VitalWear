package com.github.cfogrady.vitalwear.firmware

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.cfogrady.vb.dim.sprite.BemSpriteReader
import com.github.cfogrady.vb.dim.sprite.SpriteData.Sprite
import com.github.cfogrady.vb.dim.util.RelativeByteOffsetInputStream
import com.github.cfogrady.vitalwear.battle.data.BattleFirmwareSprites
import com.github.cfogrady.vitalwear.card.SpriteBitmapConverter
import com.github.cfogrady.vitalwear.character.data.CharacterFirmwareSprites
import com.github.cfogrady.vitalwear.character.data.EmoteFirmwareSprites
import com.github.cfogrady.vitalwear.menu.MenuFirmwareSprites
import com.github.cfogrady.vitalwear.training.TrainingFirmwareSprites
import com.google.common.collect.Lists
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files

const val FIRMWARE_FILE = "VBBE_10B.vb2"
const val SPRITE_DIMENSIONS_LOCATION = 0x90a4
const val SPRITE_PACKAGE_LOCATION = 0x80000

const val TIMER_ICON = 81
const val INSERT_CARD_ICON = 38
const val DEFAULT_BACKGROUND = 0
const val STEPS_ICON = 55
const val VITALS_ICON = 54
const val BATTLE_ICON = 370
const val BATTLE_BACKGROUND = 350
const val SMALL_ATTACK_START_IDX = 310
const val SMALL_ATTACK_END_IDX = 350
const val BIG_ATTACK_START_IDX = 288
const val BIG_ATTACK_END_IDX = 310
const val READY_IDX = 167
const val GO_IDX = 168
const val EMPTY_HP_IDX = 360
const val PARTNER_HP_START_IDX = 361
const val PARTNER_HP_END_IDX = 367
const val OPPONENT_HP_START_IDX = 354
const val OPPONENT_HP_END_IDX = 360
const val HIT_START_IDX = 351
const val HIT_END_IDX = 354
const val HAPPY_EMOTE_START_IDX = 23 //also win
const val HAPPY_EMOTE_END_IDX = 25
const val LOSE_EMOTE_START_IDX = 25
const val LOSE_EMOTE_END_IDX = 27
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
const val DASH_ICON_IDX = 249
const val TRAINING_STATE_START_IDX = 238
const val TRAINING_STATE_END_IDX = 245
const val CLEAR_IDX = 175
const val MISSION_IDX = 205
const val FAILED_IDX = 176
const val GOOD_IDX = 184
const val GREAT_IDX = 185
const val BP_IDX = 409
const val HP_IDX = 410
const val AP_IDX = 411
const val PP_IDX = 412
const val VITALS_RANGE_START_IDX = 281
const val VITALS_RANGE_END_IDX = 288

class FirmwareManager(
    val spriteBitmapConverter: SpriteBitmapConverter
) {
    private val bemSpriteReader = BemSpriteReader()
    private val TAG = "FirmwareManager"
    private val firmware = MutableLiveData<Firmware>()
    private val initialized = MutableLiveData(false)

    fun hasBeenInitialized() : LiveData<Boolean> {
        return initialized
    }

    fun storeFirmware(applicationContext: Context, file: String): Job {
        return GlobalScope.launch(Dispatchers.IO) {
            val srcFile = File(file)
            val filesRoot = applicationContext.filesDir
            val firmwareFile = File(filesRoot, FIRMWARE_FILE)
            if (!srcFile.exists()) {
                throw IllegalArgumentException("Given firmware file, $file does not exist!")
            }
            Files.copy(srcFile.toPath(), firmwareFile.toPath())
            if(!internalLoadFirmware(applicationContext)) {
                firmwareFile.delete()
                throw IllegalArgumentException("Given firmware file, $file has errors!")
            }
        }
    }

    fun loadFirmware(applicationContext: Context) {
        GlobalScope.launch {
            internalLoadFirmware(applicationContext)
        }
    }

    fun getFirmware() : LiveData<Firmware> {
        return firmware
    }

    private fun internalLoadFirmware(applicationContext: Context) : Boolean {
        var filesRoot = applicationContext.filesDir
        var firmwareFile = File(filesRoot, FIRMWARE_FILE)
        try {
            FileInputStream(firmwareFile).use { fileInput ->
                val startFirmwareRead = System.currentTimeMillis()
                val input = RelativeByteOffsetInputStream(fileInput)
                input.readToOffset(SPRITE_DIMENSIONS_LOCATION)
                val dimensionsList =
                    bemSpriteReader.readSpriteDimensions(input)
                input.readToOffset(SPRITE_PACKAGE_LOCATION)
                val sprites = bemSpriteReader.getSpriteData(input, dimensionsList).sprites
                Log.i(TAG, "Time to initialize firmware: ${System.currentTimeMillis() - startFirmwareRead}")
                val loadingIcon = spriteBitmapConverter.getBitmap(sprites[TIMER_ICON])
                val inserCardIcon = spriteBitmapConverter.getBitmap(sprites[INSERT_CARD_ICON])
                val defaultBackground = spriteBitmapConverter.getBitmap(sprites[DEFAULT_BACKGROUND])


                val readyIcon = spriteBitmapConverter.getBitmap(sprites[READY_IDX])
                val goIcon = spriteBitmapConverter.getBitmap(sprites[GO_IDX])

                val clearIcon = spriteBitmapConverter.getBitmap(sprites[CLEAR_IDX])
                val missionIcon = spriteBitmapConverter.getBitmap(sprites[MISSION_IDX])
                val failedIcon = spriteBitmapConverter.getBitmap(sprites[FAILED_IDX])

                val loadedFirmware = Firmware(
                    characterFirmwareSprites(sprites),
                    MenuFirmwareSprites.menuFirmwareSprites(spriteBitmapConverter, sprites),
                    battleFirmwareSprites(sprites),
                    emoteFirmwareSprites(sprites),
                    trainingFirmwareSprites(sprites),
                    loadingIcon,
                    inserCardIcon,
                    defaultBackground,
                    readyIcon,
                    goIcon,
                    missionIcon,
                    clearIcon,
                    failedIcon,
                )
                firmware.postValue(loadedFirmware)
                initialized.postValue(true)
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Unable to load firmware", e)
            initialized.postValue(true)
            return false
        }
    }

    private fun characterFirmwareSprites(sprites: List<Sprite>): CharacterFirmwareSprites {
        val stepsIcon = spriteBitmapConverter.getBitmap(sprites[STEPS_ICON])
        val vitalsIcon = spriteBitmapConverter.getBitmap(sprites[VITALS_ICON])
        return CharacterFirmwareSprites(stepsIcon, vitalsIcon)
    }

    private fun emoteFirmwareSprites(sprites: List<Sprite>) : EmoteFirmwareSprites {
        val happyEmote = spriteBitmapConverter.getBitmaps(sprites.subList(
            HAPPY_EMOTE_START_IDX, HAPPY_EMOTE_END_IDX
        ))
        val loseEmote = spriteBitmapConverter.getBitmaps(sprites.subList(
            LOSE_EMOTE_START_IDX, LOSE_EMOTE_END_IDX
        ))
        val sweatEmote = spriteBitmapConverter.getBitmap(sprites[SWEAT_EMOTE_IDX])
        val injuredEmote = spriteBitmapConverter.getBitmaps(sprites.subList(
            INJURED_EMOTE_START_IDX, INJURED_EMOTE_END_IDX
        ))
        return EmoteFirmwareSprites(happyEmote, loseEmote, sweatEmote, injuredEmote)
    }

    private fun battleFirmwareSprites(sprites: List<Sprite>): BattleFirmwareSprites {
        val battleIcon = spriteBitmapConverter.getBitmap(sprites[BATTLE_ICON])
        val battleBackground = spriteBitmapConverter.getBitmap(sprites[BATTLE_BACKGROUND])
        val attackSprites = spriteBitmapConverter.getBitmaps(sprites.subList(
            SMALL_ATTACK_START_IDX, SMALL_ATTACK_END_IDX
        ))
        val largeAttackSprites= spriteBitmapConverter.getBitmaps(sprites.subList(
            BIG_ATTACK_START_IDX, BIG_ATTACK_END_IDX
        ))
        val emptyHP = spriteBitmapConverter.getBitmap(sprites[EMPTY_HP_IDX])
        val partnerHPIcons = Lists.newArrayList(emptyHP)
        partnerHPIcons.addAll(spriteBitmapConverter.getBitmaps(sprites.subList(
            PARTNER_HP_START_IDX, PARTNER_HP_END_IDX
        )))
        val opponentHPIcons = Lists.newArrayList(emptyHP)
        opponentHPIcons.addAll(spriteBitmapConverter.getBitmaps(sprites.subList(
            OPPONENT_HP_START_IDX, OPPONENT_HP_END_IDX
        )))
        val hitSprites = spriteBitmapConverter.getBitmaps(sprites.subList(HIT_START_IDX, HIT_END_IDX))
        val vitalRangeSprites = spriteBitmapConverter.getBitmaps(sprites.subList(
            VITALS_RANGE_START_IDX, VITALS_RANGE_END_IDX))
        return BattleFirmwareSprites(attackSprites, largeAttackSprites, battleBackground, battleIcon, partnerHPIcons, opponentHPIcons, hitSprites, vitalRangeSprites)
    }

    private fun trainingFirmwareSprites(sprites: List<Sprite>) : TrainingFirmwareSprites {
        val squatText = spriteBitmapConverter.getBitmap(sprites[SQUAT_TEXT_IDX])
        val squatIcon = spriteBitmapConverter.getBitmap(sprites[SQUAT_ICON_IDX])
        val crunchText = spriteBitmapConverter.getBitmap(sprites[CRUNCH_TEXT_IDX])
        val crunchIcon = spriteBitmapConverter.getBitmap(sprites[CRUNCH_ICON_IDX])
        val punchText = spriteBitmapConverter.getBitmap(sprites[PUNCH_TEXT_IDX])
        val punchIcon = spriteBitmapConverter.getBitmap(sprites[PUNCH_ICON_IDX])
        val dashText = spriteBitmapConverter.getBitmap(sprites[DASH_TEXT_IDX])
        val dashIcon = spriteBitmapConverter.getBitmap(sprites[DASH_ICON_IDX])
        val trainingStateIcons = spriteBitmapConverter.getBitmaps(sprites.subList(
            TRAINING_STATE_START_IDX, TRAINING_STATE_END_IDX
        ))
        val goodIcon = spriteBitmapConverter.getBitmap(sprites[GOOD_IDX])
        val greatIcon = spriteBitmapConverter.getBitmap(sprites[GREAT_IDX])
        val bpIcon = spriteBitmapConverter.getBitmap(sprites[BP_IDX])
        val hpIcon = spriteBitmapConverter.getBitmap(sprites[HP_IDX])
        val apIcon = spriteBitmapConverter.getBitmap(sprites[AP_IDX])
        val ppIcon = spriteBitmapConverter.getBitmap(sprites[PP_IDX])
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