package com.github.cfogrady.vitalwear.heartrate

import android.hardware.SensorManager
import android.util.Log
import com.github.cfogrady.vitalwear.util.SensorThreadHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class HeartRateService(
    private val sensorManager: SensorManager,
    private val sensorThreadHandler: SensorThreadHandler,
) {

    companion object {
        const val TAG = "HeartRateService"
    }

    private val _currentExerciseLevel = MutableStateFlow(0)
    val currentExerciseLevel: StateFlow<Int> = _currentExerciseLevel

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

    class ExerciseLevel(val level: Int, val heartRate: HeartRateResult)

    suspend fun getExerciseLevel(lastLevel: Int): ExerciseLevel {
        val heartRateResult = getHeartRate()
        val level = exerciseLevelFromResult(heartRateResult, lastLevel)
        _currentExerciseLevel.value = level
        return ExerciseLevel(level, heartRateResult)
    }

    fun restingHeartRate(): Int {
        //TODO: Get actual
        return 65
    }
}