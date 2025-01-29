package com.github.cfogrady.vitalwear.activity

import com.github.cfogrady.vitalwear.adventure.AdventureScreenController
import com.github.cfogrady.vitalwear.background.BackgroundManager
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.training.BackgroundTrainingController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class InitialScreenControllerImpl(
    characterManager: CharacterManager,
    firmwareManager: FirmwareManager,
    backgroundManager: BackgroundManager,
    override val gameState: StateFlow<GameState>,
    override val backgroundTrainingController: BackgroundTrainingController,
    override val adventureScreenController: AdventureScreenController,
    override val activityLaunchers: ActivityLaunchers,
    coroutineScope: CoroutineScope,
    ): InitialScreenController {

    override val firmwareState: StateFlow<FirmwareManager.FirmwareState> = firmwareManager.firmwareState
    override val characterLoadingDone: StateFlow<Boolean> = characterManager.initialized
    override val backgroundLoaded: StateFlow<Boolean> = backgroundManager.selectedBackground.map {
        it != null
    }.stateIn(coroutineScope, SharingStarted.Lazily, false)
}