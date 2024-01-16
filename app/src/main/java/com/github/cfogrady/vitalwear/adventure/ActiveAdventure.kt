package com.github.cfogrady.vitalwear.adventure

import android.content.Context
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity
import com.github.cfogrady.vitalwear.util.BridgedSensorEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ActiveAdventure(private val context: Context, private val service: AdventureService, private val adventures: List<AdventureEntity>, val backgrounds: List<Bitmap>, private var currentZone: Int, val partnerId: Int) : SensorEventListener, BridgedSensorEventListener {
    companion object {
        const val TAG = "ActiveAdventure"
    }

    private var startingStep: Int? = null
    private val internalCurrentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = internalCurrentStep
    private val internalZoneCompleted = MutableStateFlow(false)
    val zoneCompleted: StateFlow<Boolean> = internalZoneCompleted

    fun stepsTowardsGoal(): Int {
        if(startingStep == null) {
            return 0
        }
        Log.i(TAG, "Steps towards goal: ${internalCurrentStep.value - startingStep!!}")
        return internalCurrentStep.value - startingStep!!
    }

    fun goal(): Int {
        return adventures[currentZone].steps
    }

    fun currentBackground(): Bitmap {
        return backgrounds[adventures[currentZone].walkingBackgroundId]
    }

    fun currentAdventureEntity(): AdventureEntity {
        return adventures[currentZone]
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            sensorChanged(event.sensor.type, event.timestamp, event.values)
        }
    }

    override fun sensorChanged(sensorType: Int, timestamp: Long, values: FloatArray) {
        internalCurrentStep.value = values[0].toInt()
        if (startingStep == null) {
            startingStep = internalCurrentStep.value - 1
        }
        checkSteps()
    }

    fun checkSteps() {
        if(internalCurrentStep.value - startingStep!! >= adventures[currentZone].steps && !internalZoneCompleted.value) {
            internalZoneCompleted.value = true
            service.notifyZoneCompletion(context)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(TAG, "step sensor accuracy changed to: $accuracy")
    }

    fun finishZone(moveToNext: Boolean) {
        val goalSteps = adventures[currentZone].steps
        startingStep = startingStep!! + goalSteps
        if(moveToNext) {
            currentZone++
        }
        internalZoneCompleted.value = false
        Handler.createAsync(Looper.getMainLooper()).postDelayed(this::checkSteps, 500)
    }
}