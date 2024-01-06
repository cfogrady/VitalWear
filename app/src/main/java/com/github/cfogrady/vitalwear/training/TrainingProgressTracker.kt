package com.github.cfogrady.vitalwear.training

import android.hardware.SensorEventListener
import kotlinx.coroutines.flow.StateFlow

interface TrainingProgressTracker : SensorEventListener {

    val trainingType: TrainingType
    fun progressFlow(): StateFlow<Float>

    fun unregister()

    fun getPoints(): Int

    fun finishRep()

    fun results(): BackgroundTrainingResults
}