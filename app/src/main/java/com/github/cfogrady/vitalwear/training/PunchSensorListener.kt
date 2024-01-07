package com.github.cfogrady.vitalwear.training

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.LinkedList

class PunchSensorListener(private val restingHeartRate: Float, private val unregisterFunctor: (SensorEventListener)->Unit, private val timeProvider: () -> Long = System::currentTimeMillis) : TrainingProgressTracker() {
    companion object {
        const val TAG = "PunchSensorListener"
        const val THRESHOLD_VALUE = 200
        const val GOAL = 12
        const val BONUS = 16
    }

    override val trainingType = TrainingType.PUNCH

    private val squaredMagnitudeQueue = LinkedList<Float>()
    private var previousDiff = 0f
    private var lastTime = timeProvider.invoke()
    private var roundsUntilNextReading = 0
    private var totalRegisters = 0
    private val progress = MutableStateFlow(0.0f)
    private var maxTrainingHeartRate = restingHeartRate

    override fun onSensorChanged(maybeEvent: SensorEvent?) {
        when (maybeEvent?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                handleAccelerometer(maybeEvent)
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

    private fun handleAccelerometer(event: SensorEvent) {
        // Note: Accelerometer includes forces of gravity and can be against any axis or a
        // combination depending on watch orientation.
        val currentTime = timeProvider.invoke()
        // 8 times a second
        if (currentTime - lastTime < 1_000 / 8) {
            return
        }
        lastTime = currentTime
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val squaredMagnitude = x*x + y*y + z*z
        squaredMagnitudeQueue.addLast(squaredMagnitude)
        if(squaredMagnitudeQueue.size > 2) {
            val secondPrevious = squaredMagnitudeQueue.removeFirst()
            val diff = squaredMagnitude - secondPrevious
            if(roundsUntilNextReading == 0) {
                if(previousDiff > THRESHOLD_VALUE && diff < THRESHOLD_VALUE) {
                    totalRegisters++
                    progress.value = totalRegisters.toFloat() / GOAL.toFloat()
                    roundsUntilNextReading = 6
                }
            } else {
                roundsUntilNextReading--
            }
            previousDiff = diff
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
        if (totalRegisters >= BONUS) {
            points += 2
        } else if (totalRegisters >= GOAL) {
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
        totalRegisters = 0
        maxTrainingHeartRate = restingHeartRate
        roundsUntilNextReading = 0
        progress.value = 0f
        previousDiff = 0f
        squaredMagnitudeQueue.clear()
    }

    override fun unregister() {
        unregisterFunctor.invoke(this)
    }
}