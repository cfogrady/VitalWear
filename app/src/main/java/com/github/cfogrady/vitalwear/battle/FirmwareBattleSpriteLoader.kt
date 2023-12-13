package com.github.cfogrady.vitalwear.battle

import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.firmware.Firmware

class FirmwareBattleSpriteLoader(private val firmware: Firmware) : BattleSpriteLoader {
    override fun getBackground(): Bitmap {
        return firmware.battleFirmwareSprites.battleBackground
    }

    override fun getReadyIcon(): Bitmap {
        return firmware.readyIcon
    }

    override fun getGoIcon(): Bitmap {
        return firmware.goIcon
    }
}