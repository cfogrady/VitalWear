package com.github.cfogrady.vitalwear.training

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.firmware.Firmware

class TrainingActivity : ComponentActivity() {
    companion object {
        const val TRAINING_TYPE = "TRAINING_TYPE"
        const val FINISH_TO_MENU = "FINISH_TO_MENU"
    }

    private lateinit var trainingScreenFactory: TrainingScreenFactory
    private lateinit var partner: VBCharacter
    private lateinit var firmware: Firmware
    private lateinit var background: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val trainingType = intent.getSerializableExtra(TRAINING_TYPE) as TrainingType
        partner = (application as VitalWearApp).characterManager.getCharacterFlow().value!!
        firmware = (application as VitalWearApp).firmwareManager.getFirmware().value!!
        background = (application as VitalWearApp).backgroundManager.selectedBackground.value!!
        trainingScreenFactory = (application as VitalWearApp).trainingScreenFactory
        setContent {
            trainingScreenFactory.ExerciseScreen(
                this,
                partner,
                firmware,
                background,
                trainingType
            ) { finishToMenu ->
                val intent = Intent()
                intent.putExtra(FINISH_TO_MENU, finishToMenu)
                setResult(0, intent)
                finish()
            }
        }
    }
}