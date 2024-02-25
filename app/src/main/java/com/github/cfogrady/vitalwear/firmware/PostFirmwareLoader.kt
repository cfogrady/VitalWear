package com.github.cfogrady.vitalwear.firmware

import android.content.Context
import com.github.cfogrady.vitalwear.background.BackgroundManager

/**
 * This class is mean to load dependent on firmware after the firmware is done being loaded
 */
class PostFirmwareLoader(private val backgroundManager: BackgroundManager) {
    fun loadWithFirmware(context: Context, firmware: Firmware) {
        backgroundManager.loadBackgrounds(context, firmware)
    }
}