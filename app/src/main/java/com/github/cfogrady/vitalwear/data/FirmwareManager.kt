package com.github.cfogrady.vitalwear.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.cfogrady.vb.dim.sprite.BemSpriteReader
import com.github.cfogrady.vb.dim.util.RelativeByteOffsetInputStream
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
                firmware.value = Firmware(sprites, loadingIcon, inserCardIcon, defaultBackground, characterSelectorIcon)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to load firmware", e)
        }
    }
}