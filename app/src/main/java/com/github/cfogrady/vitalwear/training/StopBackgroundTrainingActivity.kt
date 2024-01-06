package com.github.cfogrady.vitalwear.training

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Alignment
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.data.GameState

class StopBackgroundTrainingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val backgroundTrainingScreenFactory = (application as VitalWearApp).backgroundTrainingScreenFactory
        val partner = (application as VitalWearApp).characterManager.getCurrentCharacter()!!
        val firmware = (application as VitalWearApp).firmwareManager.getFirmware().value!!
        val background = (application as VitalWearApp).backgroundManager.selectedBackground.value!!
        val bitmapScaler = (application as VitalWearApp).bitmapScaler
        val vitalBoxFactory = (application as VitalWearApp).vitalBoxFactory
        setContent {
            vitalBoxFactory.VitalBox {
                bitmapScaler.ScaledBitmap(bitmap = background, contentDescription = "Background", alignment = Alignment.BottomCenter)
                backgroundTrainingScreenFactory.EndTraining(
                    context = applicationContext,
                    partner = partner,
                    firmware = firmware
                ) {
                    (application as VitalWearApp).gameState.value = GameState.IDLE
                    finish()
                }
            }
        }
    }
}