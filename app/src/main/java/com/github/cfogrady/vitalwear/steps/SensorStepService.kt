package com.github.cfogrady.vitalwear.steps

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.hardware.SensorManager
import android.util.Log
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.Mood
import com.github.cfogrady.vitalwear.util.SensorThreadHandler
import com.github.cfogrady.vitalwear.vitals.VitalService
import kotlinx.coroutines.*
import java.time.*

/**
 * This architecture for this service is as follows.
 * When an activity is running (the app is in the foreground)
 *   Listen to the service, until the activity exits.
 * When an update is made in the background that requires vitals be up-to-date
 *   Register. This first event should come back immediately with the current count.
 *   Calculate that value and de-register. This should be before mood is changed, before evolution
 *   is determined, before before device shutdown.
 * Special logic when the app starts up or is shutdown to handle stepcounter resets
 */
class SensorStepService(
    private val sharedPreferences: SharedPreferences,
    private val sensorManager: SensorManager,
    private val sensorThreadHandler: SensorThreadHandler,
    private val stepChangeListeners: List<StepChangeListener>): StepService, DailyStepHandler {
    companion object {
        const val TAG = "StepsService"
        const val DAILY_STEPS_KEY = "DAILY_STEPS"
        const val DAY_OF_LAST_READ_KEY = "DAY_OF_LAST_READ"
        const val STEP_COUNTER_KEY = "STEP_COUNTER_VALUE"
        private const val WORK_TAG = "StepsService"
        const val LAST_MIDNIGHT_KEY = "LAST_MIDNIGHT"

        fun setupDailyStepReset(context: Context) {
            val now = LocalDateTime.now()
            val midnight = LocalDate.now().plusDays(1).atStartOfDay()
            val durationUntilMidnight = Duration.between(now, midnight)
            val stepCounterResetWorkRequest = PeriodicWorkRequestBuilder<DailyStepWorker>(
                Duration.ofDays(1))
                .setInitialDelay(durationUntilMidnight)
                .addTag(WORK_TAG)
                .build()
            val workManager = WorkManager.getInstance(context)
            workManager.cancelAllWorkByTag(WORK_TAG)
            workManager.enqueue(stepCounterResetWorkRequest)
        }
    }

    private var currentSteps = 0

    private var startOfDaySteps = 0
    private var dailySteps = 0

    fun debug(): List<Pair<String, String>> {
        return listOf(
            Pair("currentSteps", "$currentSteps"),
            Pair("startOfDaySteps", "$startOfDaySteps"),
            Pair("dailySteps", "$dailySteps"),
            Pair(DAILY_STEPS_KEY, "${sharedPreferences.getInt(DAILY_STEPS_KEY, 0)}"),
            Pair(DAY_OF_LAST_READ_KEY, "${LocalDate.ofEpochDay(sharedPreferences.getLong(DAY_OF_LAST_READ_KEY, 0))}"),
            Pair(STEP_COUNTER_KEY, "${sharedPreferences.getInt(STEP_COUNTER_KEY, 0)}"),
            Pair(LAST_MIDNIGHT_KEY, "${LocalDateTime.ofEpochSecond(sharedPreferences.getLong(LAST_MIDNIGHT_KEY, 0), 0, ZoneOffset.UTC)}"),
        )
    }

    /**
     * Process new steps since the last step counter reading.
     * Returns the daily steps after processing the new steps.
     */
    private fun processNewSteps(newStepCount: Int) : Int {
        Log.i(TAG, "StepCount: $newStepCount")
        for(stepChangeListener in stepChangeListeners) {
            stepChangeListener.processStepChanges(currentSteps, newStepCount)
        }
        currentSteps = newStepCount
        dailySteps = currentSteps - startOfDaySteps
        return dailySteps
    }

    private suspend fun getSingleSensorReading(): Int {
        var deferred = CompletableDeferred<Int>()
        SingleStepSensorListener(sensorManager, sensorThreadHandler, deferred)
        return deferred.await()
    }

    override suspend fun addStepsToVitals() {
        val newStepCounter = getSingleSensorReading()
        processNewSteps(newStepCounter)
    }

    override fun listenDailySteps(): ManyStepListener {
        return ManyStepProcessingListener(dailySteps, this::processNewSteps, sensorManager, sensorThreadHandler)
    }

    fun stepPreferenceUpdates(now: LocalDate, sharedPreferencesEditor: Editor = sharedPreferences.edit()) : Editor {
        return sharedPreferencesEditor.putInt(DAILY_STEPS_KEY, dailySteps)
            .putInt(STEP_COUNTER_KEY, currentSteps)
            .putLong(DAY_OF_LAST_READ_KEY, now.toEpochDay())
    }

    override suspend fun handleDayTransition(newDay: LocalDate) {
        addStepsToVitals()
        startOfDaySteps = currentSteps
        dailySteps = 0
    }

    /**
     * Handles the a boot up of the application.
     * If the return value is true, a call to the SaveService should be made.
     */
    suspend fun handleBoot(today: LocalDate) : Boolean {
        Log.i(TAG, "Handling startup")
        // For some reason this call seems to wait until we receive a step change before actually registering a change.
        val stepCounterOnBoot = getSingleSensorReading()
        //TODO: Sometimes this isn't called for several minutes after startup because we don't get readings from the sensor.
        // See if there is a way to fix that. We generally rely on fast sensor readings... and we generally get fast sensor readings.
        // But sometimes we don't...
        return stepsAtBoot(stepCounterOnBoot, today)
    }

    private fun stepsAtBoot(curentStepCounter: Int, today: LocalDate): Boolean {
        Log.i(TAG, "Performing steps on app startup")
        val dailyStepsBeforeShutdown = sharedPreferences.getInt(DAILY_STEPS_KEY, 0)
        val lastStepCounter = sharedPreferences.getInt(STEP_COUNTER_KEY, 0)
        val timeSinceEpoch = sharedPreferences.getLong(DAY_OF_LAST_READ_KEY, 0)
        val dateFromSave = LocalDate.ofEpochDay(timeSinceEpoch)
        if(dateFromSave != today) {
            // we're on a different day than the last save, so reset everything
            Log.i(TAG, "Restarting steps with new day")
            startOfDaySteps = curentStepCounter
            currentSteps = curentStepCounter
            dailySteps = 0
            return true //should save because we are starting fresh
        } else if(lastStepCounter > curentStepCounter) {
            // we reset the step counter, so assume a reboot
            Log.i(TAG, "Restarting steps from device reboot")
            startOfDaySteps = curentStepCounter - dailyStepsBeforeShutdown
            currentSteps = lastStepCounter
            dailySteps = dailyStepsBeforeShutdown
            return true //should save because we are starting fresh
        } else {
            // App shutdown and restarted. We're on the same day.
            Log.i(TAG, "Restarting steps from app restart")
            currentSteps = lastStepCounter
            startOfDaySteps = lastStepCounter - dailyStepsBeforeShutdown
            processNewSteps(curentStepCounter)
            return false
        }
    }

}