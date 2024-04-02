package com.github.cfogrady.vitalwear.training

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.github.cfogrady.vitalwear.SaveService
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import java.lang.IllegalStateException

/**
 * Service to setup and handle training.
 * Note: Accelerometer includes forces of gravity and can be against any axis or a combination
 * depending on watch orientation.
 */
class TrainingService (
    private val sensorManager: SensorManager,
    private val heartRateService: HeartRateService,
    private val saveService: SaveService,
) {
    companion object {
    }

    var backgroundTrainingProgressTracker: TrainingProgressTracker? = null

    fun startBackgroundTraining(context: Context, trainingType: TrainingType): TrainingProgressTracker {
        backgroundTrainingProgressTracker = when(trainingType) {
            TrainingType.SQUAT -> trainSquats()
            TrainingType.CRUNCH -> trainCrunches()
            TrainingType.PUNCH -> trainPunches()
            TrainingType.DASH -> trainDash()
        }
        val foregroundIntent = Intent(context, TrainingForegroundService::class.java)
        context.startForegroundService(foregroundIntent)
        return backgroundTrainingProgressTracker!!
    }

    fun stopBackgroundTraining(context: Context): BackgroundTrainingResults {
        if(backgroundTrainingProgressTracker == null) {
            throw IllegalStateException("Can't stopBackgroundService if training progress tracker isn't present")
        }
        val tracker = backgroundTrainingProgressTracker!!
        backgroundTrainingProgressTracker = null
        tracker.unregister()
        tracker.finishRep() // Might remove in the future to eliminate counting partial reps
        context.stopService(Intent(context, TrainingForegroundService::class.java))
        return tracker.results()
    }

    fun startTraining(trainingType: TrainingType): TrainingProgressTracker {
        return when(trainingType) {
            TrainingType.SQUAT -> trainSquats()
            TrainingType.CRUNCH -> trainCrunches()
            TrainingType.PUNCH -> trainPunches()
            TrainingType.DASH -> trainDash()
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

    private fun trainDash(): DashSensorListener {
        val dashSensorListener = DashSensorListener(heartRateService.restingHeartRate().toFloat(), this::stopListening)
        listenToStepCounter(dashSensorListener)
        listenToHeartRate(dashSensorListener)
        return dashSensorListener
    }

    private fun stopListening(sensorEventListener: SensorEventListener) {
        sensorManager.unregisterListener(sensorEventListener)
    }

    fun increaseStats(partner: VBCharacter, trainingType: TrainingType, great: Boolean): TrainingStatChanges {
        val statChange = partner.increaseStats(trainingType, great)
        saveService.saveAsync()
        return statChange
    }

    fun increaseStatsFromMultipleTrainings(partner: VBCharacter, backgroundTrainingResults: BackgroundTrainingResults): TrainingStatChanges {
        val statChange = partner.increaseStatsFromMultipleTrainings(backgroundTrainingResults)
        saveService.saveAsync()
        return statChange
    }

    private fun listenToAccelerometer(sensorEventListener: SensorEventListener) {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    private fun listenToHeartRate(sensorEventListener: SensorEventListener) {
        val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        sensorManager.registerListener(sensorEventListener, heartRateSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    private fun listenToStepCounter(sensorEventListener: SensorEventListener) {
        val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sensorManager.registerListener(sensorEventListener, stepCounter, SensorManager.SENSOR_DELAY_GAME)
    }
}