package com.github.cfogrady.vitalwear.character.mood

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.github.cfogrady.vitalwear.SaveService
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.character.VBUpdater
import com.github.cfogrady.vitalwear.character.data.Mood
import com.github.cfogrady.vitalwear.heartrate.HeartRateResult
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import com.github.cfogrady.vitalwear.vitals.VitalService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class MoodService(
    private val heartRateService: HeartRateService,
    private val sensorManager: SensorManager,
    private val vbUpdater: VBUpdater,
    private val characterManager: CharacterManager,
    private val vitalService: VitalService,
    private val saveService: SaveService,
    private val localDateTimeProvider: () -> LocalDateTime = LocalDateTime::now) {

    var offBodySensor: Sensor? = null
    var lastWorn: LocalDateTime? = null
    private var lastLevel : Int = 0

    @Synchronized fun initialize() {
        if (offBodySensor == null) {
            val maybeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT)
            if (maybeSensor == null) {
                Timber.e("No off body sensor!")
            } else {
                offBodySensor = maybeSensor
                if(!sensorManager.registerListener(offBodySensorEvenListener, offBodySensor, SensorManager.SENSOR_DELAY_NORMAL)) {
                    Timber.e("Can't setup body sensor!")
                }
            }
        }
//        vbUpdater.scheduleExactMoodUpdates()
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
            Timber.i("OffBodySensor Accuracy changed: $accuracy")
        }

    }

    private fun handleDeviceTakenOff() {
        Timber.i("Device removed")
        vbUpdater.unRegisterMoodUpdates()
        lastWorn = localDateTimeProvider.invoke()
    }

    private fun handleDevicePutOn() {
        Timber.i("Device being worn again")
        if(lastWorn != null) {
            val current = localDateTimeProvider.invoke()
            val minutesSinceLastWorn = ChronoUnit.MINUTES.between(lastWorn, current).toInt()
            val updateEvents = minutesSinceLastWorn/5
            characterManager.getCurrentCharacter()?.let {
                if(!it.characterStats.sleeping) {
                    val vitalDecrease = -20*updateEvents
                    vitalService.addVitals("untethered for $minutesSinceLastWorn", it, vitalDecrease)
                    val moodDecrease = updateEvents
                    it.characterStats.mood = (it.characterStats.mood - moodDecrease).coerceAtLeast(0)
                    lastLevel = 0
                    it.characterStats.updateTimeStamps(current)
                    logMoodUpdate(it.mood(), it.characterStats.mood, "Device Back On|$minutesSinceLastWorn|Vitals Lost|$vitalDecrease|Mood Decrease|$moodDecrease")
                }
            }
            if(minutesSinceLastWorn >= 24*60) {
                Timber.i("Device not worn for 24 hours. Character Death")
                // TODO: Death
            }
        }
        vbUpdater.scheduleExactMoodUpdates()
        saveService.saveAsync()
    }

    fun updateMood(now: LocalDateTime) {
        characterManager.getCurrentCharacter()?.let {character ->
            if(!character.characterStats.sleeping) {
                CoroutineScope(Dispatchers.Default).launch {
                    updateCharacterMood(character, now)
                    saveService.save()
                }
            }
        }
    }

    private suspend fun updateCharacterMood(character: VBCharacter, now: LocalDateTime) {
        try {
            val exerciseLevel = heartRateService.getExerciseLevel(lastLevel)
            vitalService.processVitalsFromHeartRate(character, exerciseLevel.heartRate.heartRate, heartRateService.restingHeartRate())
            val heartRateResultStr = if(exerciseLevel.heartRate.heartRateError == HeartRateResult.Companion.HeartRateError.NONE) "${exerciseLevel.heartRate.heartRate}" else exerciseLevel.heartRate.heartRateError.name
            updateFromExerciseLevel(character, exerciseLevel.level, now)
            lastLevel = exerciseLevel.level
            logMoodUpdate(character.mood(), character.characterStats.mood, "Heart Rate Update|$heartRateResultStr|Exercise Level|${exerciseLevel.level}")
        } catch (ise: IllegalStateException) {
            // primarily caused in emulator by lack of step sensor
            Timber.e("Failed to update mood", ise)
        }
    }

    private fun logMoodUpdate(mood: Mood, rawMood: Int, reason: String) {
        Timber.i("Mood|$rawMood|$mood|$reason")
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
}