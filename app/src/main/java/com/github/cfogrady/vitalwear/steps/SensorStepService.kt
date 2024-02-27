package com.github.cfogrady.vitalwear.steps

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.github.cfogrady.vitalwear.util.SensorThreadHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.*
import java.util.LinkedList
import java.util.Queue

/**
 * Lose Step Vitals between reboots (maybe)
 * Lose Step Vitals between days
 *
 */
class SensorStepService (
    private val sharedPreferences: SharedPreferences,
    private val sensorManager: SensorManager,
    private val sensorThreadHandler: SensorThreadHandler,
    private val stepChangeListeners: List<StepChangeListener>,
    private val dateTimeFetcher: ()->LocalDateTime = LocalDateTime::now): StepService, SensorEventListener {
    companion object {
        const val TAG = "StepsService"
        const val DAILY_STEPS_KEY = "DAILY_STEPS"
        const val DAY_OF_LAST_READ_KEY = "DAY_OF_LAST_READ"
        const val STEP_COUNTER_KEY = "STEP_COUNTER_VALUE"
        private const val WORK_TAG = "StepsService"
        const val LAST_MIDNIGHT_KEY = "LAST_MIDNIGHT"
    }

    private val _dailySteps = MutableStateFlow(0)
    override val dailySteps: StateFlow<Int> = _dailySteps
    private var hasFirstReading = false
    private lateinit var dateOfLastRead: LocalDate
    private var stepSensorEnabled = false

    private var lastStepReading = 0
    private var startOfDaySteps = 0
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
        stepSensorEnabled = true
    }

    fun stepSensorEnabled(): Boolean {
        return stepSensorEnabled
    }

    private fun stepsAtBoot(curentStepCounter: Int, today: LocalDate) {
        Log.i(TAG, "Performing steps on app startup")
        val dailyStepsBeforeShutdown = sharedPreferences.getInt(DAILY_STEPS_KEY, 0)
        val lastStepCounter = sharedPreferences.getInt(STEP_COUNTER_KEY, 0)
        val timeSinceEpoch = sharedPreferences.getLong(DAY_OF_LAST_READ_KEY, 0)
        val dateFromSave = LocalDate.ofEpochDay(timeSinceEpoch)
        if(dateFromSave != today) {
            // we're on a different day than the last save, so reset everything
            Log.i(TAG, "Restarting steps with new day")
            startOfDaySteps = curentStepCounter
            lastStepReading = curentStepCounter
            _dailySteps.value = 0
        } else if(lastStepCounter > curentStepCounter) {
            // we reset the step counter, so assume a reboot
            Log.i(TAG, "Restarting steps from device reboot")
            startOfDaySteps = curentStepCounter - dailyStepsBeforeShutdown
            lastStepReading = lastStepCounter
            _dailySteps.value = dailyStepsBeforeShutdown
        } else {
            // App shutdown and restarted. We're on the same day.
            Log.i(TAG, "Restarting steps from app restart")
            startOfDaySteps = lastStepCounter - dailyStepsBeforeShutdown
            lastStepReading = lastStepCounter
            _dailySteps.value = curentStepCounter - startOfDaySteps
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val now = dateTimeFetcher.invoke()
            val today = now.toLocalDate()
            val newStepCount = it.values[0].toInt()
            if(!hasFirstReading) {
                dateOfLastRead = today
                stepsAtBoot(newStepCount, today)
            } else if (today > dateOfLastRead) {
                dateOfLastRead = today
                startOfDaySteps += dailySteps.value
            }
            for(stepChangeListener in stepChangeListeners) {
                stepChangeListener.processStepChanges(lastStepReading, newStepCount)
            }
            lastStepReading = newStepCount
            _dailySteps.value = newStepCount - startOfDaySteps
            last10Steps.addFirst(now)
            while(last10Steps.size > 10) {
                last10Steps.removeLast()
            }
        }
    }

    fun timeAt10StepsAgo(): LocalDateTime {
        return last10Steps.last
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(TAG, "Received accuracy change for step sensor")
    }

    /**
     * Caller is expected to commit or apply these changes
     */
    fun stepPreferenceUpdates(now: LocalDate, sharedPreferencesEditor: Editor = sharedPreferences.edit()) : Editor {
        return sharedPreferencesEditor.putInt(DAILY_STEPS_KEY, dailySteps.value)
            .putInt(STEP_COUNTER_KEY, lastStepReading)
            .putLong(DAY_OF_LAST_READ_KEY, now.toEpochDay())
    }

    fun debug(): List<Pair<String, String>> {
        return listOf(
            Pair("currentSteps", "$lastStepReading"),
            Pair("startOfDaySteps", "$startOfDaySteps"),
            Pair("dailySteps", "${dailySteps.value}"),
            Pair(DAILY_STEPS_KEY, "${sharedPreferences.getInt(DAILY_STEPS_KEY, 0)}"),
            Pair(DAY_OF_LAST_READ_KEY, "${LocalDate.ofEpochDay(sharedPreferences.getLong(DAY_OF_LAST_READ_KEY, 0))}"),
            Pair(STEP_COUNTER_KEY, "${sharedPreferences.getInt(STEP_COUNTER_KEY, 0)}"),
            Pair(LAST_MIDNIGHT_KEY, "${LocalDateTime.ofEpochSecond(sharedPreferences.getLong(LAST_MIDNIGHT_KEY, 0), 0, ZoneOffset.UTC)}"),
        )
    }

}