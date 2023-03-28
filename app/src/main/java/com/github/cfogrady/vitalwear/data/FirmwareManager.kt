package com.github.cfogrady.vitalwear.data

import android.content.Context
import android.util.Log
import com.github.cfogrady.vb.dim.sprite.BemSpriteReader
import com.github.cfogrady.vb.dim.util.RelativeByteOffsetInputStream
import java.io.File
import java.io.FileInputStream
import java.lang.Exception


const val FIRMWARE_FILE = "VBBE_10B.vb2"
const val SPRITE_DIMENSIONS_LOCATION = 0x90a4
const val SPRITE_PACKAGE_LOCATION = 0x80000

class FirmwareManager {
    private val bemSpriteReader = BemSpriteReader()
    private val TAG = "FirmwareManager"
    lateinit var firmware : Firmware

    fun loadFirmware(applicationContext: Context) {
        var filesRoot = applicationContext.filesDir
        var firmwareFile = File(filesRoot, FIRMWARE_FILE)
        try {
            FileInputStream(firmwareFile).use { fileInput ->
                val input = RelativeByteOffsetInputStream(fileInput)
                input.readToOffset(SPRITE_DIMENSIONS_LOCATION)
                val dimensionsList =
                    bemSpriteReader.readSpriteDimensions(input)
                input.readToOffset(SPRITE_PACKAGE_LOCATION)
                firmware = Firmware(bemSpriteReader.getSpriteData(input, dimensionsList).sprites)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to load firmware", e)
        }
    }
}