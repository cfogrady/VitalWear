package com.github.cfogrady.vitalwear.training

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.github.cfogrady.vitalwear.SaveService
import com.github.cfogrady.vitalwear.character.data.CharacterEntity
import com.github.cfogrady.vitalwear.debug.Debuggable
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
) : Debuggable {
    companion object {
    }

    var backgroundTrainingProgressTracker: TrainingProgressTracker? = null

    var lastTraining: BackgroundTrainingResults? = null

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
            throw IllegalStateException("Can't stopBackgroundService if not training progress tracker is present")
        }
        val tracker = backgroundTrainingProgressTracker!!
        backgroundTrainingProgressTracker = null
        tracker.unregister()
        tracker.finishRep()
        context.stopService(Intent(context, TrainingForegroundService::class.java))
        lastTraining = tracker.results()
        return lastTraining!!
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

    fun stopListening(sensorEventListener: SensorEventListener) {
        sensorManager.unregisterListener(sensorEventListener)
    }

    fun increaseStats(stats: CharacterEntity, trainingType: TrainingType, great: Boolean) {
        val increase = increaseBonus(great, trainingType == TrainingType.SQUAT)
        when(trainingType) {
            TrainingType.SQUAT -> {
                stats.trainedPP = (stats.trainedPP + increase).coerceAtMost(99)
            }
            TrainingType.CRUNCH -> {
                stats.trainedHp = (stats.trainedHp + increase).coerceAtMost(999)
            }
            TrainingType.PUNCH -> {
                stats.trainedAp = (stats.trainedAp + increase).coerceAtMost(999)
            }
            TrainingType.DASH -> {
                stats.trainedBp = (stats.trainedBp + increase).coerceAtMost(999)
            }
        }
        saveService.saveAsync()
    }

    fun increaseStatsFromMultipleTrainings(stats: CharacterEntity, backgroundTrainingResults: BackgroundTrainingResults): Int {
        val isPP = backgroundTrainingResults.trainingType == TrainingType.SQUAT
        var statChange = increaseBonus(true, isPP) * backgroundTrainingResults.great
        statChange += increaseBonus(false, isPP) * backgroundTrainingResults.good
        when(backgroundTrainingResults.trainingType) {
            TrainingType.SQUAT -> {
                stats.trainedPP = (stats.trainedPP + statChange).coerceAtMost(99)
            }
            TrainingType.CRUNCH -> {
                stats.trainedHp = (stats.trainedHp + statChange).coerceAtMost(999)
            }
            TrainingType.PUNCH -> {
                stats.trainedAp = (stats.trainedAp + statChange).coerceAtMost(999)
            }
            TrainingType.DASH -> {
                stats.trainedBp = (stats.trainedBp + statChange).coerceAtMost(999)
            }
        }
        saveService.saveAsync()
        return statChange
    }

    fun increaseBonus(great: Boolean, isPP: Boolean): Int {
        val increase = if(great) 10 else 5
        if(isPP) {
            return increase/5
        }
        return increase
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

    override fun debug(): List<Pair<String, String>> {
        if(lastTraining == null) {
            return emptyList()
        }
        return lastTraining!!.reps
    }
}