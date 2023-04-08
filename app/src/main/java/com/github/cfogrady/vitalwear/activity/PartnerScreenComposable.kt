package com.github.cfogrady.vitalwear.activity

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.data.Firmware
import java.time.LocalDateTime

class PartnerScreenComposable(val bitmapScaler: BitmapScaler) {
    companion object {
        val TAG = "PartnerScreenComposable"
    }

    @Composable
    fun PartnerScreen(character: BEMCharacter, firmware: Firmware, backgroundHeight: Dp) {
        val emojiHeight = 5.dp //imageScaler
        val now = LocalDateTime.now()
        Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .fillMaxWidth()
            .offset(y = backgroundHeight.times(-.05f))) {
            Text(text="${timeFormat(now.hour)}:${timeFormat(now.minute)}", fontWeight = FontWeight.Bold, fontSize = 4.em)
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                bitmapScaler.ScaledBitmap(bitmap = firmware.vitalsIcon, contentDescription = "Vitals Icon")
                Text(text = "0000", color = Color.White)
            }
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                bitmapScaler.ScaledBitmap(bitmap = firmware.stepsIcon, contentDescription = "Steps Icon")
                Text(text = "00000", color = Color.White)
            }
            Row(Modifier.height(emojiHeight)) {
            }
            bitmapScaler.AnimatedScaledBitmap(bitmaps = character.sprites, startIdx = character.activityIdx, frames = 2, contentDescription = "Character", alignment = Alignment.BottomCenter)
        }
    }

    fun timeFormat(value: Int): String {
        if(value < 10) {
            return "0$value"
        }
        return value.toString()
    }
}