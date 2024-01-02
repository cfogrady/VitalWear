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
    private val sensorManager: SensorManager,
    private val heartRateService: HeartRateService
) {
    companion object {


    }

    fun startTraining(trainingType: TrainingType): TrainingProgressTracker {
        return when(trainingType) {
            TrainingType.SQUAT -> trainSquats()
            TrainingType.CRUNCH -> trainCrunches()
            TrainingType.PUNCH -> trainPunches()
            TrainingType.DASH -> TODO()
        }
    }

    private fun trainSquats(): SquatSensorListener {
        val squatSensorListener = SquatSensorListener(heartRateService.restingHeartRate().toFloat(), this::stopListening)
        listenToAccelerometer(squatSensorListener)
        listenToHeartRate(squatSensorListener)
        return squatSensorListener
    }

    private fun trainCrunches(): CrunchSensorListener {
        val crunchSensorListener = CrunchSensorListener(heartRateService.restingHeartRate().toFloat(), this::stopListening)
        listenToAccelerometer(crunchSensorListener)
        listenToHeartRate(crunchSensorListener)
        return crunchSensorListener
    }

    private fun trainPunches(): PunchSensorListener {
        val punchSensorListener = PunchSensorListener(heartRateService.restingHeartRate().toFloat(), this::stopListening)
        listenToAccelerometer(punchSensorListener)
        listenToHeartRate(punchSensorListener)
        return punchSensorListener
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