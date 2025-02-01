package com.github.cfogrady.vitalwear.training

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.wear.tooling.preview.devices.WearDevices
import com.github.cfogrady.vitalwear.composable.util.ImageScaler
import com.github.cfogrady.vitalwear.common.card.CardSpriteLoader
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.PositionOffsetRatios
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.firmware.Firmware

@Composable
fun ActiveTraining(
    characterSprites: List<Bitmap>,
    progress: Float,
    firmware: Firmware,
    sweatIcon: Bitmap,
    backgroundHeight: Dp,
    bitmapScaler: BitmapScaler) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        var spriteIdx = (progress * firmware.trainingFirmwareSprites.trainingState.size).toInt()
        if (spriteIdx >= firmware.trainingFirmwareSprites.trainingState.size) {
            spriteIdx = firmware.trainingFirmwareSprites.trainingState.size-1
        }
        bitmapScaler.ScaledBitmap(bitmap = firmware.trainingFirmwareSprites.trainingState[spriteIdx], contentDescription = "level", modifier = Modifier.offset(y = backgroundHeight.times(.2f)))
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .fillMaxWidth()
            .offset(
                y = backgroundHeight.times(
                    PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM
                )
            )) {
            var spriteIdx by remember { mutableStateOf(0) }
            LaunchedEffect(key1 = spriteIdx) {
                Handler(Looper.getMainLooper()!!).postDelayed({
                    spriteIdx = (spriteIdx + 1) % 2
                }, 500)
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                if(spriteIdx % 2 == 1) {
                    bitmapScaler.ScaledBitmap(bitmap = sweatIcon, contentDescription = "Emote")
                }
            }
            bitmapScaler.ScaledBitmap(
                bitmap = characterSprites[spriteIdx],
                contentDescription = "Character"
            )
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
private fun PreviewActiveTraining() {
    val context = LocalContext.current
    val imageScaler = ImageScaler.getContextImageScaler(context)
    val bitmapScaler = BitmapScaler(imageScaler)
    val characterSprites = CardSpriteLoader.loadTestCharacterSprites(context, 4)
    val trainingSprites = GameState.TRAINING.bitmaps(characterSprites.sprites)
    val firmware = Firmware.loadPreviewFirmwareFromDisk(context)
    ActiveTraining(
        characterSprites = trainingSprites,
        progress = 0.6f,
        firmware = firmware,
        sweatIcon = firmware.characterFirmwareSprites.emoteFirmwareSprites.sweatEmote,
        backgroundHeight = imageScaler.calculateBackgroundHeight(),
        bitmapScaler = bitmapScaler,
    )
}