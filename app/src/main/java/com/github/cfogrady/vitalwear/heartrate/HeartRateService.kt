package com.github.cfogrady.vitalwear.heartrate

import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import java.util.concurrent.CompletableFuture

class HeartRateService(
    private val sensorManager: SensorManager
) {

    companion object {
        const val TAG = "HeartRateService"
    }

    private fun getHeartRate(): CompletableFuture<HeartRateResult> {
        val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        var future = CompletableFuture<HeartRateResult>()
        val listener = HeartRateSensorListener(){ result ->
            future.complete(result)
        }
        val newFuture = future.whenCompleteAsync { _: HeartRateResult, _: Throwable ->
            sensorManager.unregisterListener(listener)
            Log.i(TAG, "Unregistered Heart Rate Sensor Listener")
        }
        //TODO: add handler thread (https://stackoverflow.com/questions/3286815/sensoreventlistener-in-separate-thread)
        // Normally the sensor events come through on the main thread. This means we can't block the main thread and still receive events.
        // We need to block the main thread for the shutdown, so that we can ensure everything is saved before the shutdown occurs;
        // otherwise, there is a risk that the shutdown will occur before the other threads have finished.
        if(!sensorManager.registerListener(listener, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)) {
            Log.e(TAG, "Failed to register heart rate sensor!")
        }
        return newFuture
    }

    fun getExerciseLevel(lastLevel: Int): CompletableFuture<Int> {
        val latestReadingFuture = getHeartRate()
        return latestReadingFuture.thenApplyAsync { heartRateResult: HeartRateResult ->
            exerciseLevelFromResult(heartRateResult, lastLevel)
        }
    }

    private fun exerciseLevelFromResult(heartRateResult: HeartRateResult, lastLevel: Int): Int {
        Log.i(TAG, "HeartRate: ${heartRateResult.heartRate}, Status: ${heartRateResult.heartRateError}")
        if(heartRateResult.heartRateError != HeartRateResult.Companion.HeartRateError.NONE) {
            return 0
        } else {
            val current = heartRateResult.heartRate
            val resting = restingHeartRate()
            val delta = current - resting
            return if (delta > 40) {
                3
            } else if (delta > 10) {
                2
            } else if (lastLevel == 1) {
                0
            } else {
                1
            }
        }
    }

    private fun restingHeartRate(): Int {
        //TODO: Get actual
        return 65
    }
}