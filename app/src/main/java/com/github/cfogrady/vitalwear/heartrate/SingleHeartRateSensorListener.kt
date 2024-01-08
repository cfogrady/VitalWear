package com.github.cfogrady.vitalwear.heartrate

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.github.cfogrady.vitalwear.util.SensorThreadHandler
import kotlinx.coroutines.CompletableDeferred

class SingleHeartRateSensorListener(
    private val sensorManager: SensorManager,
    sensorThreadHandler: SensorThreadHandler,
) : SensorEventListener {
    private val hasReading: CompletableDeferred<Int> = CompletableDeferred()
    private val hasAccuracy: CompletableDeferred<HeartRateResult.Companion.HeartRateError> = CompletableDeferred()
    companion object {
        const val TAG = "HeartRateSensorListener"
    }

    init {
        val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        if(heartRateSensor == null) {
            Log.e(TAG, "Heart rate sensor doesn't exist on device.")
            hasReading.complete(0)
            hasAccuracy.complete(HeartRateResult.Companion.HeartRateError.UNRELIABLE)
        } else {
            if(!sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL, sensorThreadHandler.handler)) {
                Log.w(HeartRateService.TAG, "Failed to register heart rate sensor!")
                hasReading.complete(0)
                hasAccuracy.complete(HeartRateResult.Companion.HeartRateError.UNAVAILABLE)
            } else {
                Log.i(HeartRateService.TAG, "Registered to Heart Rate Sensor")
            }
        }
    }

    suspend fun getValue(): HeartRateResult {
        val heartRate = hasReading.await()
        val heartAccuracyError = hasAccuracy.await()
        sensorManager.unregisterListener(this)
        return HeartRateResult(heartRate, heartAccuracyError)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event != null) {
            val currentRate = event.values[event.values.size-1].toInt()
            Log.i(TAG, "Heart Rate Change: $currentRate")
            hasReading.complete(currentRate)
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