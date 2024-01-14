package com.github.cfogrady.vitalwear.adventure

import android.content.Context
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ActiveAdventure(private val context: Context, private val service: AdventureService, private val adventures: List<AdventureEntity>, val backgrounds: List<Bitmap>, private var currentZone: Int) : SensorEventListener {
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
        return internalCurrentStep.value - startingStep!!
    }

    fun goal(): Int {
        return adventures[currentZone].steps
    }

    fun currentBackground(): Bitmap {
        return backgrounds[adventures[currentZone].walkingBackgroundId]
    }

    fun currentBossBackground(): Bitmap {
        return backgrounds[adventures[currentZone].bossBackgroundId]
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            internalCurrentStep.value = event.values[0].toInt()
            if (startingStep == null) {
                startingStep =  - 1
            }
            if(internalCurrentStep.value - startingStep!! >= adventures[currentZone].steps && !internalZoneCompleted.value) {
                internalZoneCompleted.value = true
                service.notifyZoneCompletion(context)
            }
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
    }
}