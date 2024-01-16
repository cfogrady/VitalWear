package com.github.cfogrady.vitalwear.util

import android.hardware.Sensor

interface BridgedSensorEventListener {
    fun sensorChanged(sensorType: Int, timestamp: Long, values: FloatArray)
}