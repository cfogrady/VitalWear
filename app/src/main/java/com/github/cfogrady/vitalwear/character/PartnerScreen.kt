package com.github.cfogrady.vitalwear.character

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.github.cfogrady.vitalwear.common.composable.util.formatNumber
import com.github.cfogrady.vitalwear.composable.util.ImageScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory

@Composable
fun PartnerScreen(controller: PartnerScreenController) {
    val bitmapScaler = controller.bitmapScaler
    val firmware = controller.characterFirmwareSprites
    val backgroundHeight = controller.backgroundHeight
    val coroutineScope = rememberCoroutineScope()
    val dailyStepCount by controller.dailyStepCount.collectAsStateWithLifecycle()
    val characterBitmaps by remember {controller.getCharacterBitmaps(coroutineScope)}.collectAsStateWithLifecycle()
    val emoteBitmaps by controller.emoteBitmaps.collectAsStateWithLifecycle()
    val vitals by controller.vitals.collectAsStateWithLifecycle()
    val emojiHeight = bitmapScaler.scaledDimension(firmware.emoteFirmwareSprites.sweatEmote.height)
    val now by remember {controller.getTimeFlow(coroutineScope)}.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .fillMaxWidth()
            .offset(y = backgroundHeight.times(-.05f))) {
            Text(text="${formatNumber(now.hour, 2)}:${formatNumber(now.minute, 2)}", fontWeight = FontWeight.Bold, fontSize = 4.em)
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                bitmapScaler.ScaledBitmap(bitmap = firmware.vitalsIcon, contentDescription = "Vitals Icon")
                Text(text = formatNumber(vitals, 4), color = Color.White)
            }
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                bitmapScaler.ScaledBitmap(bitmap = firmware.stepsIcon, contentDescription = "Steps Icon")
                Text(text = formatNumber(dailyStepCount, 5), color = Color.White)
            }
            Box(modifier = Modifier.fillMaxWidth().height(emojiHeight), contentAlignment = Alignment.BottomEnd) {
                if(emoteBitmaps.isNotEmpty()) {
                    bitmapScaler.AnimatedScaledBitmap(
                        bitmaps = emoteBitmaps,
                        contentDescription = "emote",
                    )
                }
            }
            bitmapScaler.AnimatedScaledBitmap(bitmaps = characterBitmaps, contentDescription = "Character", alignment = Alignment.BottomCenter)
        }
    }
}

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
private fun PartnerScreenPreview() {
    val controller = PartnerScreenController.EmptyController(LocalContext.current)
    val background = BitmapFactory.decodeStream(LocalContext.current.assets.open("test_background.png"))
    VitalBoxFactory(ImageScaler.getContextImageScaler(LocalContext.current))
        .VitalBox {
            Box(modifier = Modifier.fillMaxSize()) {
                controller.bitmapScaler.ScaledBitmap(background, "background")
            }
            PartnerScreen(controller)
        }
}