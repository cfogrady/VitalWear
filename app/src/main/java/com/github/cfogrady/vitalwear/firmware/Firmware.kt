package com.github.cfogrady.vitalwear.firmware

import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.battle.data.BattleFirmwareSprites
import com.github.cfogrady.vitalwear.character.data.CharacterFirmwareSprites
import com.github.cfogrady.vitalwear.character.data.EmoteFirmwareSprites
import com.github.cfogrady.vitalwear.menu.MenuFirmwareSprites
import com.github.cfogrady.vitalwear.training.TrainingFirmwareSprites

class Firmware constructor(
    val characterFirmwareSprites: CharacterFirmwareSprites,
    val menuFirmwareSprites: MenuFirmwareSprites,
    val battleFirmwareSprites: BattleFirmwareSprites,
    val emoteFirmwareSprites: EmoteFirmwareSprites,
    val trainingFirmwareSprites: TrainingFirmwareSprites,
    val loadingIcon : Bitmap,
    val insertCardIcon: Bitmap,
    val defaultBackground: Bitmap,
    val readyIcon: Bitmap,
    val goIcon: Bitmap,
    val mission: Bitmap,
    val clear: Bitmap,
    val failedIcon: Bitmap,
) {

}