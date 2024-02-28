package com.github.cfogrady.vitalwear.steps

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.github.cfogrady.vitalwear.SaveService
import com.github.cfogrady.vitalwear.util.SensorThreadHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.*
import java.util.LinkedList

/**
 * Lose Step Vitals between reboots (maybe)
 * Lose Step Vitals between days
 *
 */
class StepSensorService (
    private val sensorManager: SensorManager,
    private val sensorThreadHandler: SensorThreadHandler,
    private val stepChangeListeners: List<StepChangeListener>,
    private val stepState: StepState,
    private val stepIOService: StepIOService,
    private val saveService: SaveService,
    private val dateTimeFetcher: ()->LocalDateTime = LocalDateTime::now): StepService, SensorEventListener {
    companion object {
        const val TAG = "StepsService"
    }

    override val dailySteps: StateFlow<Int> = stepState.dailySteps
    private val _timeFrom10StepsAgo = MutableStateFlow(LocalDateTime.now().minusMinutes(10))
    override val timeFrom10StepsAgo: StateFlow<LocalDateTime> = _timeFrom10StepsAgo


    private var last10Steps = LinkedList<LocalDateTime>()

    /**
     * Handles the a boot up of the application.
     */
    fun startup() {
        Log.i(TAG, "Handling startup")
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if(stepSensor == null) {
            Log.e(TAG, "No Step Sensor On Device")
            return
        }
        if(!sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL, sensorThreadHandler.handler)) {
            Log.e(TAG, "Unable to register step sensor")
            return
        }
    }

    fun stepSensorEnabled(): Boolean {
        return stepState.stepSensorEnabled
    }

    /**
     * Handle stepsAtBoot.
     * Takes current step counter and current date.
     * Returns a boolean indicating whether a save is necessary (date has changed and we have a new
     * start of days steps or step counter has been reset from a reboot)
     */
    private fun stepsAtBootChanges(currentStepCounter: Int, today: LocalDate): Boolean {
        Log.i(TAG, "Performing steps on app startup")
        val oldStepState = stepIOService.stepPreferences()
        val dateFromSave = LocalDate.ofEpochDay(oldStepState.timeLastReadDaysFromEpoch)
        if(dateFromSave != today) {
            // we're on a different day than the last save, so reset everything
            Log.i(TAG, "Restarting steps with new day")
            stepState.startOfDaySteps = currentStepCounter
            stepState.lastStepReading = currentStepCounter
            stepState.dailySteps.value = 0
            return true
        } else if(oldStepState.lastStepReading > currentStepCounter) {
            // we reset the step counter, so assume a reboot
            Log.i(TAG, "Restarting steps from device reboot")
            stepState.startOfDaySteps = currentStepCounter - oldStepState.dailySteps
            stepState.lastStepReading = oldStepState.lastStepReading
            stepState.dailySteps.value = oldStepState.dailySteps
            return true
        } else {
            // App shutdown and restarted. We're on the same day.
            Log.i(TAG, "Restarting steps from app restart")
            stepState.startOfDaySteps = oldStepState.lastStepReading - oldStepState.dailySteps
            stepState.lastStepReading = oldStepState.lastStepReading
            stepState.dailySteps.value = currentStepCounter - stepState.startOfDaySteps
            return false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Log.i(TAG, "SensorChange Called")
        event?.let {
            val now = dateTimeFetcher.invoke()
            val today = now.toLocalDate()
            val newStepCount = it.values[0].toInt()
            var shouldSaveFromSteps = false
            if(!stepState.stepSensorEnabled) {
                stepState.dateOfLastRead = today
                shouldSaveFromSteps = stepsAtBootChanges(newStepCount, today)
                stepState.stepSensorEnabled = true
            } else if (today > stepState.dateOfLastRead) {
                stepState.dateOfLastRead = today
                stepState.startOfDaySteps += dailySteps.value
                shouldSaveFromSteps = true
            }
            for(stepChangeListener in stepChangeListeners) {
                shouldSaveFromSteps = shouldSaveFromSteps || stepChangeListener.processStepChanges(stepState.lastStepReading, newStepCount)
            }
            stepState.lastStepReading = newStepCount
            stepState.dailySteps.value = newStepCount - stepState.startOfDaySteps
            if(shouldSaveFromSteps) {
                saveService.saveAsync()
            }
            last10Steps.addFirst(now)
            while(last10Steps.size > 9) {
                _timeFrom10StepsAgo.value = last10Steps.removeLast()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(TAG, "Received accuracy change for step sensor")
    }

    fun debug(): List<Pair<String, String>> {
        return buildList {
            this.add(Pair("currentSteps", "${stepState.lastStepReading}"))
            this.add(Pair("startOfDaySteps", "${stepState.startOfDaySteps}"))
            this.add(Pair("dailySteps", "${dailySteps.value}"))
            this.add(Pair("stepSensorEnabled", "${stepState.stepSensorEnabled}"))
            this.addAll(stepIOService.debug())
        }
    }

}