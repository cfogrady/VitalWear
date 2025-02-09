package com.github.cfogrady.vitalwear.main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.character.PartnerScreenController
import com.github.cfogrady.vitalwear.common.card.CardSpriteLoader
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.ImageScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.firmware.Firmware
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface MainScreenController {
    val bitmapScaler: BitmapScaler
    val vitalBoxFactory: VitalBoxFactory
    val menuFirmwareSprites: MenuFirmwareSprites
    val partnerScreenController: PartnerScreenController
    val hasActivePartner: StateFlow<Boolean>
    val readyToTransform: StateFlow<Boolean>
    val characterPhase: StateFlow<Int>
    val characterIsAsleep: StateFlow<Boolean>
    fun getBackgroundFlow(): StateFlow<Bitmap>

    fun launchStatsMenuActivity()
    fun launchTransformActivity()
    fun launchCharacterSelectionActivity()
    fun launchTrainingMenuActivity()
    fun launchAdventureActivity()
    fun launchTransferActivity()
    fun launchBattleActivity()
    fun launchSettingsActivity()
    fun toggleSleep()

    companion object {
        fun buildMainScreenController(vitalWearApp: VitalWearApp, activityLaunchers: ActivityLaunchers): MainScreenController {
            return MainScreenControllerImpl(
                bitmapScaler = vitalWearApp.bitmapScaler,
                vitalBoxFactory = vitalWearApp.vitalBoxFactory,
                partnerScreenController = PartnerScreenController.buildPartnerScreenController(vitalWearApp),
                firmwareManager = vitalWearApp.firmwareManager,
                characterManager = vitalWearApp.characterManager,
                activityLaunchers = activityLaunchers,
                saveService = vitalWearApp.saveService,
                backgroundManager = vitalWearApp.backgroundManager
            )
        }
    }

    class EmptyController(
        context: Context,
        imageScaler: ImageScaler = ImageScaler.getContextImageScaler(context),
        override val bitmapScaler: BitmapScaler = BitmapScaler(imageScaler),
        override val vitalBoxFactory: VitalBoxFactory = VitalBoxFactory(imageScaler, ImageScaler.VB_WIDTH.toInt(), ImageScaler.VB_HEIGHT.toInt()),
        firmware: Firmware = Firmware.loadPreviewFirmwareFromDisk(context),
        override val menuFirmwareSprites: MenuFirmwareSprites = firmware.menuFirmwareSprites,
        private val background: StateFlow<Bitmap> = MutableStateFlow(BitmapFactory.decodeStream(context.assets.open("test_background.png"))),
        override val hasActivePartner: StateFlow<Boolean> = MutableStateFlow(true),
        override val characterPhase: StateFlow<Int> = MutableStateFlow(2),
        override val characterIsAsleep: StateFlow<Boolean> = MutableStateFlow(false),
        characterSprites: CharacterSprites = CardSpriteLoader.loadTestCharacterSprites(context, 3),
        override val partnerScreenController: PartnerScreenController =
            PartnerScreenController.EmptyController(
                context,
                imageScaler = imageScaler,
                bitmapScaler = bitmapScaler,
                firmware = firmware,
                characterSprites = characterSprites,
            )
    ) : MainScreenController {
        override val readyToTransform: StateFlow<Boolean> = MutableStateFlow(false)

        override fun getBackgroundFlow(): StateFlow<Bitmap> {
            return background
        }

        override fun launchStatsMenuActivity() {}
        override fun launchTransformActivity() {}
        override fun launchCharacterSelectionActivity() {}
        override fun launchTrainingMenuActivity() {}
        override fun launchAdventureActivity() {}
        override fun launchTransferActivity() {}
        override fun launchBattleActivity() {}
        override fun launchSettingsActivity() {}
        override fun toggleSleep() {}

    }
}