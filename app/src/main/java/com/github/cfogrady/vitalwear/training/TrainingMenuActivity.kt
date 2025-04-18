package com.github.cfogrady.vitalwear.training

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.background.BackgroundManager
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.common.util.ActivityHelper
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.firmware.components.TrainingBitmaps
import com.github.cfogrady.vitalwear.util.flow.transformState

class TrainingMenuActivity : ComponentActivity() {

    private lateinit var bitmapScaler: BitmapScaler
    private lateinit var firmware: TrainingBitmaps
    private lateinit var backgroundManager: BackgroundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityHelper = ActivityHelper(this)
        bitmapScaler = (application as VitalWearApp).bitmapScaler
        firmware = (application as VitalWearApp).firmwareManager.getFirmware().value!!.trainingBitmaps
        backgroundManager = (application as VitalWearApp).backgroundManager
        val vitalBoxFactory = (application as VitalWearApp).vitalBoxFactory
        val trainingLauncher = activityHelper.getActivityLauncherWithResultHandling(TrainingActivity::class.java) {
            val finishToMenu = it.data?.extras?.getBoolean(TrainingActivity.FINISH_TO_MENU, true)
            if (finishToMenu != true) {
                finish()
            }
        }
        val backgroundOnLoad = backgroundManager.selectedBackground.value!! //do here because value shouldn't be called within composable
        setContent {
            val background by backgroundManager.selectedBackground.transformState(backgroundOnLoad) {
                if(it != null) {
                    emit(it)
                }
            }.collectAsStateWithLifecycle()
            vitalBoxFactory.VitalBox {
                bitmapScaler.ScaledBitmap(bitmap = background, contentDescription = "Background", alignment = Alignment.BottomCenter)
                val pagerState = rememberPagerState(pageCount = {4})
                VerticalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> {
                            val trainingType = TrainingType.SQUAT
                            MenuItem(trainingIcon = firmware.squatIcon, trainingText = firmware.squatText, timeInSeconds = trainingType.durationSeconds) {
                                trainingLauncher.invoke { trainingIntent ->
                                    trainingIntent.putExtra(TrainingActivity.TRAINING_TYPE, trainingType)
                                }
                            }
                        }
                        1 -> {
                            val trainingType = TrainingType.CRUNCH
                            MenuItem(trainingIcon = firmware.crunchIcon, trainingText = firmware.crunchText, timeInSeconds = trainingType.durationSeconds) {
                                trainingLauncher.invoke { trainingIntent ->
                                    trainingIntent.putExtra(TrainingActivity.TRAINING_TYPE, trainingType)
                                }
                            }
                        }
                        2 -> {
                            val trainingType = TrainingType.PUNCH
                            MenuItem(trainingIcon = firmware.punchIcon, trainingText = firmware.punchText, timeInSeconds = trainingType.durationSeconds) {
                                trainingLauncher.invoke { trainingIntent ->
                                    trainingIntent.putExtra(TrainingActivity.TRAINING_TYPE, trainingType)
                                }
                            }
                        }
                        3 -> {
                            val trainingType = TrainingType.DASH
                            MenuItem(trainingIcon = firmware.dashIcon, trainingText = firmware.dashText, timeInSeconds = trainingType.durationSeconds) {
                                trainingLauncher.invoke { trainingIntent ->
                                    trainingIntent.putExtra(TrainingActivity.TRAINING_TYPE, trainingType)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MenuItem(trainingIcon: Bitmap, trainingText: Bitmap, timeInSeconds: Int, onClick: () -> Unit) {
        Column(modifier = Modifier.fillMaxSize().clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            bitmapScaler.ScaledBitmap(bitmap = trainingIcon, contentDescription = "Training Icon", modifier = Modifier.padding(5.dp))
            bitmapScaler.ScaledBitmap(bitmap = trainingText, contentDescription = "Training Text")
            Text(text = "$timeInSeconds", fontWeight = FontWeight.Bold, fontSize = 4.em, color = Color.Yellow, modifier = Modifier.padding(top = 10.dp))
            Text(text = "sec", fontSize = 3.em, color = Color.Yellow)
        }
    }
}