package com.github.cfogrady.vitalwear.adventure

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.github.cfogrady.vitalwear.common.composable.util.formatNumber
import com.github.cfogrady.vitalwear.firmware.Firmware
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.LocalDateTime

@Composable
fun AdventureScreen(controller: AdventureScreenController) {
    val vitalBoxFactory = controller.vitalBoxFactory
    val bitmapScaler = controller.bitmapScaler
    val goalComplete by controller.zoneCompleted.collectAsStateWithLifecycle()
    val background by controller.adventureBackground.collectAsStateWithLifecycle()
    LaunchedEffect(goalComplete) {
        if(goalComplete) {
            delay(500)
            controller.launchBattle()
        }
    }
    vitalBoxFactory.VitalBox {
        bitmapScaler.ScaledBitmap(bitmap = background, contentDescription = "background")
        val pagerState = rememberPagerState(pageCount = {2})
        VerticalPager(state = pagerState) {
            when(it) {
                0 -> PartnerScreen(controller)
                1 -> CancelScreen(controller, controller.firmware)
            }

        }
    }
}

@Composable
fun PartnerScreen(controller: AdventureScreenController) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    val bitmapScaler = controller.bitmapScaler
    val firmware = controller.firmware
    val partnerWalkingSprites by remember {controller.partnerWalkingSprites}.collectAsStateWithLifecycle()
    val stepsToGoal by controller.stepsToGoal.collectAsStateWithLifecycle()
    val goal by controller.goal.collectAsStateWithLifecycle()
    LaunchedEffect(true) {
        while(isActive) {
            val secondsUntilNextMinute = 60 - now.second
            val millisUntilNextMinute = secondsUntilNextMinute * 1000L
            delay(millisUntilNextMinute)
            now = LocalDateTime.now()
        }
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .fillMaxWidth()
            .offset(y = controller.backgroundHeight.times(-.05f))) {
            Text(text="${formatNumber(now.hour, 2)}:${formatNumber(now.minute, 2)}", fontWeight = FontWeight.Bold, fontSize = 4.em)
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                bitmapScaler.ScaledBitmap(bitmap = firmware.adventureBitmaps.flagImage, contentDescription = "Goal")
                Text(text = formatNumber(goal, 4), color = Color.Yellow, modifier = Modifier.padding(5.dp, 0.dp))
            }
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp)) {
                bitmapScaler.ScaledBitmap(bitmap = firmware.characterIconBitmaps.stepsIcon, contentDescription = "Steps")
                Text(text = formatNumber(stepsToGoal, 4), color = Color.White, modifier = Modifier.padding(5.dp, 0.dp))
            }
            bitmapScaler.AnimatedScaledBitmap(bitmaps = partnerWalkingSprites, contentDescription = "Character", alignment = Alignment.BottomCenter)
        }
    }
}

@Composable
fun CancelScreen(controller: AdventureScreenController, firmware: Firmware) {
    val bitmapScaler = controller.bitmapScaler
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                controller.stopAdventure()
            },
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        bitmapScaler.ScaledBitmap(
            bitmap = firmware.menuBitmaps.stopText,
            contentDescription = "stop")
        bitmapScaler.ScaledBitmap(
            bitmap = firmware.menuBitmaps.stopIcon,
            contentDescription = "adventure")
    }
}

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
private fun AdventureScreenPreview() {
    AdventureScreen(
        controller = AdventureScreenController.EmptyController(LocalContext.current)
    )
}