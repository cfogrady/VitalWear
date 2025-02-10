package com.github.cfogrady.vitalwear.training

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.tooling.preview.devices.WearDevices
import com.github.cfogrady.vitalwear.main.ActivityLaunchers
import com.github.cfogrady.vitalwear.firmware.Firmware

@Composable
fun BackgroundTraining(
    controller: BackgroundTrainingController,
    activityLaunchers: ActivityLaunchers,
    ) {
    val vitalBoxFactory = controller.vitalBoxFactory
    val bitmapScalingFactory = controller.bitmapScaler
    val firmware = controller.firmware
    val background by controller.background.collectAsStateWithLifecycle()
    vitalBoxFactory.VitalBox {
        bitmapScalingFactory.ScaledBitmap(bitmap = background, contentDescription = "Background", alignment = Alignment.BottomCenter)
        val pagerState = rememberPagerState(pageCount = {
            2
        })
        VerticalPager(state = pagerState) { page ->
            when (page) {
                0 -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        BackgroundTrainingPartner(controller, firmware)
                    }
                }
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                activityLaunchers.stopBackgroundTrainingLauncher.invoke()
                            },
                        verticalArrangement = Arrangement.SpaceAround,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        bitmapScalingFactory.ScaledBitmap(
                            bitmap = firmware.menuFirmwareSprites.stopText,
                            contentDescription = "stop")
                        bitmapScalingFactory.ScaledBitmap(
                            bitmap = firmware.menuFirmwareSprites.stopIcon,
                            contentDescription = "training")
                    }
                }
            }
        }
    }
}

@Composable
fun BackgroundTrainingPartner(
    controller: BackgroundTrainingController,
    firmware: Firmware) {
    val trainingSprites by controller.partnerTrainingSprites.collectAsStateWithLifecycle()
    val swearIcon = firmware.characterFirmwareSprites.emoteFirmwareSprites.sweatEmote
    val progress by controller.backgroundTrainingProgress.collectAsStateWithLifecycle()
    ActiveTraining(
        characterSprites = trainingSprites,
        progress = progress,
        firmware = firmware,
        sweatIcon = swearIcon,
        controller.backgroundHeight,
        controller.bitmapScaler
    )
}


@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
private fun BackgroundTrainingPreview() {
    BackgroundTraining(
        controller = BackgroundTrainingController.EmptyController(LocalContext.current, trainingProgress = .6f),
        activityLaunchers = ActivityLaunchers()
    )
}