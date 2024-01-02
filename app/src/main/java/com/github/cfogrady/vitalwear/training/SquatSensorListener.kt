package com.github.cfogrady.vitalwear.training

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.util.ArrayList
import java.util.LinkedList
import kotlin.math.abs

class SquatSensorListener(private val restingHeartRate: Float, private val unregisterFunctor: (SensorEventListener)->Unit, private val timeProvider: () -> Long = System::currentTimeMillis) : TrainingProgressTracker {
    companion object {
        const val TAG = "SquatSensorListener"
        const val PEAK_VALUE = 2.0
        const val GOAL = 5
        const val BONUS = 9
    }

    private val sumQueue = LinkedList<Float>()
    private val deltaQueue = LinkedList<Float>()
    private var lastTime = timeProvider.invoke()
    private var roundsUntilNextReading = 0
    private var totalPeaks = 0
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
        for(heartRate in event.values) {
            if(heartRate > maxTrainingHeartRate) {
                maxTrainingHeartRate = heartRate
            }
        }
    }

    private fun handleAccelerometer(event: SensorEvent) {
        // Note: Accelerometer includes forces of gravity and can be against any axis or a
        // combination depending on watch orientation.
        val currentTime = timeProvider.invoke()
        // 8 times a second
        if (currentTime - lastTime < 1_000/8) {
            return
        }
        lastTime = currentTime
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val sum = abs(x) + abs(y) + abs(z)
        sumQueue.addLast(sum)
        while(sumQueue.size > 17) {
            sumQueue.removeFirst()
        }
        if(sumQueue.size == 17) {
            val fullAverage = average(sumQueue)
            val middleAverage = average(sumQueue.subList(7, 12)) // from inclusive to exclusive (5 elements)
            deltaQueue.addLast(middleAverage - fullAverage)
            if( roundsUntilNextReading > 0) {
                roundsUntilNextReading--
            } else if(hasPeak(deltaQueue)) {
                roundsUntilNextReading = 8
                totalPeaks++
                progress.value = totalPeaks.toFloat()/ GOAL.toFloat()
            }
            while(deltaQueue.size > 2) {
                deltaQueue.removeFirst()
            }
        }
    }

    private fun hasPeak(values: Collection<Float>): Boolean {
        if(values.size < 3) {
            return false
        }
        for(value in values) {
            if (value > PEAK_VALUE) {
                return true
            }
        }
        return false
    }

    private fun average(values: Collection<Float>): Float {
        var sum = 0f
        for (value in values) {
            sum += value
        }
        return sum / values.size
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(TAG, "accuracy change: $accuracy")
    }

    override fun progressFlow(): StateFlow<Float> {
        return progress
    }

    override fun getPoints(): Int {
        var points = 0
        if(totalPeaks >= BONUS) {
            points +=2
        } else if(totalPeaks >= GOAL) {
            points++
        }

        if(maxTrainingHeartRate >= restingHeartRate + 15) {
            points +=2
        } else if(maxTrainingHeartRate >= restingHeartRate + 10) {
            points++
        }
        return points
    }

    override fun unregister() {
        unregisterFunctor.invoke(this)
    }
}