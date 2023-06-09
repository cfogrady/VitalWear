package com.github.cfogrady.vitalwear.training

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.firmware.Firmware

class TrainingActivity : ComponentActivity() {
    companion object {
        const val TRAINING_TYPE = "TRAINING_TYPE"
    }

    lateinit var exerciseScreenFactory: ExerciseScreenFactory
    lateinit var partner: BEMCharacter
    lateinit var firmware: Firmware
    lateinit var background: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val trainingType = intent.getSerializableExtra(TRAINING_TYPE) as TrainingType
        partner = (application as VitalWearApp).characterManager.getLiveCharacter().value!!
        firmware = (application as VitalWearApp).firmwareManager.getFirmware().value!!
        background = (application as VitalWearApp).backgroundManager.selectedBackground.value!!
        exerciseScreenFactory = (application as VitalWearApp).exerciseScreenFactory
        setContent {
            exerciseScreenFactory.exerciseScreen(partner, firmware, background, trainingType) {
                finish()
            }
        }
    }
}