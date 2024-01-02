package com.github.cfogrady.vitalwear.training

import android.hardware.SensorEventListener
import kotlinx.coroutines.flow.StateFlow

interface TrainingProgressTracker : SensorEventListener {
    fun progressFlow(): StateFlow<Float>

    fun unregister()

    fun getPoints(): Int
}