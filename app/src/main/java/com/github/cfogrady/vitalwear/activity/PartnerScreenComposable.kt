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
import com.github.cfogrady.vitalwear.character.data.CharacterFirmwareSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.formatNumber
import com.github.cfogrady.vitalwear.steps.ManyStepListener
import com.github.cfogrady.vitalwear.steps.StepService
import java.time.LocalDateTime

class PartnerScreenComposable(private val bitmapScaler: BitmapScaler, private val backgroundHeight: Dp, private val stepService: StepService) {
    companion object {
        val TAG = "PartnerScreenComposable"
    }

    @Composable
    fun PartnerScreen(character: BEMCharacter, firmware: CharacterFirmwareSprites, manyStepListener: ManyStepListener) {
        val emojiHeight = 5.dp //imageScaler
        val now = LocalDateTime.now()
        val dailyStepCount by manyStepListener.dailyStepObserver.collectAsState(manyStepListener.dailyStepsAtStart)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .fillMaxWidth()
                .offset(y = backgroundHeight.times(-.05f))) {
                Text(text="${formatNumber(now.hour, 2)}:${formatNumber(now.minute, 2)}", fontWeight = FontWeight.Bold, fontSize = 4.em)
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    bitmapScaler.ScaledBitmap(bitmap = firmware.vitalsIcon, contentDescription = "Vitals Icon")
                    Text(text = formatNumber(character.characterStats.vitals, 4), color = Color.White)
                }
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    bitmapScaler.ScaledBitmap(bitmap = firmware.stepsIcon, contentDescription = "Steps Icon")
                    Text(text = formatNumber(dailyStepCount!!, 5), color = Color.White)
                }
                Row(Modifier.height(emojiHeight)) {
                }
                bitmapScaler.AnimatedScaledBitmap(bitmaps = character.sprites, startIdx = character.activityIdx, frames = 2, contentDescription = "Character", alignment = Alignment.BottomCenter)
            }
        }
    }
}