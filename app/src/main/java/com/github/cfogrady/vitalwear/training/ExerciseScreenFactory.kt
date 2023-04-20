package com.github.cfogrady.vitalwear.training

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.battle.composable.FightTargetState
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.CharacterEntity
import com.github.cfogrady.vitalwear.character.data.PreviewCharacterManager
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.KeepScreenOn
import com.github.cfogrady.vitalwear.composable.util.PositionOffsetRatios
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.data.Firmware
import com.google.common.collect.Lists
import java.time.LocalDateTime
import java.util.Random


class ExerciseScreenFactory(private val characterManager: CharacterManager, private val vitalBoxFactory: VitalBoxFactory, private val bitmapScaler: BitmapScaler, private val backgroundHeight: Dp) {

    companion object {
        const val TAG = "ExerciseScreenFactory"
        enum class ExerciseState {
            READY,
            GO,
            EXERCISE,
            CLEAR,
            FAIL,
            RESULT,
        }

        enum class ExerciseResult {
            FAIL,
            GOOD,
            GREAT
        }
    }

    val random = Random()
    @Composable
    fun exerciseScreen(partner: BEMCharacter, firmware: Firmware, background: Bitmap, exerciseType: TrainingType, finished: () -> Unit) {
        KeepScreenOn()
        var exerciseState by remember { mutableStateOf(ExerciseState.READY) }
        var exerciseResult by remember { mutableStateOf(ExerciseResult.FAIL) }
        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(bitmap = background, contentDescription = "Background")
            when(exerciseState) {
                ExerciseState.READY -> {
                    ready(firmware = firmware) {
                        exerciseState = ExerciseState.GO
                    }
                }
                ExerciseState.GO -> {
                    go(partner, firmware) {
                        exerciseState = ExerciseState.EXERCISE
                    }
                }
                ExerciseState.EXERCISE -> {
                    LaunchedEffect(key1 = true) {
                        //TODO: Start exercise in service
                        Log.i(TAG, "Starting exercise")
                    }
                    exercise(partner = partner, firmware = firmware, durationSeconds = exerciseType.durationSeconds) {
                        //TODO: End exercise in service
                        val roll = random.nextInt(100)
                        if(roll < 20) {
                            exerciseState = ExerciseState.FAIL
                        } else if(roll < 80) {
                            increaseStats(partner.characterStats, exerciseType,false)
                            exerciseState = ExerciseState.CLEAR
                            exerciseResult = ExerciseResult.GOOD
                        } else {
                            increaseStats(partner.characterStats, exerciseType,true)
                            exerciseState = ExerciseState.CLEAR
                            exerciseResult = ExerciseResult.GREAT
                        }
                    }
                }
                ExerciseState.CLEAR -> {
                    clear(partner = partner, firmware = firmware) {
                        exerciseState = ExerciseState.RESULT
                    }
                }
                ExerciseState.FAIL -> {
                    finished.invoke()
                }
                ExerciseState.RESULT -> {
                    finished.invoke()
                }
            }
        }
    }

    fun increaseStats(stats: CharacterEntity, trainingType: TrainingType, great: Boolean) {
        val increase = if(great) 10 else 5
        when(trainingType) {
            TrainingType.SQUAT -> {
                stats.trainedPP += increase/5
            }
            TrainingType.CRUNCH -> {
                stats.trainedHp += increase
            }
            TrainingType.PUNCH -> {
                stats.trainedAp += increase
            }
            TrainingType.DASH -> {
                stats.trainedBp += increase
            }
        }
        characterManager.updateCharacterStats(stats, LocalDateTime.now())
    }

    @Composable
    private fun ready(firmware: Firmware, finished: () -> Unit) {
        var countDown by remember { mutableStateOf(3) }
        Handler(Looper.getMainLooper()!!).postDelayed({
            countDown -= 1
            if(countDown == 0) {
                finished.invoke()
            }
        }, 1000)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            bitmapScaler.ScaledBitmap(bitmap = firmware.readyIcon, contentDescription = "ready", modifier = Modifier.offset(y = backgroundHeight.times(.3f)))
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "$countDown", fontSize = 3.em, fontWeight = FontWeight.Bold)
        }
    }

    @Composable
    private fun go(partner: BEMCharacter, firmware: Firmware, finished:() -> Unit) {
        var charaterSprite by remember { mutableStateOf(partner.sprites[1]) }
        Handler(Looper.getMainLooper()!!).postDelayed({
            charaterSprite = partner.sprites[9]
        }, 500)
        LaunchedEffect(true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                finished.invoke()
            }, 1000)
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            bitmapScaler.ScaledBitmap(bitmap = firmware.goIcon, contentDescription = "go", modifier = Modifier.offset(y = backgroundHeight.times(.3f)))
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.ScaledBitmap(bitmap = charaterSprite, contentDescription = "go", modifier = Modifier.offset(y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM)))
        }
    }

    @Composable
    private fun exercise(partner: BEMCharacter, firmware: Firmware, durationSeconds: Int, finished:() -> Unit) {
        var charaterSprites by remember { mutableStateOf(Lists.newArrayList(partner.sprites[7], partner.sprites[8])) }
        val sweatIcon = remember {firmware.sweatEmote}
        LaunchedEffect(true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                finished.invoke()
            }, durationSeconds * 1000L)
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            bitmapScaler.ScaledBitmap(bitmap = firmware.trainingState[0], contentDescription = "level", modifier = Modifier.offset(y = backgroundHeight.times(.3f)))
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
                Handler(Looper.getMainLooper()!!).postDelayed({
                    spriteIdx = (spriteIdx + 1) % 2
                }, 500)
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                    if(spriteIdx % 2 == 1) {
                        bitmapScaler.ScaledBitmap(bitmap = sweatIcon, contentDescription = "Emote")
                    }
                }
                bitmapScaler.ScaledBitmap(
                    bitmap = charaterSprites[spriteIdx],
                    contentDescription = "Character"
                )
            }
        }
    }

    @Composable
    fun clear(partner: BEMCharacter, firmware: Firmware, finished: () -> Unit) {
        val characterAnimation = remember {Lists.newArrayList(partner.sprites[1], partner.sprites[9])}
        LaunchedEffect(true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                finished.invoke()
            }, 1000)
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.offset(y = backgroundHeight.times(.3f))) {
                bitmapScaler.ScaledBitmap(bitmap = firmware.mission, contentDescription = "mission")
                bitmapScaler.ScaledBitmap(bitmap = firmware.clear, contentDescription = "clear")
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.AnimatedScaledBitmap(bitmaps = characterAnimation, frames = characterAnimation.size, contentDescription = "character", modifier = Modifier.offset(y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM)))
        }
    }
}