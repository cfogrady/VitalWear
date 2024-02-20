package com.github.cfogrady.vitalwear.character.mood

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.character.VBUpdater
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.debug.Debuggable
import com.github.cfogrady.vitalwear.heartrate.HeartRateResult
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import com.github.cfogrady.vitalwear.steps.SensorStepService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.LinkedList

class MoodService(
    private val heartRateService: HeartRateService,
    private val stepService: SensorStepService,
    private val sensorManager: SensorManager,
    private val vbUpdater: VBUpdater,
    private val characterManager: CharacterManager,
    private val gameState: StateFlow<GameState>,
    private val localDateTimeProvider: () -> LocalDateTime = LocalDateTime::now): Debuggable {

    companion object {
        const val TAG = "MoodService"
        const val DEBUG_HISTORY = 100
    }

    var offBodySensor: Sensor? = null
    var lastWorn: LocalDateTime? = null
    private var lastLevel : Int = 0

    @Synchronized fun initialize() {
        if (offBodySensor == null) {
            offBodySensor = sensorManager.getDefaultSensor(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT)!!
            if(!sensorManager.registerListener(offBodySensorEvenListener, offBodySensor, SensorManager.SENSOR_DELAY_NORMAL)) {
                Log.e(TAG, "Can't setup body sensor!")
            }
            vbUpdater.scheduleExactMoodUpdates()
        }
    }

    private val offBodySensorEvenListener = object: SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            val offBody = event!!.values[0] == 0.0f
            if(offBody) {
                handleDeviceTakenOff()
            } else {
                handleDevicePutOn()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            Log.i(TAG, "OffBodySensor Accuracy changed: $accuracy")
        }

    }

    private fun handleDeviceTakenOff() {
        vbUpdater.unRegisterMoodUpdates()
        lastWorn = localDateTimeProvider.invoke()
    }

    private fun handleDevicePutOn() {
        if(lastWorn != null && gameState.value != GameState.SLEEPING) {
            val current = localDateTimeProvider.invoke()
            val minutesSinceLastWorn = ChronoUnit.MINUTES.between(lastWorn, current).toInt()
            val updateEvents = minutesSinceLastWorn/5
            characterManager.getCurrentCharacter()?.let {
                val vitalDecrease = 20*updateEvents
                it.characterStats.vitals = (it.characterStats.vitals - vitalDecrease).coerceAtLeast(0)
                val moodDecrease = updateEvents
                it.characterStats.mood = (it.characterStats.mood - moodDecrease).coerceAtLeast(0)
                lastLevel = 0
                it.characterStats.updateTimeStamps(current)
                events.addFirst(Pair("$current", "Device back on after $minutesSinceLastWorn minutes. Lost $vitalDecrease vitals and $moodDecrease mood"))
                while(events.size > DEBUG_HISTORY) {
                    events.removeLast()
                }
            }
            if(minutesSinceLastWorn >= 24*60) {
                Log.i(TAG, "Device not worn for 24 hours. Character Death")
                // TODO: Death
            }
        }
        vbUpdater.scheduleExactMoodUpdates()
    }

    fun updateMood(now: LocalDateTime) {
        characterManager.getCurrentCharacter()?.let {character ->
            if(gameState.value != GameState.SLEEPING) {
                CoroutineScope(Dispatchers.Default).launch {
                    updateCharacterMood(character, now)
                }
            }
        }
    }

    private suspend fun updateCharacterMood(character: VBCharacter, now: LocalDateTime) {
        try {
            stepService.addStepsToVitals()
            val exerciseLevel = heartRateService.getExerciseLevel(lastLevel)
            val heartRateResultStr = if(exerciseLevel.heartRate.heartRateError == HeartRateResult.Companion.HeartRateError.NONE) "${exerciseLevel.heartRate.heartRate}" else exerciseLevel.heartRate.heartRateError.name
            updateFromExerciseLevel(character, exerciseLevel.level, now)
            lastLevel = exerciseLevel.level
            events.addFirst(Pair("$now", "Heart Rate: $heartRateResultStr yielding level ${exerciseLevel.level} and mood ${character.characterStats.mood}"))
            while(events.size > DEBUG_HISTORY) {
                events.removeLast()
            }
            Log.i(TAG, "Mood updated successfully")
        } catch (ise: IllegalStateException) {
            // primarily caused in emulator by lack of step sensor
            Log.e(TAG, "Failed to update mood", ise)
        }
    }

    private fun updateFromExerciseLevel(character: VBCharacter, exerciseLevel: Int, now: LocalDateTime) {
        when(exerciseLevel) {
            0 -> character.characterStats.mood -= 1
            2 -> character.characterStats.mood += 10
            3 -> character.characterStats.mood += 15
        }
        if(character.characterStats.mood < 0) {
            character.characterStats.mood = 0
        } else if(character.characterStats.mood > 100) {
            character.characterStats.mood = 100
        }
        character.characterStats.updateTimeStamps(now)
    }

    private val events = LinkedList<Pair<String, String>>()

    override fun debug(): List<Pair<String, String>> {
        return events
    }
}