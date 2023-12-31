package com.github.cfogrady.vitalwear.training

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.SaveService
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.CharacterEntity
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.common.composable.util.KeepScreenOn
import com.github.cfogrady.vitalwear.common.composable.util.formatNumber
import com.github.cfogrady.vitalwear.composable.util.*
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.google.common.collect.Lists
import java.util.Random


class TrainingScreenFactory(private val saveService: SaveService, private val vitalBoxFactory: VitalBoxFactory, private val bitmapScaler: BitmapScaler, private val backgroundHeight: Dp) {

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
    fun ExerciseScreen(partner: BEMCharacter, firmware: Firmware, background: Bitmap, exerciseType: TrainingType, finished: () -> Unit) {
        KeepScreenOn()
        var exerciseState by remember { mutableStateOf(ExerciseState.READY) }
        var exerciseResult by remember { mutableStateOf(ExerciseResult.FAIL) }
        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(bitmap = background, contentDescription = "Background")
            when(exerciseState) {
                ExerciseState.READY -> {
                    Ready(firmware = firmware) {
                        exerciseState = ExerciseState.GO
                    }
                }
                ExerciseState.GO -> {
                    Go(partner, firmware) {
                        exerciseState = ExerciseState.EXERCISE
                    }
                }
                ExerciseState.EXERCISE -> {
                    LaunchedEffect(key1 = true) {
                        //TODO: Start exercise in service
                        Log.i(TAG, "Starting exercise")
                    }
                    Exercise(partner = partner, firmware = firmware, durationSeconds = exerciseType.durationSeconds) {
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
                    Clear(partner = partner, firmware = firmware) {
                        exerciseState = ExerciseState.RESULT
                    }
                }
                ExerciseState.FAIL -> {
                    Fail(partner = partner, firmware = firmware) {
                        finished.invoke()
                    }
                }
                ExerciseState.RESULT -> {
                    Result(partner, firmware, exerciseType, exerciseResult) {
                        finished.invoke()
                    }
                }
            }
        }
    }

    private fun increaseStats(stats: CharacterEntity, trainingType: TrainingType, great: Boolean) {
        val increase = increaseBonus(trainingType, great, true)
        when(trainingType) {
            TrainingType.SQUAT -> {
                stats.trainedPP += increase
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
        saveService.saveAsync()
    }

    @Composable
    private fun Ready(firmware: Firmware, finished: () -> Unit) {
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
    private fun Go(partner: BEMCharacter, firmware: Firmware, finished:() -> Unit) {
        var charaterSprite by remember { mutableStateOf(partner.characterSprites.sprites[CharacterSprites.IDLE_1]) }
        Handler(Looper.getMainLooper()!!).postDelayed({
            charaterSprite = partner.characterSprites.sprites[CharacterSprites.WIN]
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
    private fun Exercise(partner: BEMCharacter, firmware: Firmware, durationSeconds: Int, finished:() -> Unit) {
        val characterSprites = arrayListOf(partner.characterSprites.sprites[CharacterSprites.TRAIN_1],
            partner.characterSprites.sprites[CharacterSprites.TRAIN_2])
        val sweatIcon = remember {firmware.emoteFirmwareSprites.sweatEmote}
        LaunchedEffect(true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                finished.invoke()
            }, durationSeconds * 1000L)
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            bitmapScaler.ScaledBitmap(bitmap = firmware.trainingFirmwareSprites.trainingState[0], contentDescription = "level", modifier = Modifier.offset(y = backgroundHeight.times(.3f)))
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
                    bitmap = characterSprites[spriteIdx],
                    contentDescription = "Character"
                )
            }
        }
    }

    @Composable
    fun Clear(partner: BEMCharacter, firmware: Firmware, finished: () -> Unit) {
        val characterAnimation = remember {Lists.newArrayList(partner.characterSprites.sprites[CharacterSprites.IDLE_1], partner.characterSprites.sprites[CharacterSprites.WIN])}
        LaunchedEffect(true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                finished.invoke()
            }, 1000)
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.offset(y = backgroundHeight.times(.3f)), horizontalAlignment = Alignment.CenterHorizontally) {
                bitmapScaler.ScaledBitmap(bitmap = firmware.mission, contentDescription = "mission")
                bitmapScaler.ScaledBitmap(bitmap = firmware.clear, contentDescription = "clear")
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.AnimatedScaledBitmap(bitmaps = characterAnimation, frames = characterAnimation.size, contentDescription = "character", modifier = Modifier.offset(y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM)))
        }
    }

    @Composable
    fun Fail(partner: BEMCharacter, firmware: Firmware, finished: () -> Unit) {
        val characterAnimation = remember {Lists.newArrayList(partner.characterSprites.sprites[CharacterSprites.IDLE_1], partner.characterSprites.sprites[CharacterSprites.DOWN])}
        LaunchedEffect(true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                finished.invoke()
            }, 1000)
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.offset(y = backgroundHeight.times(.3f))) {
                bitmapScaler.ScaledBitmap(bitmap = firmware.failedIcon, contentDescription = "failed")
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.AnimatedScaledBitmap(bitmaps = characterAnimation, frames = characterAnimation.size, contentDescription = "character", modifier = Modifier.offset(y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM)))
        }
    }

    @Composable
    fun Result(partner: BEMCharacter, firmware: Firmware, exerciseType: TrainingType, exerciseResult: ExerciseResult, finished: () -> Unit) {
        val characterAnimation = remember {Lists.newArrayList(partner.characterSprites.sprites[CharacterSprites.IDLE_1], partner.characterSprites.sprites[CharacterSprites.WIN])}
        val resultIcon = remember {if(exerciseResult == ExerciseResult.GREAT) firmware.trainingFirmwareSprites.greatIcon else firmware.trainingFirmwareSprites.goodIcon}
        LaunchedEffect(true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                finished.invoke()
            }, 1000)
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.offset(y = backgroundHeight.times(.3f)), horizontalAlignment = Alignment.CenterHorizontally) {
                bitmapScaler.ScaledBitmap(bitmap = resultIcon, contentDescription = "result")
                ResultTextRow(exerciseType = exerciseType, exerciseResult = exerciseResult, trainingFirmwareSprites = firmware.trainingFirmwareSprites)
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.AnimatedScaledBitmap(bitmaps = characterAnimation, frames = characterAnimation.size, contentDescription = "character", modifier = Modifier.offset(y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM)))
        }
    }

    @Composable
    fun ResultTextRow(exerciseType: TrainingType, exerciseResult: ExerciseResult, trainingFirmwareSprites: TrainingFirmwareSprites) {
        val typeIcon = remember {
            when(exerciseType) {
                TrainingType.SQUAT -> trainingFirmwareSprites.ppIcon
                TrainingType.CRUNCH -> trainingFirmwareSprites.hpIcon
                TrainingType.PUNCH -> trainingFirmwareSprites.apIcon
                TrainingType.DASH -> trainingFirmwareSprites.bpIcon
            }
        }
        val increase = remember {
            increaseBonus(exerciseType, exerciseResult == ExerciseResult.GREAT, true)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            bitmapScaler.ScaledBitmap(bitmap = typeIcon, contentDescription = "result")
            Text(text = "+${formatNumber(increase, 2)}", color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 3.em)
        }
    }

    private fun increaseBonus(trainingType: TrainingType, great: Boolean, canHaveStatIncrease: Boolean): Int {
        val increase = if(great) 10 else 5
        if(!canHaveStatIncrease) {
            return increase/5
        }
        return when(trainingType) {
            TrainingType.SQUAT -> {
                increase/5
            }

            else -> increase
        }
    }
}