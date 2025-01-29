package com.github.cfogrady.vitalwear.activity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.tooling.preview.devices.WearDevices
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.adventure.AdventureScreen
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.training.BackgroundTraining
import timber.log.Timber

@Composable
fun InitialScreen(
    controller: InitialScreenController,
) {
    val firmwareState by controller.firmwareState.collectAsState()
    val characterLoadingDone by controller.characterLoadingDone.collectAsState()
    val backgroundLoaded by controller.backgroundLoaded.collectAsState()

    if(!characterLoadingDone || firmwareState == FirmwareManager.FirmwareState.Loading || !backgroundLoaded) {
        Timber.i("Loading in mainScreen")
        Timber.i("Character Manager Initialized: $characterLoadingDone")
        Timber.i("Firmware Manager Initialized: $firmwareState")
        Timber.i("Background Initialized: $backgroundLoaded")
        Loading(loadingText = "Initializing") {}
    } else if(firmwareState == FirmwareManager.FirmwareState.Missing) {
        controller.activityLaunchers.firmwareLoadingLauncher.invoke()
    } else {
        GameStateScreen(controller)
    }
}

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
private fun InitialScreenPreviewLoading() {

    InitialScreen(
        controller = InitialScreenController.emptyController(context = LocalContext.current),
    )
}

@Composable
fun GameStateScreen(
    controller: InitialScreenController) {
    val gameState by controller.gameState.collectAsState()
    if(gameState == GameState.TRAINING) {
        BackgroundTraining(controller.backgroundTrainingController, activityLaunchers = controller.activityLaunchers)
    } else if (gameState == GameState.ADVENTURE) {
        AdventureScreen(controller.adventureScreenController)
    } else {
        // DailyScreen(firmware, character = character!!, activityLaunchers)
    }
}