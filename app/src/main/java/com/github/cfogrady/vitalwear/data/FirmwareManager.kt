package com.github.cfogrady.vitalwear.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
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

class FirmwareManager(
    val spriteBitmapConverter: SpriteBitmapConverter
) {
    private val bemSpriteReader = BemSpriteReader()
    private val TAG = "FirmwareManager"
    lateinit var firmware : Firmware

    fun loadFirmware(applicationContext: Context) {
        GlobalScope.launch {
            internalLoadFirmware(applicationContext)
        }
    }

    fun getFirmware() : Optional<Firmware> {
        if(this::firmware.isInitialized) {
            return Optional.of(firmware)
        }
        return Optional.empty()
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
                firmware = Firmware(sprites, loadingIcon, inserCardIcon)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to load firmware", e)
        }
    }
}