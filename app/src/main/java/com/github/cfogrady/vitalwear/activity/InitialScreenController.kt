package com.github.cfogrady.vitalwear.activity

import android.content.Context
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.training.BackgroundTrainingController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface InitialScreenController {

    companion object {
        fun buildInitialScreenController(vitalWearApp: VitalWearApp, coroutineScope: CoroutineScope): InitialScreenController {
            return InitialScreenControllerImpl(
                characterManager = vitalWearApp.characterManager,
                firmwareManager = vitalWearApp.firmwareManager,
                backgroundManager = vitalWearApp.backgroundManager,
                gameState = vitalWearApp.gameState,
                backgroundTrainingController = BackgroundTrainingController.buildBackgroundTrainingController(vitalWearApp, coroutineScope),
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
            override val backgroundTrainingController = BackgroundTrainingController.EmptyController(context)
        }
    }

    val firmwareState: StateFlow<FirmwareManager.FirmwareState>
    val characterLoadingDone: StateFlow<Boolean>
    val backgroundLoaded: StateFlow<Boolean>
    val gameState: StateFlow<GameState>
    val backgroundTrainingController: BackgroundTrainingController
}