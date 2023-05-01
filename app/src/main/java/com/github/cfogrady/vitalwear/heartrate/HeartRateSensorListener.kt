package com.github.cfogrady.vitalwear.heartrate

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.util.concurrent.CompletableFuture

class HeartRateSensorListener(val onReading: (HeartRateResult) -> Unit) : SensorEventListener {
    private val hasReading: CompletableFuture<Int> = CompletableFuture()
    private val hasAccuracy: CompletableFuture<HeartRateResult.Companion.HeartRateError> = CompletableFuture()
    init {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event != null) {
            hasReading.thenAcceptBoth(hasAccuracy) { rate: Int, error: HeartRateResult.Companion.HeartRateError ->
                onReading.invoke(HeartRateResult(rate, error))
            }
            hasReading.complete(event.values[event.values.size-1].toInt())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        val error = when(accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE -> HeartRateResult.Companion.HeartRateError.UNRELIABLE
            SensorManager.SENSOR_STATUS_NO_CONTACT -> HeartRateResult.Companion.HeartRateError.NO_CONTACT
            else -> HeartRateResult.Companion.HeartRateError.NONE
        }
        hasAccuracy.complete(error)
    }
}