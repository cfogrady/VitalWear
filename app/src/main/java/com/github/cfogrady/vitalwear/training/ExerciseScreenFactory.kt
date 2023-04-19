package com.github.cfogrady.vitalwear.training

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.battle.composable.FightTargetState
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.data.Firmware


class ExerciseScreenFactory(private val vitalBoxFactory: VitalBoxFactory, private val bitmapScaler: BitmapScaler, private val backgroundHeight: Dp) {

    companion object {
        enum class ExerciseState {
            READY,
            GO,
            EXERCISE,
            CLEAR,
            FAIL,
            RESULT,
        }
    }
    @Composable
    fun exerciseScreen(partner: BEMCharacter, firmware: Firmware, background: Bitmap, exciseType: TrainingType) {
        var exerciseState by remember { mutableStateOf(ExerciseState.READY) }
        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(bitmap = background, contentDescription = "Background")
            when(exerciseState) {
                ExerciseState.READY -> {
                    ready(firmware = firmware) {
                        exerciseState = ExerciseState.GO
                    }
                }
                ExerciseState.GO -> TODO()
                ExerciseState.EXERCISE -> TODO()
                ExerciseState.CLEAR -> TODO()
                ExerciseState.FAIL -> TODO()
                ExerciseState.RESULT -> TODO()
            }
        }
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
}