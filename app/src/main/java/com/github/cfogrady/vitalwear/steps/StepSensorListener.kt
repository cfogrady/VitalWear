package com.github.cfogrady.vitalwear.steps

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class StepSensorListener(private val onNewSteps: (Int) -> Unit) : SensorEventListener{
    companion object {
        const val TAG = "StepSensorListener"
    }
    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.values != null) {
            val stepCount = event.values[event.values.size-1].toInt()
            onNewSteps.invoke(stepCount)
        } else {
            Log.w(TAG, "Received null event or null values")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Accuracy changed")
    }
}