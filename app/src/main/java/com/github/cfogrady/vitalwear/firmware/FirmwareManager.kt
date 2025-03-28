package com.github.cfogrady.vitalwear.firmware

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import com.github.cfogrady.vb.dim.sprite.BemSpriteReader
import com.github.cfogrady.vitalwear.common.card.SpriteBitmapConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

const val FIRMWARE_FILE = "VBBE_FIRMWARE.vb2"

class FirmwareManager(
    spriteBitmapConverter: SpriteBitmapConverter,
    val postFirmwareLoader: PostFirmwareLoader
) {

    private val firmwareLoader = FirmwareLoader(BemSpriteReader(), spriteBitmapConverter)

    enum class FirmwareState {
        Loading,
        Missing,
        Loaded,
    }

    private val firmware = MutableStateFlow<Firmware?>(null)
    private val mutalbeFirmwareState = MutableStateFlow(FirmwareState.Loading)
    val firmwareState: StateFlow<FirmwareState> = mutalbeFirmwareState

    fun firmwareUri(context: Context): Uri {
        val filesRoot = context.filesDir
        val firmwareFile = File(filesRoot, FIRMWARE_FILE)
        return firmwareFile.toUri()
    }

    suspend fun loadFirmware(applicationContext: Context) {
        withContext(Dispatchers.IO) {
            mutalbeFirmwareState.value = FirmwareState.Loading
            if(!internalLoadFirmware(applicationContext)) {
                val filesRoot = applicationContext.filesDir
                val firmwareFile = File(filesRoot, FIRMWARE_FILE)
                firmwareFile.delete()
                mutalbeFirmwareState.value = FirmwareState.Missing
                Timber.w("Imported Firmware file had errors!")
                Toast.makeText(applicationContext, "Invalid Firmware Detected", Toast.LENGTH_SHORT).show()
            }
            if(firmwareState.value == FirmwareState.Loaded) {
                postFirmwareLoader.loadWithFirmware(applicationContext, firmware.value!!)
            }
        }
    }

    fun getFirmware() : StateFlow<Firmware?> {
        return firmware
    }

    private suspend fun internalLoadFirmware(applicationContext: Context) : Boolean {
        val filesRoot = applicationContext.filesDir
        val firmwareFile = File(filesRoot, FIRMWARE_FILE)
        try {
            withContext(Dispatchers.IO) {
                FileInputStream(firmwareFile).use { fileInput ->
                    firmware.value = firmwareLoader.loadFirmware(fileInput)
                }
            }
            mutalbeFirmwareState.value = FirmwareState.Loaded
            return true
        } catch(fnfe: FileNotFoundException) {
            Timber.e("No firmware file", fnfe)
            mutalbeFirmwareState.value = FirmwareState.Missing
            return true
        } catch(iae: IllegalArgumentException) {
            Timber.e("Unable to load firmware", iae)
            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, iae.message, Toast.LENGTH_SHORT).show()
            }
            return false
        } catch (e: Exception) {
            Timber.e("Unable to load firmware", e)
            return false
        }
    }
}