package com.github.cfogrady.vitalwear.steps

import android.content.SharedPreferences
import timber.log.Timber
import java.time.LocalDate

class StepIOService(private val sharedPreferences: SharedPreferences, private val stepState: StepState) {
    companion object {
        const val DAILY_STEPS_KEY = "DAILY_STEPS"
        const val DAY_OF_LAST_READ_KEY = "DAY_OF_LAST_READ" // Last Save Date
        const val STEP_COUNTER_KEY = "STEP_COUNTER_VALUE"
    }

    class StepPreferences(
        val dailySteps: Int,
        val lastStepReading: Int,
        val dateOfLastSave: LocalDate,
    ) {
    }

    /**
     * Caller is expected to commit or apply these changes
     */
    fun editStepPreferenceUpdates(now: LocalDate, sharedPreferencesEditor: SharedPreferences.Editor = sharedPreferences.edit()) : SharedPreferences.Editor {
        if(!stepState.stepSensorEnabled) {
            return sharedPreferencesEditor
        }
        val saveDate = now.toEpochDay()
        Timber.i("Saving Saved Daily Steps: ${stepState.dailySteps.value}")
        Timber.i("Saving Date Of Last Read: $saveDate")
        Timber.i("Saving Last Step Counter: ${stepState.lastStepReading}")
        return sharedPreferencesEditor.putInt(DAILY_STEPS_KEY, stepState.dailySteps.value)
            .putInt(STEP_COUNTER_KEY, stepState.lastStepReading)
            .putLong(DAY_OF_LAST_READ_KEY, saveDate)
    }

    fun stepPreferences(): StepPreferences {
        val dailyStepsBeforeShutdown = sharedPreferences.getInt(DAILY_STEPS_KEY, 0)
        val lastStepCounter = sharedPreferences.getInt(STEP_COUNTER_KEY, 0)
        val dateLastRead = LocalDate.ofEpochDay(sharedPreferences.getLong(DAY_OF_LAST_READ_KEY, 0))
        Timber.i("Loading Saved Daily Steps: $dailyStepsBeforeShutdown")
        Timber.i("Loading Date Of Last Read: $dateLastRead")
        Timber.i("Loading Last Step Counter: $lastStepCounter")
        return StepPreferences(dailyStepsBeforeShutdown, lastStepCounter, dateLastRead)
    }
}