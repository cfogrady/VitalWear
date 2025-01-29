package com.github.cfogrady.vitalwear.activity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.training.BackgroundTraining
import timber.log.Timber

@Composable
fun InitialScreen(
    activityLaunchers: ActivityLaunchers,
    initialScreenController: InitialScreenController,
) {
    val firmwareState by initialScreenController.firmwareState.collectAsState()
    val characterLoadingDone by initialScreenController.characterLoadingDone.collectAsState()
    val backgroundLoaded by initialScreenController.backgroundLoaded.collectAsState()

    if(!characterLoadingDone || firmwareState == FirmwareManager.FirmwareState.Loading || !backgroundLoaded) {
        Timber.i("Loading in mainScreen")
        Timber.i("Character Manager Initialized: $characterLoadingDone")
        Timber.i("Firmware Manager Initialized: $firmwareState")
        Timber.i("Background Initialized: $backgroundLoaded")
        Loading(loadingText = "Initializing") {}
    } else if(firmwareState == FirmwareManager.FirmwareState.Missing) {
        activityLaunchers.firmwareLoadingLauncher.invoke()
    } else {
        GameStateScreen(initialScreenController, activityLaunchers)
    }
}

@Preview
@Composable
private fun InitialScreenPreviewLoading() {

    InitialScreen(
        activityLaunchers = ActivityLaunchers(),
        initialScreenController = InitialScreenController.emptyController(LocalContext.current),
    )
}

@Composable
fun GameStateScreen(
    initialScreenController: InitialScreenController,
    activityLaunchers: ActivityLaunchers) {
    val gameState by initialScreenController.gameState.collectAsState()
    if(gameState == GameState.TRAINING) {
        BackgroundTraining(initialScreenController.backgroundTrainingController, activityLaunchers = activityLaunchers)
    } else if (gameState == GameState.ADVENTURE) {
        // adventureScreenFactory.AdventureScreen(activityLaunchers.context, activityLaunchers.adventureActivityLauncher, character!!)
    } else {
        // DailyScreen(firmware, character = character!!, activityLaunchers)
    }
}