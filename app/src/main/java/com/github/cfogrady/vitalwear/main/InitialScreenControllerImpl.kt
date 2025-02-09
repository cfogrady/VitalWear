package com.github.cfogrady.vitalwear.main

import com.github.cfogrady.vitalwear.adventure.AdventureScreenController
import com.github.cfogrady.vitalwear.background.BackgroundManager
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.training.BackgroundTrainingController
import com.github.cfogrady.vitalwear.util.flow.mapState
import kotlinx.coroutines.flow.StateFlow

class InitialScreenControllerImpl(
    characterManager: CharacterManager,
    firmwareManager: FirmwareManager,
    backgroundManager: BackgroundManager,
    override val gameState: StateFlow<GameState>,
    override val backgroundTrainingController: BackgroundTrainingController,
    override val adventureScreenController: AdventureScreenController,
    override val mainScreenController: MainScreenController,
    override val activityLaunchers: ActivityLaunchers,
    ): InitialScreenController {

    override val firmwareState: StateFlow<FirmwareManager.FirmwareState> = firmwareManager.firmwareState
    override val characterLoadingDone: StateFlow<Boolean> = characterManager.initialized
    override val backgroundLoaded: StateFlow<Boolean> = backgroundManager.selectedBackground.mapState {
        it != null
    }
}