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
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
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
    private val characterManager: CharacterManager,
    private val sharedPreferences: SharedPreferences,
    private val sensorManager: SensorManager): StepService, DailyStepHandler {
    companion object {
        const val TAG = "StepsService"
        const val STEPS_PER_VITAL = 50
        const val DAILY_STEPS_KEY = "DAILY_STEPS"
        const val DAY_OF_LAST_READ_KEY = "DAY_OF_LAST_READ"
        private const val WORK_TAG = "StepsService"

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
    var startOfDaySteps = 0
    override val dailySteps = MutableLiveData(0)

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

    override fun addStepsToVitals(): Future<Void> {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        // This relies on quickly receiving an event describing current value.
        var future = CompletableFuture<Void>()
        val listener = StepSensorListener(){ value ->
            newSteps(value)
            future.complete(null)
        }
        //future.accept doesn't work unless something is waiting for that future
        val newFuture = future.thenAccept() {
            sensorManager.unregisterListener(listener)
            Log.i(TAG, "Unregistered Sensor Listener")
        }
        if(!sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)) {
            Log.e(TAG, "Failed to register sensor!")
        } else {
            Log.i(TAG, "Registered Sensor Listener")
        }
        return newFuture
    }

    override fun listenDailySteps(lifecycleOwner: LifecycleOwner): LiveData<Int> {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // This relies on quickly receiving an event describing current value.
        val listener = StepSensorListener(){ value ->
            Log.i(TAG, "Steps triggered")
            newSteps(value)
        }
        if(!sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)) {
            Log.e(TAG, "Failed to register sensor!")
        } else {
            Log.i(TAG, "Registered Listener to sensor.")
            val onStopObserver = OnDestroyLifecycleObserver() {
                Log.i(TAG, "Unregistered to sensor.")
                sensorManager.unregisterListener(listener)
            }
            lifecycleOwner.lifecycle.addObserver(onStopObserver)
        }
        return dailySteps
    }

    private fun logStartOfDaySteps(now: LocalDate) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                sharedPreferences.edit().putInt(DAILY_STEPS_KEY, dailySteps.value!!)
                sharedPreferences.edit().putLong(DAY_OF_LAST_READ_KEY, now.toEpochDay())
            }
        }
    }

    override fun handleDayTransition(newDay: LocalDate) {
        addStepsToVitals()
        startOfDaySteps = currentSteps
        dailySteps.postValue(0)
        logStartOfDaySteps(newDay)
    }

    fun handleBoot(today: LocalDate) {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        // This relies on quickly receiving an event describing current value.
        val future = CompletableFuture<Void>()
        val listener = StepSensorListener(){ value ->
            stepsAtBoot(value, today)
            future.complete(null)
        }
        future.thenAccept() {
            sensorManager.unregisterListener(listener)
            Log.i(TAG, "Unregistered sensor from handle boot successfully")
        }
        if(!sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)) {
            Log.e(TAG, "Failed to register sensor!")
        }
    }

    fun handleShutdown(today: LocalDate) {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        // This relies on quickly receiving an event describing current value.
        val future = CompletableFuture<Void>()
        val listener = StepSensorListener(){ value ->
            newSteps(value)
            sharedPreferences.edit().putLong(DAY_OF_LAST_READ_KEY, today.toEpochDay())
            sharedPreferences.edit().putInt(DAILY_STEPS_KEY, dailySteps.value!!)
            future.complete(null)
        }
        //hope this complete before the shutdown finishes
        future.thenAccept() {
            sensorManager.unregisterListener(listener)
            Log.i(TAG, "Unregistered sensor from handle boot successfully")
        }
        if(!sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)) {
            Log.e(TAG, "Failed to register sensor!")
        }
    }

    private fun stepsAtBoot(steps: Int, today: LocalDate) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val dailyStepsBeforeShutdown = sharedPreferences.getInt(DAILY_STEPS_KEY, 0)
                val timeSinceEpoch = sharedPreferences.getLong(DAY_OF_LAST_READ_KEY, 0)
                val dateFromSave = LocalDate.ofEpochDay(timeSinceEpoch)
                if(dateFromSave != today) {
                    startOfDaySteps = steps
                    sharedPreferences.edit().putLong(DAY_OF_LAST_READ_KEY, today.toEpochDay())
                    dailySteps.postValue(0)
                } else {
                    dailySteps.postValue(dailyStepsBeforeShutdown)
                    startOfDaySteps = steps - dailyStepsBeforeShutdown
                }
                currentSteps = steps
            }
        }
    }
}