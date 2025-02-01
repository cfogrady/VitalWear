package com.github.cfogrady.vitalwear.character

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.character.data.CharacterFirmwareSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.common.composable.util.formatNumber
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import com.github.cfogrady.vitalwear.steps.StepService
import timber.log.Timber
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class PartnerScreenComposable(
    private val bitmapScaler: BitmapScaler,
    private val backgroundHeight: Dp,
    private val stepService: StepService,
    private val heartRateService: HeartRateService) {

    @Composable
    fun PartnerScreen(character: VBCharacter, firmware: CharacterFirmwareSprites) {
        val emojiHeight = bitmapScaler.scaledDimension(firmware.emoteFirmwareSprites.sweatEmote.height)
        val dailyStepCount by stepService.dailySteps.collectAsState()
        val timeFrom10StepsAgo by stepService.timeFrom10StepsAgo.collectAsState()
        var now by remember {   mutableStateOf(LocalDateTime.now()) }
        val exerciseLevel by heartRateService.currentExerciseLevel.collectAsState()
        val characterBitmaps = remember(exerciseLevel, now, timeFrom10StepsAgo) {
            character.getNormalBitmaps(stepService.hasRecentSteps(now), exerciseLevel)
        }
        val emoteBitmaps = remember(exerciseLevel, now) {
            character.getEmoteBitmaps(firmware.emoteFirmwareSprites, exerciseLevel)
        }
        LaunchedEffect(timeFrom10StepsAgo, now) {
            val currentNow = LocalDateTime.now()
            val millisUntilIdle = 60_000 - ChronoUnit.MILLIS.between(timeFrom10StepsAgo, currentNow)
            Timber.i("Time To Idle: $millisUntilIdle")
            val millisUntilNextMinute = (60 - currentNow.second)*1_000.toLong()
            val nextUpdate = if(millisUntilIdle > 0) millisUntilIdle.coerceAtMost(millisUntilNextMinute) else millisUntilNextMinute
            Timber.i("Next Update: $nextUpdate")
            Handler.createAsync(Looper.getMainLooper()).postDelayed({
                now = LocalDateTime.now()
            }, nextUpdate)
        }
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
}