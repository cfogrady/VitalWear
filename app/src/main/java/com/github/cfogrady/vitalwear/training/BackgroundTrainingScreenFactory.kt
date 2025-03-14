package com.github.cfogrady.vitalwear.training

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.common.composable.util.formatNumber
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.firmware.Firmware

class BackgroundTrainingScreenFactory(
    private val trainingScreenFactory: TrainingScreenFactory,
    private val trainingService: TrainingService,
) {
    

    enum class EndTrainingScreen {
        CLEAR,
        FAIL,
        TOTALS,
        RESULTS,
    }

    @Composable
    fun EndTraining(context: Context, partner: VBCharacter, firmware: Firmware, onFinish: () -> Unit) {
        val results = remember {
            trainingService.stopBackgroundTraining(context)
        }
        val statIncrease = remember {
            trainingService.increaseStatsFromMultipleTrainings(partner, results)
        }
        var endTrainingScreen by remember {
            if (results.good == 0 && results.great == 0) {
                mutableStateOf(EndTrainingScreen.FAIL)
            } else {
                mutableStateOf(EndTrainingScreen.CLEAR)
            }
        }
        when(endTrainingScreen) {
            EndTrainingScreen.CLEAR -> trainingScreenFactory.Clear(partner = partner, firmware = firmware) {
                endTrainingScreen = EndTrainingScreen.TOTALS
            }
            EndTrainingScreen.FAIL -> trainingScreenFactory.Fail(partner = partner, firmware = firmware) {
                onFinish.invoke()
            }
            EndTrainingScreen.TOTALS -> BackgroundTrainingTotals(results) {
                endTrainingScreen = EndTrainingScreen.RESULTS
            }
            EndTrainingScreen.RESULTS -> trainingScreenFactory.Result(
                partner = partner,
                firmware = firmware,
                trainingResult = results.resultType(),
                statChange = statIncrease
            ) {
                onFinish.invoke()
            }
        }
    }

    @Composable
    fun BackgroundTrainingTotals(backgroundTrainingResults: BackgroundTrainingResults, finished: () -> Unit) {
        LaunchedEffect(true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                finished.invoke()
            }, 3000)
        }
        Column(modifier = Modifier.fillMaxSize().padding(3.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceAround) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Greats:", fontWeight = FontWeight.Bold, fontSize = 3.em)
                Text(formatNumber(backgroundTrainingResults.great, 3), fontSize = 3.em)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Goods:", fontWeight = FontWeight.Bold, fontSize = 3.em)
                Text(formatNumber(backgroundTrainingResults.good, 3), fontSize = 3.em)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Fails:", fontWeight = FontWeight.Bold, fontSize = 3.em)
                Text(formatNumber(backgroundTrainingResults.failure, 3), fontSize = 3.em)
            }
        }
    }
}