package com.github.cfogrady.vitalwear.firmware

import android.content.Context
import android.graphics.Bitmap
import com.github.cfogrady.vb.dim.sprite.BemSpriteReader
import com.github.cfogrady.vitalwear.firmware.components.AdventureBitmaps
import com.github.cfogrady.vitalwear.firmware.components.BattleBitmaps
import com.github.cfogrady.vitalwear.firmware.components.CharacterIconBitmaps
import com.github.cfogrady.vitalwear.character.transformation.TransformationFirmwareSprites
import com.github.cfogrady.vitalwear.common.card.SpriteBitmapConverter
import com.github.cfogrady.vitalwear.firmware.components.MenuBitmaps
import com.github.cfogrady.vitalwear.training.TrainingFirmwareSprites

class Firmware(
    val characterIconBitmaps: CharacterIconBitmaps,
    val menuBitmaps: MenuBitmaps,
    val adventureBitmaps: AdventureBitmaps,
    val battleBitmaps: BattleBitmaps,
    val trainingFirmwareSprites: TrainingFirmwareSprites,
    val transformationFirmwareSprites: TransformationFirmwareSprites,
    val loadingIcon : Bitmap,
    val insertCardIcon: Bitmap,
    val backgrounds: List<Bitmap>,
    val readyIcon: Bitmap,
    val goIcon: Bitmap,
    val mission: Bitmap,
    val clear: Bitmap,
    val failedIcon: Bitmap,
) {
    companion object {
        fun loadPreviewFirmwareFromDisk(context: Context): Firmware {
            val firmwareLoader = Firmware10BLoader(BemSpriteReader(), SpriteBitmapConverter())
            return firmwareLoader.load10BFirmware(context.assets.open("VBBE_10B.vb2"))
        }
    }
}
