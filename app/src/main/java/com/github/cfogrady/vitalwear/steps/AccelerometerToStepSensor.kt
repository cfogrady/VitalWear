package com.github.cfogrady.vitalwear.steps

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import com.github.cfogrady.vitalwear.training.PunchSensorListener
import com.github.cfogrady.vitalwear.util.BridgedSensorEventListener
import java.util.LinkedList

class AccelerometerToStepSensor(private val stepSensor: BridgedSensorEventListener, private val timeProvider: ()->Long = System::currentTimeMillis): SensorEventListener {
    private var currentStep = 0;
    private val squaredMagnitudeQueue = LinkedList<Float>()
    private var previousDiff = 0f
    private var lastTime = timeProvider.invoke()
    private var roundsUntilNextReading = 0

    override fun onSensorChanged(event: SensorEvent?) {
        if(event == null) {
            return
        }
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
                if(previousDiff > PunchSensorListener.THRESHOLD_VALUE && diff < PunchSensorListener.THRESHOLD_VALUE) {
                    currentStep++
                    stepSensor.sensorChanged(Sensor.TYPE_STEP_COUNTER, currentTime, floatArrayOf(currentStep.toFloat()))
                    roundsUntilNextReading = 6
                }
            } else {
                roundsUntilNextReading--
            }
            previousDiff = diff
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i("AccelerometerToStepSensor", "Accelerometer accuracy changed: $accuracy")
    }
}