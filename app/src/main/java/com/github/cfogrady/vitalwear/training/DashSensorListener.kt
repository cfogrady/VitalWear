package com.github.cfogrady.vitalwear.training

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DashSensorListener(private val restingHeartRate: Float, private val unregisterFunctor: (SensorEventListener)->Unit) : TrainingProgressTracker {
    companion object {
        const val TAG = "DashSensorListener"
        const val GOAL = 12
        const val BONUS = 16
    }

    private var startingSteps = Int.MAX_VALUE
    private var lastStep = 0
    private val progress = MutableStateFlow(0.0f)
    private var maxTrainingHeartRate = restingHeartRate
    override fun onSensorChanged(maybeEvent: SensorEvent?) {
        when (maybeEvent?.sensor?.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                handleStepCounter(maybeEvent)
            }

            Sensor.TYPE_HEART_RATE -> {
                handleHeartRate(maybeEvent)
            }

            else -> {
                Log.w(TAG, "Event for unexpected sensor type: ${maybeEvent?.sensor?.name}")
            }
        }
    }

    private fun handleHeartRate(event: SensorEvent) {
        for (heartRate in event.values) {
            if (heartRate > maxTrainingHeartRate) {
                maxTrainingHeartRate = heartRate
            }
        }
    }

    private fun handleStepCounter(event: SensorEvent) {
        for (value in event.values) {
            lastStep = value.toInt()
            if(startingSteps == Int.MAX_VALUE) {
                startingSteps = value.toInt()
            }
            progress.value = (lastStep - startingSteps).toFloat() / GOAL.toFloat()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(TAG, "accuracy change: $accuracy")
    }

    override fun progressFlow(): StateFlow<Float> {
        return progress
    }

    override fun getPoints(): Int {
        var points = 0
        if ((lastStep - startingSteps) >= BONUS) {
            points += 2
        } else if ((lastStep - startingSteps) >= GOAL) {
            points++
        }

        if (maxTrainingHeartRate >= restingHeartRate + 15) {
            points += 2
        } else if (maxTrainingHeartRate >= restingHeartRate + 10) {
            points++
        }
        return points
    }

    override fun unregister() {
        unregisterFunctor.invoke(this)
    }
}