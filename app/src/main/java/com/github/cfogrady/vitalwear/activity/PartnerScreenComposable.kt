package com.github.cfogrady.vitalwear.activity

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.CharacterFirmwareSprites
import com.github.cfogrady.vitalwear.character.data.Mood
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.common.composable.util.formatNumber
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.steps.StepService
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class PartnerScreenComposable(private val bitmapScaler: BitmapScaler, private val backgroundHeight: Dp, private val stepService: StepService) {
    companion object {
        const val TAG = "PartnerScreenComposable"
    }

    @Composable
    fun PartnerScreen(character: VBCharacter, firmware: CharacterFirmwareSprites) {
        val emojiHeight = bitmapScaler.scaledDimension(firmware.emoteFirmwareSprites.sweatEmote.height)
        val dailyStepCount by stepService.dailySteps.collectAsState()
        val timeFrom10StepsAgo by stepService.timeFrom10StepsAgo.collectAsState()
        var now by remember {   mutableStateOf(LocalDateTime.now()) }
        val walking = ChronoUnit.SECONDS.between(timeFrom10StepsAgo, now) < 60
        val spriteIdx = if(walking) CharacterSprites.WALK_1 else CharacterSprites.IDLE_1
        LaunchedEffect(timeFrom10StepsAgo, now) {
            val currentNow = LocalDateTime.now()
            val millisUntilIdle = 60_000 - ChronoUnit.MILLIS.between(timeFrom10StepsAgo, currentNow)
            Log.i(TAG, "Time To Idle: $millisUntilIdle")
            val millisUntilNextMinute = (60 - currentNow.second)*1_000.toLong()
            val nextUpdate = if(millisUntilIdle > 0) millisUntilIdle.coerceAtMost(millisUntilNextMinute) else millisUntilNextMinute
            Log.i(TAG, "Next Update: $nextUpdate")
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
                if(character.characterStats.sleeping) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                        bitmapScaler.AnimatedScaledBitmap(
                            bitmaps = firmware.emoteFirmwareSprites.sleepEmote,
                            startIdx = 0,
                            frames = 2,
                            contentDescription = "sleep"
                        )
                    }
                    bitmapScaler.ScaledBitmap(bitmap = character.characterSprites.sprites[CharacterSprites.DOWN], contentDescription = "character", alignment = Alignment.BottomCenter)
                } else {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                        if (character.mood() == Mood.GOOD) {
                            bitmapScaler.AnimatedScaledBitmap(
                                bitmaps = firmware.emoteFirmwareSprites.happyEmote,
                                startIdx = 0,
                                frames = 2,
                                contentDescription = "happy"
                            )
                        } else if (character.mood() == Mood.BAD) {
                            bitmapScaler.AnimatedScaledBitmap(
                                bitmaps = firmware.emoteFirmwareSprites.loseEmote,
                                startIdx = 0,
                                frames = 2,
                                contentDescription = "sad",
                            )
                        } else {
                            Row(Modifier.height(emojiHeight)) {
                                bitmapScaler
                            }
                        }
                    }
                    bitmapScaler.AnimatedScaledBitmap(bitmaps = character.characterSprites.sprites, startIdx = spriteIdx, frames = 2, contentDescription = "Character", alignment = Alignment.BottomCenter)
                }
            }
        }
    }
}