package com.github.cfogrady.vitalwear.heartrate

import android.hardware.SensorManager
import android.util.Log
import com.github.cfogrady.vitalwear.util.SensorThreadHandler
import java.time.LocalDateTime
import java.util.*


class HeartRateService(
    private val sensorManager: SensorManager,
    private val sensorThreadHandler: SensorThreadHandler,
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

    private suspend fun getHeartRate(): HeartRateResult {
        val listener = SingleHeartRateSensorListener(sensorManager, sensorThreadHandler)
        return listener.getValue()
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

    suspend fun getExerciseLevel(lastLevel: Int, now: LocalDateTime): Int {
        val heartRateResult = getHeartRate()
        val level = exerciseLevelFromResult(heartRateResult, lastLevel)
        readingsLog.addFirst(HeartRateLog(now, heartRateResult.heartRate, heartRateResult.heartRateError, level, LocalDateTime.now()))
        if(readingsLog.size > 9) {
            readingsLog.removeLast()
        }
        return level
    }

    private fun restingHeartRate(): Int {
        //TODO: Get actual
        return 65
    }
}