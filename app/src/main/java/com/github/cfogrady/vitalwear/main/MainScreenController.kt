package com.github.cfogrady.vitalwear.main

import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.character.PartnerScreenController
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import kotlinx.coroutines.flow.StateFlow

interface MainScreenController {
    val menuFirmwareSprites: MenuFirmwareSprites
    val character: StateFlow<VBCharacter>
    val characterPhase: StateFlow<Int>
    val characterSleepStatus: StateFlow<Boolean>
    val characterReadyToTransform: StateFlow<Boolean>
    val characterSprites: StateFlow<CharacterSprites>
    val background: StateFlow<Bitmap>
    val vitalBoxFactory: VitalBoxFactory
    val bitmapScaler: BitmapScaler
    val partnerScreenController: PartnerScreenController

    fun launchStatsMenuActivity()
    fun launchTransformActivity()
    fun launchCharacterSelectionActivity()
    fun launchTrainingMenuActivity()
    fun launchAdventureActivity()
    fun launchTransferActivity()
    fun launchBattleActivity()
    fun launchSettingsActivity()
    fun toggleSleep()

}