package com.github.cfogrady.vitalwear.firmware

import android.content.Context
import android.graphics.Bitmap
import com.github.cfogrady.vb.dim.sprite.BemSpriteReader
import com.github.cfogrady.vitalwear.firmware.components.AdventureBitmaps
import com.github.cfogrady.vitalwear.firmware.components.BattleBitmaps
import com.github.cfogrady.vitalwear.firmware.components.CharacterIconBitmaps
import com.github.cfogrady.vitalwear.firmware.components.MenuBitmaps
import com.github.cfogrady.vitalwear.firmware.components.TrainingBitmaps
import com.github.cfogrady.vitalwear.firmware.components.TransformationBitmaps
import com.github.cfogrady.vitalwear.common.card.SpriteBitmapConverter

class Firmware(
    val characterIconBitmaps: CharacterIconBitmaps,
    val menuBitmaps: MenuBitmaps,
    val adventureBitmaps: AdventureBitmaps,
    val battleBitmaps: BattleBitmaps,
    val trainingBitmaps: TrainingBitmaps,
    val transformationBitmaps: TransformationBitmaps,
    val insertCardIcon: Bitmap,
    val backgrounds: List<Bitmap>,
    val readyIcon: Bitmap,
    val goIcon: Bitmap,
) {
    companion object {
        fun loadPreviewFirmwareFromDisk(context: Context): Firmware {
            val firmwareLoader = FirmwareLoader(BemSpriteReader(), SpriteBitmapConverter())
            val use10BFirmware = true
            if (use10BFirmware) {
                return firmwareLoader.loadFirmware(Firmware10BSpriteIndexes.instance, context.assets.open("VBBE_10B.vb2"))
            } else {
                return firmwareLoader.loadFirmware(Firmware20ASpriteIndexes.instance, context.assets.open("VBBE_20A.vb2"))
            }
        }
    }
}
