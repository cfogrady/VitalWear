package com.github.cfogrady.vitalwear.training

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.BackgroundManager
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.data.Firmware

class TrainingMenuActivity : ComponentActivity() {

    lateinit var bitmapScaler: BitmapScaler
    lateinit var firmware: Firmware
    lateinit var backgroundManager: BackgroundManager

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bitmapScaler = (application as VitalWearApp).bitmapScaler
        firmware = (application as VitalWearApp).firmwareManager.getFirmware().value!!
        backgroundManager = (application as VitalWearApp).backgroundManager
        val vitalBoxFactory = (application as VitalWearApp).vitalBoxFactory
        setContent {
            val trainingIntent = Intent(applicationContext, TrainingActivity::class.java)
            val background = backgroundManager.selectedBackground.value!!
            vitalBoxFactory.VitalBox {
                bitmapScaler.ScaledBitmap(bitmap = background, contentDescription = "Background", alignment = Alignment.BottomCenter)
                VerticalPager(pageCount = 4) { page ->
                    when (page) {
                        0 -> {
                            val trainingType = TrainingType.SQUAT
                            menuItem(trainingIcon = firmware.squatIcon, trainingText = firmware.squatText, timeInSeconds = trainingType.durationSeconds) {
                                trainingIntent.putExtra(TrainingActivity.TRAINING_TYPE, trainingType)
                                startActivity(trainingIntent)
                            }
                        }
                        1 -> {
                            val trainingType = TrainingType.CRUNCH
                            menuItem(trainingIcon = firmware.crunchIcon, trainingText = firmware.crunchText, timeInSeconds = trainingType.durationSeconds) {
                                trainingIntent.putExtra(TrainingActivity.TRAINING_TYPE, trainingType)
                                startActivity(trainingIntent)
                            }
                        }
                        2 -> {
                            val trainingType = TrainingType.PUNCH
                            menuItem(trainingIcon = firmware.punchIcon, trainingText = firmware.punchText, timeInSeconds = trainingType.durationSeconds) {
                                trainingIntent.putExtra(TrainingActivity.TRAINING_TYPE, trainingType)
                                startActivity(trainingIntent)
                            }
                        }
                        3 -> {
                            val trainingType = TrainingType.DASH
                            menuItem(trainingIcon = firmware.dashIcon, trainingText = firmware.dashText, timeInSeconds = trainingType.durationSeconds) {
                                trainingIntent.putExtra(TrainingActivity.TRAINING_TYPE, trainingType)
                                startActivity(trainingIntent)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun menuItem(trainingIcon: Bitmap, trainingText: Bitmap, timeInSeconds: Int, onClick: () -> Unit) {
        Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            bitmapScaler.ScaledBitmap(bitmap = trainingIcon, contentDescription = "Training Icon", modifier = Modifier.padding(5.dp))
            bitmapScaler.ScaledBitmap(bitmap = trainingText, contentDescription = "Training Text")
            Text(text = "$timeInSeconds", fontWeight = FontWeight.Bold, fontSize = 4.em, color = Color.Yellow, modifier = Modifier.padding(top = 10.dp))
            Text(text = "sec", fontSize = 3.em, color = Color.Yellow)
        }
    }
}