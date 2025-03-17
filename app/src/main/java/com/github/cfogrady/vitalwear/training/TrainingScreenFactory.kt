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
import com.github.cfogrady.vitalwear.character.StatType
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.common.composable.util.KeepScreenOn
import com.github.cfogrady.vitalwear.common.composable.util.formatNumber
import com.github.cfogrady.vitalwear.composable.util.*
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.github.cfogrady.vitalwear.firmware.components.TrainingBitmaps
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
    fun ExerciseScreen(context: Context, partner: VBCharacter, firmware: Firmware, background: Bitmap, trainingType: TrainingType, finishedToMenu: (Boolean) -> Unit) {
        KeepScreenOn()
        var trainingState by remember { mutableStateOf(TrainingState.READY) }
        var trainingResult by remember { mutableStateOf(TrainingResult.FAIL) }
        var statIncease by remember { mutableStateOf(TrainingStatChanges(trainingType.affectedStat, 0)) }
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
                            trainingService.startBackgroundTraining(context, trainingType)
                            finishedToMenu.invoke(false)
                        } else {
                            trainingState = TrainingState.EXERCISE
                        }
                    }
                }
                TrainingState.EXERCISE -> {
                    val trainingListener = remember { trainingService.startTraining(trainingType) }
                    DisposableEffect(key1 = true) {
                        onDispose {
                            trainingListener.unregister()
                        }
                    }
                    Exercise(partner = partner, firmware = firmware, trainingListener.progressFlow(), durationSeconds = trainingType.durationSeconds) {
                        val points = trainingListener.getPoints()
                        if(points == 4) {
                            statIncease = trainingService.increaseStats(partner, trainingType, true)
                            trainingState = TrainingState.CLEAR
                            trainingResult = TrainingResult.GREAT
                        } else if(points >= 1) {
                            statIncease = trainingService.increaseStats(partner, trainingType, false)
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
                    Result(partner, firmware, trainingResult, statIncease) {
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
    private fun Go(partner: VBCharacter, firmware: Firmware, finished:() -> Unit) {
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
    private fun Exercise(partner: VBCharacter, firmware: Firmware, progressFlow: StateFlow<Float>, durationSeconds: Int, finished:() -> Unit) {
        val characterSprites = arrayListOf(partner.characterSprites.sprites[CharacterSprites.TRAIN_1],
            partner.characterSprites.sprites[CharacterSprites.TRAIN_2])
        val sweatIcon = firmware.characterIconBitmaps.emoteBitmaps.sweatEmote
        val progress by progressFlow.collectAsState()
        LaunchedEffect(true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                finished.invoke()
            }, durationSeconds * 1000L)
        }
        ActiveTraining(characterSprites, progress, firmware, sweatIcon, backgroundHeight, bitmapScaler)
    }

    @Composable
    fun Clear(partner: VBCharacter, firmware: Firmware, finished: () -> Unit) {
        val characterAnimation = remember {Lists.newArrayList(partner.characterSprites.sprites[CharacterSprites.IDLE_1], partner.characterSprites.sprites[CharacterSprites.WIN])}
        LaunchedEffect(true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                finished.invoke()
            }, 1000)
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.offset(y = backgroundHeight.times(.3f)), horizontalAlignment = Alignment.CenterHorizontally) {
                bitmapScaler.ScaledBitmap(bitmap = firmware.trainingBitmaps.mission, contentDescription = "mission")
                bitmapScaler.ScaledBitmap(bitmap = firmware.trainingBitmaps.clear, contentDescription = "clear")
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.AnimatedScaledBitmap(bitmaps = characterAnimation, frames = characterAnimation.size, contentDescription = "character", modifier = Modifier.offset(y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM)))
        }
    }

    @Composable
    fun Fail(partner: VBCharacter, firmware: Firmware, finished: () -> Unit) {
        val characterAnimation = remember {Lists.newArrayList(partner.characterSprites.sprites[CharacterSprites.IDLE_1], partner.characterSprites.sprites[CharacterSprites.DOWN])}
        LaunchedEffect(true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                finished.invoke()
            }, 1000)
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.offset(y = backgroundHeight.times(.3f))) {
                bitmapScaler.ScaledBitmap(bitmap = firmware.trainingBitmaps.failed, contentDescription = "failed")
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.AnimatedScaledBitmap(bitmaps = characterAnimation, frames = characterAnimation.size, contentDescription = "character", modifier = Modifier.offset(y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM)))
        }
    }

    @Composable
    fun Result(partner: VBCharacter, firmware: Firmware, trainingResult: TrainingResult, statChange: TrainingStatChanges, finished: () -> Unit) {
        val characterAnimation = remember {Lists.newArrayList(partner.characterSprites.sprites[CharacterSprites.IDLE_1], partner.characterSprites.sprites[CharacterSprites.WIN])}
        val resultIcon = remember {if(trainingResult == TrainingResult.GREAT) firmware.trainingBitmaps.greatIcon else firmware.trainingBitmaps.goodIcon}
        LaunchedEffect(true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                finished.invoke()
            }, 1000)
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.offset(y = backgroundHeight.times(.3f)), horizontalAlignment = Alignment.CenterHorizontally) {
                bitmapScaler.ScaledBitmap(bitmap = resultIcon, contentDescription = "result")
                ResultTextRow(statChange, firmware)
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.AnimatedScaledBitmap(bitmaps = characterAnimation, frames = characterAnimation.size, contentDescription = "character", modifier = Modifier.offset(y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM)))
        }
    }

    @Composable
    fun ResultTextRow(statChange: TrainingStatChanges, firmware: Firmware) {
        val typeIcon = remember {
            when(statChange.stat) {
                StatType.PP -> firmware.trainingBitmaps.ppIcon
                StatType.HP -> firmware.trainingBitmaps.hpIcon
                StatType.AP -> firmware.trainingBitmaps.apIcon
                StatType.BP -> firmware.trainingBitmaps.bpIcon
            }
        }
        val digits = if(statChange.increase >= 100) 3 else 2
        Row(verticalAlignment = Alignment.CenterVertically) {
            bitmapScaler.ScaledBitmap(bitmap = typeIcon, contentDescription = "result")
            Text(text = "+${formatNumber(statChange.increase, digits)}", color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 3.em)
        }
    }
}