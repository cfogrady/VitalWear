package com.github.cfogrady.vitalwear.steps

import android.content.SharedPreferences
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class StepIOService(private val sharedPreferences: SharedPreferences, private val stepState: StepState) {
    companion object {
        const val DAILY_STEPS_KEY = "DAILY_STEPS"
        const val DAY_OF_LAST_READ_KEY = "DAY_OF_LAST_READ"
        const val STEP_COUNTER_KEY = "STEP_COUNTER_VALUE"
        const val LAST_MIDNIGHT_KEY = "LAST_MIDNIGHT"
    }

    class StepPreferences(
        val dailySteps: Int,
        val lastStepReading: Int,
        val timeLastReadDaysFromEpoch: Long,
    ) {
        companion object {
            fun fromStepState(stepState: StepState): StepPreferences {
                return StepPreferences(stepState.dailySteps.value,
                    stepState.lastStepReading,
                    stepState.dateOfLastRead.toEpochDay())
            }
        }
    }

    /**
     * Caller is expected to commit or apply these changes
     */
    fun editStepPreferenceUpdates(now: LocalDate, sharedPreferencesEditor: SharedPreferences.Editor = sharedPreferences.edit()) : SharedPreferences.Editor {
        if(!stepState.stepSensorEnabled) {
            return sharedPreferencesEditor
        }
        return sharedPreferencesEditor.putInt(DAILY_STEPS_KEY, stepState.dailySteps.value)
            .putInt(STEP_COUNTER_KEY, stepState.lastStepReading)
            .putLong(DAY_OF_LAST_READ_KEY, now.toEpochDay())
    }

    fun stepPreferences(): StepPreferences {
        val dailyStepsBeforeShutdown = sharedPreferences.getInt(DAILY_STEPS_KEY, 0)
        val lastStepCounter = sharedPreferences.getInt(STEP_COUNTER_KEY, 0)
        val daysSinceEpoch = sharedPreferences.getLong(DAY_OF_LAST_READ_KEY, 0)
        return StepPreferences(dailyStepsBeforeShutdown, lastStepCounter, daysSinceEpoch)
    }

    fun debug(): List<Pair<String, String>> {
        return listOf(
            Pair(DAILY_STEPS_KEY, "${sharedPreferences.getInt(DAILY_STEPS_KEY, 0)}"),
            Pair(DAY_OF_LAST_READ_KEY, "${LocalDate.ofEpochDay(sharedPreferences.getLong(DAY_OF_LAST_READ_KEY, 0))}"),
            Pair(STEP_COUNTER_KEY, "${sharedPreferences.getInt(STEP_COUNTER_KEY, 0)}"),
            Pair(LAST_MIDNIGHT_KEY, "${LocalDateTime.ofEpochSecond(sharedPreferences.getLong(LAST_MIDNIGHT_KEY, 0), 0, ZoneOffset.UTC)}"),
        )
    }
}