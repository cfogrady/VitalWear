package com.github.cfogrady.vitalwear.steps

import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.LiveData

class StepListener(val dailySteps: LiveData<Int>, private val sensorManager: SensorManager, private val sensorListener: StepSensorListener) {
    companion object {
        const val TAG = "StepListener"
    }
    fun unregsiter() {
        sensorManager.unregisterListener(sensorListener)
        Log.i(TAG, "Unregistered StepSensorListener")
    }
}