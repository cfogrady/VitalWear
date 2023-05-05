package com.github.cfogrady.vitalwear.heartrate

import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture


class HeartRateService(
    private val sensorManager: SensorManager
) {

    companion object {
        const val TAG = "HeartRateService"

        class HeartRateLog(
            val startListening: LocalDateTime,
            val heartRate: Int,
            val sensorError: HeartRateResult.Companion.HeartRateError,
            val exerciseLevel: Int,
            val readTime: LocalDateTime
        )
    }

    var readingsLog = LinkedList<HeartRateLog>()

    fun debug(): List<Pair<String, String>> {
        val debugList = ArrayList<Pair<String, String>>(20)
        for(log in readingsLog) {
            debugList.add(Pair("HeartRateLog", "${log.startListening}: ${log.readTime}, ${log.heartRate}, ${log.sensorError}, ${log.exerciseLevel}"))
        }
        return debugList
    }

    private fun getHeartRate(): CompletableFuture<HeartRateResult> {
        val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        var future = CompletableFuture<HeartRateResult>()
        val listener = HeartRateSensorListener(){ result ->
            Log.i(TAG, "Completing the Future")
            future.complete(result)
        }
        val newFuture = future.thenApply {
            sensorManager.unregisterListener(listener)
            Log.i(TAG, "Unregistered Heart Rate Sensor Listener")
            future.get()
        }
        //TODO: add handler thread (https://stackoverflow.com/questions/3286815/sensoreventlistener-in-separate-thread)
        // Normally the sensor events come through on the main thread. This means we can't block the main thread and still receive events.
        // We need to block the main thread for the shutdown, so that we can ensure everything is saved before the shutdown occurs;
        // otherwise, there is a risk that the shutdown will occur before the other threads have finished.
        if(!sensorManager.registerListener(listener, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)) {
            Log.w(TAG, "Failed to register heart rate sensor!")
            future.complete(HeartRateResult(0, HeartRateResult.Companion.HeartRateError.UNRELIABLE))
        } else {
            Log.i(TAG, "Registered to Heart Rate Sensor")
        }
        return newFuture
    }

    fun getExerciseLevel(lastLevel: Int): CompletableFuture<Int> {
        val start = LocalDateTime.now()
        val latestReadingFuture = getHeartRate()
        return latestReadingFuture.thenApplyAsync { heartRateResult: HeartRateResult ->
            val level = exerciseLevelFromResult(heartRateResult, lastLevel)
            readingsLog.addFirst(HeartRateLog(start, heartRateResult.heartRate, heartRateResult.heartRateError, level, LocalDateTime.now()))
            if(readingsLog.size > 9) {
                readingsLog.removeLast()
            }
            level
        }
    }

    private fun exerciseLevelFromResult(heartRateResult: HeartRateResult, lastLevel: Int): Int {
        Log.i(TAG, "HeartRate: ${heartRateResult.heartRate}, Status: ${heartRateResult.heartRateError}")
        val current = if(heartRateResult.heartRateError == HeartRateResult.Companion.HeartRateError.NONE) heartRateResult.heartRate else 65
        val resting = restingHeartRate()
        val delta = current - resting
        return if (delta > 40) {
            3
        } else if (delta > 10) {
            2
        } else if (lastLevel < 2) {
            0
        } else {
            1
        }
    }

    private fun restingHeartRate(): Int {
        //TODO: Get actual
        return 65
    }
}