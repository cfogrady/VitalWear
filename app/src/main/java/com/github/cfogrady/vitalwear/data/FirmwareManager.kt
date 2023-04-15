package com.github.cfogrady.vitalwear.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.cfogrady.vb.dim.sprite.BemSpriteReader
import com.github.cfogrady.vb.dim.util.RelativeByteOffsetInputStream
import com.google.common.collect.Lists
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.lang.Exception
import java.util.Optional
import kotlin.coroutines.CoroutineContext


const val FIRMWARE_FILE = "VBBE_10B.vb2"
const val SPRITE_DIMENSIONS_LOCATION = 0x90a4
const val SPRITE_PACKAGE_LOCATION = 0x80000

const val TIMER_ICON = 81
const val INSERT_CARD_ICON = 38
const val DEFAULT_BACKGROUND = 0
const val CHARACTER_SELECTOR_ICON = 267
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

class FirmwareManager(
    val spriteBitmapConverter: SpriteBitmapConverter
) {
    private val bemSpriteReader = BemSpriteReader()
    private val TAG = "FirmwareManager"
    private val firmware = MutableLiveData<Firmware>()

    fun loadFirmware(applicationContext: Context) {
        GlobalScope.launch {
            internalLoadFirmware(applicationContext)
        }
    }

    fun getFirmware() : LiveData<Firmware> {
        return firmware
    }

    private fun internalLoadFirmware(applicationContext: Context) {
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
                val characterSelectorIcon = spriteBitmapConverter.getBitmap(sprites[CHARACTER_SELECTOR_ICON])
                val stepsIcon = spriteBitmapConverter.getBitmap(sprites[STEPS_ICON])
                val vitalsIcon = spriteBitmapConverter.getBitmap(sprites[VITALS_ICON])
                val battleIcon = spriteBitmapConverter.getBitmap(sprites[BATTLE_ICON])
                val battleBackground = spriteBitmapConverter.getBitmap(sprites[BATTLE_BACKGROUND])
                val attackSprites = spriteBitmapConverter.getBitmaps(sprites.subList(
                    SMALL_ATTACK_START_IDX, SMALL_ATTACK_END_IDX))
                val largeAttackSprites= spriteBitmapConverter.getBitmaps(sprites.subList(
                    BIG_ATTACK_START_IDX, BIG_ATTACK_END_IDX))
                val readyIcon = spriteBitmapConverter.getBitmap(sprites[READY_IDX])
                val goIcon = spriteBitmapConverter.getBitmap(sprites[GO_IDX])
                val emptyHP = spriteBitmapConverter.getBitmap(sprites[EMPTY_HP_IDX])
                val partnerHPIcons = Lists.newArrayList(emptyHP)
                partnerHPIcons.addAll(spriteBitmapConverter.getBitmaps(sprites.subList(
                    PARTNER_HP_START_IDX, PARTNER_HP_END_IDX)))
                val opponentHPIcons = Lists.newArrayList(emptyHP)
                opponentHPIcons.addAll(spriteBitmapConverter.getBitmaps(sprites.subList(
                    OPPONENT_HP_START_IDX, OPPONENT_HP_END_IDX)))
                val loadedFirmware = Firmware(loadingIcon, inserCardIcon, defaultBackground, characterSelectorIcon, stepsIcon, vitalsIcon, battleIcon, attackSprites, largeAttackSprites, battleBackground, readyIcon, goIcon, partnerHPIcons, opponentHPIcons)
                firmware.postValue(loadedFirmware)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to load firmware", e)
        }
    }
}