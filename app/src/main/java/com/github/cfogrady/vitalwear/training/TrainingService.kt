package com.github.cfogrady.vitalwear.training

import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import java.io.File

/**
 * Service to setup and handle training.
 * Note: Accelerometer includes forces of gravity and can be against any axis or a combination
 * depending on watch orientation.
 */
class TrainingService(
    private val trainingPath: File,
    private val sensorManager: SensorManager,
    private val heartRateService: HeartRateService
) {
    companion object {


    }

    fun trainSquats(): TrainingProgressTracker {
        val squatSensorListener = SquatSensorListener(trainingPath, heartRateService.restingHeartRate().toFloat(), this::stopListening)
        listenToAccelerometer(squatSensorListener)
        listenToHeartRate(squatSensorListener)
        return squatSensorListener
    }

    fun stopListening(sensorEventListener: SensorEventListener) {
        sensorManager.unregisterListener(sensorEventListener)
    }

    private fun listenToAccelerometer(sensorEventListener: SensorEventListener) {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // Reading every 1/8 second. This is best effort by the OS
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    private fun listenToHeartRate(sensorEventListener: SensorEventListener) {
        val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        sensorManager.registerListener(sensorEventListener, heartRateSensor, SensorManager.SENSOR_DELAY_GAME)
    }
}