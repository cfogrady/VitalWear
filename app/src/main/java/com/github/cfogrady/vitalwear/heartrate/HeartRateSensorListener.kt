package com.github.cfogrady.vitalwear.heartrate

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import java.util.concurrent.CompletableFuture

class HeartRateSensorListener(val onReading: (HeartRateResult) -> Unit) : SensorEventListener {
    private val hasReading: CompletableFuture<Int> = CompletableFuture()
    private val hasAccuracy: CompletableFuture<HeartRateResult.Companion.HeartRateError> = CompletableFuture()
    companion object {
        const val TAG = "HeartRateSensorListener"
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event != null) {
            val currentRate = event.values[event.values.size-1]
            Log.i(TAG, "Heart Rate Change: $currentRate")
            if(!hasReading.isDone) {
                hasReading.thenAcceptBoth(hasAccuracy) { rate: Int, error: HeartRateResult.Companion.HeartRateError ->
                    Log.i(TAG, "Accuracy and Heart Rate Measured")
                    onReading.invoke(HeartRateResult(rate, error))
                }
                hasReading.complete(currentRate.toInt())
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(TAG, "Heart Rate Accuracy Change")
        val error = when(accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE -> HeartRateResult.Companion.HeartRateError.UNRELIABLE
            SensorManager.SENSOR_STATUS_NO_CONTACT -> HeartRateResult.Companion.HeartRateError.NO_CONTACT
            else -> HeartRateResult.Companion.HeartRateError.NONE
        }
        hasAccuracy.complete(error)
    }
}