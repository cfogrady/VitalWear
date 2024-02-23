package com.github.cfogrady.vitalwear.firmware

import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.adventure.firmware.AdventureFirmwareSprites
import com.github.cfogrady.vitalwear.battle.data.BattleFirmwareSprites
import com.github.cfogrady.vitalwear.character.data.CharacterFirmwareSprites
import com.github.cfogrady.vitalwear.character.data.EmoteFirmwareSprites
import com.github.cfogrady.vitalwear.character.transformation.TransformationFirmwareSprites
import com.github.cfogrady.vitalwear.menu.MenuFirmwareSprites
import com.github.cfogrady.vitalwear.training.TrainingFirmwareSprites

class Firmware(
    val characterFirmwareSprites: CharacterFirmwareSprites,
    val menuFirmwareSprites: MenuFirmwareSprites,
    val adventureFirmwareSprites: AdventureFirmwareSprites,
    val battleFirmwareSprites: BattleFirmwareSprites,
    val emoteFirmwareSprites: EmoteFirmwareSprites,
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
)
