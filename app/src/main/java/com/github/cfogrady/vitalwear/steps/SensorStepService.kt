package com.github.cfogrady.vitalwear.steps

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
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
            Pair(DAY_OF_LAST_READ_KEY, "${LocalDate.ofEpochDay(sharedPreferences.getLong(DAY_OF_LAST_READ_KEY, 0))}"),
            Pair(STEP_COUNTER_KEY, "${sharedPreferences.getInt(STEP_COUNTER_KEY, 0)}"),
            Pair(LAST_MIDNIGHT_KEY, "${LocalDateTime.ofEpochSecond(sharedPreferences.getLong(LAST_MIDNIGHT_KEY, 0), 0, ZoneOffset.UTC)}"),
        )
    }

    private suspend fun newSteps(newStepCount: Int) {
        Log.i(TAG, "StepCount: $newStepCount")
        val character = getCharacter()
        if(currentSteps != 0 && character != BEMCharacter.DEFAULT_CHARACTER) {
            if(newStepCount - currentSteps >= remainingSteps) {
                currentSteps += remainingSteps
                character.addVitals(vitalGainModifier(4))
                val newVitals = 4 * ((newStepCount - currentSteps)/STEPS_PER_VITAL)
                character.addVitals(vitalGainModifier(newVitals))
                remainingSteps = (newStepCount - currentSteps) % STEPS_PER_VITAL
            }
            currentSteps = newStepCount
        }
        withContext(Dispatchers.Main) {
            dailySteps.value = currentSteps - startOfDaySteps
        }
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

    private suspend fun getSingleSensorReading(): Int {
        var deferred = CompletableDeferred<Int>()
        SingleStepSensorListener(sensorManager, deferred)
        return deferred.await()
    }

    override suspend fun addStepsToVitals() {
        val newStepCounter = getSingleSensorReading()
        newSteps(newStepCounter)
    }

    override fun listenDailySteps(): StepListener {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // This relies on quickly receiving an event describing current value.
        val listener = StepSensorListener(){ value ->
            Log.i(TAG, "Steps triggered")
            runBlocking {
                newSteps(value)
            }
        }
        if(!sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)) {
            Log.e(TAG, "Failed to register sensor!")
        } else {
            Log.i(TAG, "Registered Listener to sensor.")
        }
        return StepListener(dailySteps, sensorManager, listener)
    }

    fun stepPreferenceUpdates(now: LocalDate, sharedPreferencesEditor: Editor = sharedPreferences.edit()) : Editor {
        return sharedPreferencesEditor.putInt(DAILY_STEPS_KEY, dailySteps.value!!)
            .putInt(STEP_COUNTER_KEY, currentSteps)
            .putLong(DAY_OF_LAST_READ_KEY, now.toEpochDay())
    }

    override suspend fun handleDayTransition(newDay: LocalDate) {
        addStepsToVitals()
        withContext(Dispatchers.Main) {
            startOfDaySteps = currentSteps
            dailySteps.value = 0
        }
    }

    /**
     * Handles the a boot up of the application.
     * If the return value is true, a call to the SaveService should be made.
     */
    suspend fun handleBoot(today: LocalDate) : Boolean {
        Log.i(TAG, "Handling startup")
        val stepCounterOnBoot = getSingleSensorReading()
        //TODO: Sometimes this isn't called for several minutes after startup because we don't get readings from the sensor.
        // See if there is a way to fix that. We generally rely on fast sensor readings... and we generally get fast sensor readings.
        // But sometimes we don't...
        return stepsAtBoot(stepCounterOnBoot, today)
    }

    private suspend fun stepsAtBoot(curentStepCounter: Int, today: LocalDate): Boolean {
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
            withContext(Dispatchers.Main) {
                dailySteps.value = 0
            }
            return true //should save because we are starting fresh
        } else if(lastStepCounter > curentStepCounter) {
            // we reset the step counter, so assume a reboot
            Log.i(TAG, "Restarting steps from device reboot")
            startOfDaySteps = curentStepCounter - dailyStepsBeforeShutdown
            currentSteps = lastStepCounter
            withContext(Dispatchers.Main) {
                dailySteps.value = dailyStepsBeforeShutdown
            }
            return true //should save because we are starting fresh
        } else {
            // App shutdown and restarted. We're on the same day.
            Log.i(TAG, "Restarting steps from app restart")
            currentSteps = lastStepCounter
            startOfDaySteps = lastStepCounter - dailyStepsBeforeShutdown
            newSteps(curentStepCounter)
            return false
        }
    }

}