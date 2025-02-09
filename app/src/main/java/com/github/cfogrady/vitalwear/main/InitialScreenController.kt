package com.github.cfogrady.vitalwear.main

import android.content.Context
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.adventure.AdventureScreenController
import com.github.cfogrady.vitalwear.common.card.CardSpriteLoader
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.training.BackgroundTrainingController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface InitialScreenController {

    companion object {
        fun buildInitialScreenController(context: Context, vitalWearApp: VitalWearApp, activityLaunchers: ActivityLaunchers, coroutineScope: CoroutineScope): InitialScreenController {
            return InitialScreenControllerImpl(
                characterManager = vitalWearApp.characterManager,
                firmwareManager = vitalWearApp.firmwareManager,
                backgroundManager = vitalWearApp.backgroundManager,
                gameState = vitalWearApp.gameState,
                backgroundTrainingController = BackgroundTrainingController.buildBackgroundTrainingController(vitalWearApp, coroutineScope),
                adventureScreenController = AdventureScreenController.buildAdventureScreenController(vitalWearApp, activityLaunchers.context, activityLaunchers.adventureActivityLauncher, coroutineScope),
                mainScreenController = MainScreenController.buildMainScreenController(vitalWearApp, activityLaunchers),
                activityLaunchers = activityLaunchers,
                coroutineScope,
                )
        }

        fun emptyController(
            context: Context,
            firmwareState: FirmwareManager.FirmwareState = FirmwareManager.FirmwareState.Loading,
            characterLoaded: Boolean = false,
            backgroundLoaded: Boolean = false,
            gameState: GameState = GameState.IDLE,
        ) = object: InitialScreenController {
            override val firmwareState = MutableStateFlow(firmwareState)
            override val characterLoadingDone = MutableStateFlow(characterLoaded)
            override val backgroundLoaded = MutableStateFlow(backgroundLoaded)
            override val gameState = MutableStateFlow(gameState)
            val firmware = Firmware.loadPreviewFirmwareFromDisk(context)
            val characterSprites = CardSpriteLoader.loadTestCharacterSprites(context, 2)
            override val activityLaunchers = ActivityLaunchers(context = context)
            override val backgroundTrainingController = BackgroundTrainingController.EmptyController(
                context = context,
                firmware = firmware,
                characterSprites = characterSprites
            )
            override val adventureScreenController = AdventureScreenController.EmptyController(
                context = context,
                firmware = firmware,
                characterSprites = characterSprites
            )
            override val mainScreenController: MainScreenController = MainScreenController.EmptyController(
                context = context,
                firmware = firmware,
                characterSprites = characterSprites
            )

        }
    }

    val activityLaunchers: ActivityLaunchers
    val firmwareState: StateFlow<FirmwareManager.FirmwareState>
    val characterLoadingDone: StateFlow<Boolean>
    val backgroundLoaded: StateFlow<Boolean>
    val gameState: StateFlow<GameState>
    val backgroundTrainingController: BackgroundTrainingController
    val adventureScreenController: AdventureScreenController
    val mainScreenController: MainScreenController
}