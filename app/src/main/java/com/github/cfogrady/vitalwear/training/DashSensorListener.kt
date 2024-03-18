package com.github.cfogrady.vitalwear.training

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class DashSensorListener(private val restingHeartRate: Float, private val unregisterFunctor: (SensorEventListener)->Unit) : TrainingProgressTracker() {
    companion object {
        const val GOAL = 12
        const val BONUS = 16
    }

    override val trainingType = TrainingType.DASH

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
                Timber.w("Event for unexpected sensor type: ${maybeEvent?.sensor?.name}")
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
        Timber.i("accuracy change: $accuracy")
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

    override fun reset() {
        startingSteps = lastStep
        maxTrainingHeartRate = restingHeartRate
        progress.value = 0f
    }

    override fun unregister() {
        unregisterFunctor.invoke(this)
    }
}