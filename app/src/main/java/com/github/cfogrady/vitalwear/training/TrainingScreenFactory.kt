package com.github.cfogrady.vitalwear.training

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.common.composable.util.KeepScreenOn
import com.github.cfogrady.vitalwear.common.composable.util.formatNumber
import com.github.cfogrady.vitalwear.composable.util.*
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.google.common.collect.Lists
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class TrainingScreenFactory(private val vitalBoxFactory: VitalBoxFactory,
                            private val bitmapScaler: BitmapScaler,
                            private val backgroundHeight: Dp,
                            private val trainingService: TrainingService,
                            private val gameStateFlow: MutableStateFlow<GameState>,
) {

    companion object {
        const val TAG = "ExerciseScreenFactory"
        enum class TrainingState {
            READY,
            GO,
            EXERCISE,
            CLEAR,
            FAIL,
            RESULT,
        }
    }

    @Composable
    fun ExerciseScreen(context: Context, partner: BEMCharacter, firmware: Firmware, background: Bitmap, exerciseType: TrainingType, finishedToMenu: (Boolean) -> Unit) {
        KeepScreenOn()
        var trainingState by remember { mutableStateOf(TrainingState.READY) }
        var trainingResult by remember { mutableStateOf(TrainingResult.FAIL) }
        var statIncease by remember { mutableStateOf(0) }
        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(bitmap = background, contentDescription = "Background")
            when(trainingState) {
                TrainingState.READY -> {
                    Ready(firmware = firmware) {
                        trainingState = TrainingState.GO
                    }
                }
                TrainingState.GO -> {
                    Go(partner, firmware) {
                        if(partner.settings.trainInBackground) {
                            gameStateFlow.value = GameState.TRAINING
                            trainingService.startBackgroundTraining(context, exerciseType)
                            finishedToMenu.invoke(false)
                        } else {
                            trainingState = TrainingState.EXERCISE
                        }
                    }
                }
                TrainingState.EXERCISE -> {
                    val trainingListener = remember { trainingService.startTraining(exerciseType) }
                    DisposableEffect(key1 = true) {
                        onDispose {
                            trainingListener.unregister()
                        }
                    }
                    Exercise(partner = partner, firmware = firmware, trainingListener.progressFlow(), durationSeconds = exerciseType.durationSeconds) {
                        val points = trainingListener.getPoints()
                        if(points == 4) {
                            statIncease = trainingService.increaseStats(partner, exerciseType, true)
                            trainingState = TrainingState.CLEAR
                            trainingResult = TrainingResult.GREAT
                        } else if(points >= 1) {
                            statIncease = trainingService.increaseStats(partner, exerciseType, false)
                            trainingState = TrainingState.CLEAR
                            trainingResult = TrainingResult.GOOD
                        } else {
                            trainingState = TrainingState.FAIL
                            trainingResult = TrainingResult.FAIL
                        }
                    }
                }
                TrainingState.CLEAR -> {
                    Clear(partner = partner, firmware = firmware) {
                        trainingState = TrainingState.RESULT
                    }
                }
                TrainingState.FAIL -> {
                    Fail(partner = partner, firmware = firmware) {
                        finishedToMenu.invoke(true)
                    }
                }
                TrainingState.RESULT -> {
                    Result(partner, firmware, exerciseType, trainingResult, statIncease) {
                        finishedToMenu.invoke(true)
                    }
                }
            }
        }
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
    private fun Exercise(partner: BEMCharacter, firmware: Firmware, progressFlow: StateFlow<Float>, durationSeconds: Int, finished:() -> Unit) {
        val characterSprites = arrayListOf(partner.characterSprites.sprites[CharacterSprites.TRAIN_1],
            partner.characterSprites.sprites[CharacterSprites.TRAIN_2])
        val sweatIcon = firmware.emoteFirmwareSprites.sweatEmote
        val progress by progressFlow.collectAsState()
        LaunchedEffect(true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                finished.invoke()
            }, durationSeconds * 1000L)
        }
        ActiveTraining(characterSprites, progress, firmware, sweatIcon)
    }

    @Composable
    fun ActiveTraining(characterSprites: List<Bitmap>, progress: Float, firmware: Firmware, sweatIcon: Bitmap) {
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
    fun Result(partner: BEMCharacter, firmware: Firmware, exerciseType: TrainingType, trainingResult: TrainingResult, statChange: Int, finished: () -> Unit) {
        val characterAnimation = remember {Lists.newArrayList(partner.characterSprites.sprites[CharacterSprites.IDLE_1], partner.characterSprites.sprites[CharacterSprites.WIN])}
        val resultIcon = remember {if(trainingResult == TrainingResult.GREAT) firmware.trainingFirmwareSprites.greatIcon else firmware.trainingFirmwareSprites.goodIcon}
        LaunchedEffect(true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                finished.invoke()
            }, 1000)
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.offset(y = backgroundHeight.times(.3f)), horizontalAlignment = Alignment.CenterHorizontally) {
                bitmapScaler.ScaledBitmap(bitmap = resultIcon, contentDescription = "result")
                ResultTextRow(exerciseType = exerciseType, statChange, trainingFirmwareSprites = firmware.trainingFirmwareSprites)
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.AnimatedScaledBitmap(bitmaps = characterAnimation, frames = characterAnimation.size, contentDescription = "character", modifier = Modifier.offset(y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM)))
        }
    }

    @Composable
    fun ResultTextRow(exerciseType: TrainingType, statChange: Int, trainingFirmwareSprites: TrainingFirmwareSprites) {
        val typeIcon = remember {
            when(exerciseType) {
                TrainingType.SQUAT -> trainingFirmwareSprites.ppIcon
                TrainingType.CRUNCH -> trainingFirmwareSprites.hpIcon
                TrainingType.PUNCH -> trainingFirmwareSprites.apIcon
                TrainingType.DASH -> trainingFirmwareSprites.bpIcon
            }
        }
        val digits = if(statChange >= 100) 3 else 2
        Row(verticalAlignment = Alignment.CenterVertically) {
            bitmapScaler.ScaledBitmap(bitmap = typeIcon, contentDescription = "result")
            Text(text = "+${formatNumber(statChange, digits)}", color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 3.em)
        }
    }
}