package com.github.cfogrady.vitalwear.steps

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.*
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.Mood
import kotlinx.coroutines.*
import java.time.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

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
    //TODO: In addition to on onBoot and onShutdown, we need onStart and onAppShutdown. This will preserve steps over application reboots
    private val characterManager: CharacterManager,
    private val sharedPreferences: SharedPreferences,
    private val sensorManager: SensorManager): StepService, DailyStepHandler {
    companion object {
        const val TAG = "StepsService"
        const val STEPS_PER_VITAL = 50
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
    private var remainingSteps = STEPS_PER_VITAL
    private var startOfDaySteps = 0
    override val dailySteps = MutableLiveData(0)

    fun debug(): List<Pair<String, String>> {
        return listOf(
            Pair("currentSteps", "$currentSteps"),
            Pair("remainingSteps", "$remainingSteps"),
            Pair("startOfDaySteps", "$startOfDaySteps"),
            Pair("dailySteps", "${dailySteps.value}"),
            Pair(DAILY_STEPS_KEY, "${sharedPreferences.getInt(DAILY_STEPS_KEY, 0)}"),
            Pair(DAY_OF_LAST_READ_KEY, "${LocalDateTime.ofEpochSecond(sharedPreferences.getLong(DAY_OF_LAST_READ_KEY, 0), 0, ZoneOffset.UTC)}"),
            Pair(STEP_COUNTER_KEY, "${sharedPreferences.getInt(STEP_COUNTER_KEY, 0)}"),
            Pair(LAST_MIDNIGHT_KEY, "${LocalDateTime.ofEpochSecond(sharedPreferences.getLong(LAST_MIDNIGHT_KEY, 0), 0, ZoneOffset.UTC)}"),
        )
    }

    private fun newSteps(newStepCount: Int) {
        Log.i(TAG, "StepCount: $newStepCount")
        val character = getCharacter()
        if(character == BEMCharacter.DEFAULT_CHARACTER) {
            Log.w(TAG, "No character set")
            return
        }
        if(currentSteps != 0) {
            if(newStepCount - currentSteps >= remainingSteps) {
                val stats = character.characterStats
                currentSteps += remainingSteps
                stats.vitals += vitalGainModifier(4)
                val newVitals = (newStepCount - currentSteps)/STEPS_PER_VITAL
                stats.vitals += vitalGainModifier(newVitals)
                remainingSteps = (newStepCount - currentSteps) % STEPS_PER_VITAL
            }
        }
        currentSteps = newStepCount
        dailySteps.postValue(currentSteps - startOfDaySteps)
    }

    private fun getCharacter() : BEMCharacter {
        return characterManager.getCurrentCharacter()
    }

    private fun vitalGainModifier(vitals: Int) : Int {
        val character = getCharacter()
        if(character == BEMCharacter.DEFAULT_CHARACTER) {
            Log.w(TAG, "Cannot apply vitals gain modifier for null active character.")
            return vitals
        }
        if(character.characterStats.injured) {
            return vitals/2
        }
        return when(character.mood()) {
            Mood.NORMAL -> vitals
            Mood.GOOD -> vitals * 2
            Mood.BAD -> vitals/2
        }
    }

    private fun getSingleSensorReading(onReading: (Int) -> Unit): CompletableFuture<Void> {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        var future = CompletableFuture<Void>()
        val listener = StepSensorListener(){ value ->
            onReading.invoke(value)
            future.complete(null)
        }
        val newFuture = future.thenAccept() {
            sensorManager.unregisterListener(listener)
            Log.i(TAG, "Unregistered Sensor Listener")
        }
        //TODO: add handler thread (https://stackoverflow.com/questions/3286815/sensoreventlistener-in-separate-thread)
        // Normally the sensor events come through on the main thread. This means we can't block the main thread and still receive events.
        // We need to block the main thread for the shutdown, so that we can ensure everything is saved before the shutdown occurs;
        // otherwise, there is a risk that the shutdown will occur before the other threads have finished.
        if(!sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)) {
            Log.e(TAG, "Failed to register sensor!")
        }
        return newFuture
    }

    override fun addStepsToVitals(): Future<Void> {
        return getSingleSensorReading(this::newSteps)
    }

    override fun listenDailySteps(): StepListener {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // This relies on quickly receiving an event describing current value.
        val listener = StepSensorListener(){ value ->
            Log.i(TAG, "Steps triggered")
            newSteps(value)
        }
        if(!sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)) {
            Log.e(TAG, "Failed to register sensor!")
        } else {
            Log.i(TAG, "Registered Listener to sensor.")
        }
        return StepListener(dailySteps, sensorManager, listener)
    }

    private fun saveStepData(now: LocalDate) {
        sharedPreferences.edit().putInt(DAILY_STEPS_KEY, dailySteps.value!!)
            .putInt(STEP_COUNTER_KEY, currentSteps)
            .putLong(DAY_OF_LAST_READ_KEY, now.toEpochDay())
            .putInt(DAILY_STEPS_KEY, dailySteps.value!!)
            .commit()
        Log.i(TAG, "Step data saved")
    }

    override fun handleDayTransition(newDay: LocalDate) {
        addStepsToVitals()
        startOfDaySteps = currentSteps
        dailySteps.postValue(0)
        saveStepData(newDay)
        sharedPreferences.edit().putInt(DAILY_STEPS_KEY, dailySteps.value!!)
            .putLong(LAST_MIDNIGHT_KEY, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
            .commit()
    }

    fun getLastMignight() : LocalDateTime {
        val epochSeconds = sharedPreferences.getLong(LAST_MIDNIGHT_KEY, 0)
        return LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC)
    }

    fun handleBoot(today: LocalDate) {
        Log.i(TAG, "Handling startup")
        getSingleSensorReading { newSteps ->
            stepsAtBoot(newSteps, today)
        }
    }

    fun handleShutdown(today: LocalDate): CompletableFuture<Void> {
        return getSingleSensorReading {value ->
            newSteps(value)
            saveStepData(today)
        }
    }

    private fun stepsAtBoot(steps: Int, today: LocalDate) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val dailyStepsBeforeShutdown = sharedPreferences.getInt(DAILY_STEPS_KEY, 0)
                val lastStepCounter = sharedPreferences.getInt(STEP_COUNTER_KEY, 0)
                val timeSinceEpoch = sharedPreferences.getLong(DAY_OF_LAST_READ_KEY, 0)
                val dateFromSave = LocalDate.ofEpochDay(timeSinceEpoch)
                if(dateFromSave != today) {
                    // we're on a different day than the last save, so reset everything
                    startOfDaySteps = steps
                    currentSteps = steps
                    dailySteps.postValue(0)
                    saveStepData(today)
                } else if(lastStepCounter < steps) {
                    // we reset the step counter, so assume a reboot
                    startOfDaySteps = steps - dailyStepsBeforeShutdown
                    currentSteps = lastStepCounter
                    dailySteps.postValue(dailyStepsBeforeShutdown)
                    saveStepData(today)
                } else {
                    // App shutdown and restarted. We're on the same day.
                    currentSteps = lastStepCounter
                    startOfDaySteps = steps - dailyStepsBeforeShutdown
                    newSteps(steps)
                }
            }
        }
    }

}