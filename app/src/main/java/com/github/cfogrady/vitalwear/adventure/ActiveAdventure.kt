package com.github.cfogrady.vitalwear.adventure

import android.content.Context
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.collectAsState
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity
import com.github.cfogrady.vitalwear.util.BridgedSensorEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ActiveAdventure(private val context: Context, private val service: AdventureService, private val adventures: List<AdventureEntity>, private val backgrounds: List<Bitmap>, private var currentZone: Int, val partner: VBCharacter, val dailySteps: StateFlow<Int>) {
    companion object {
        const val TAG = "ActiveAdventure"
    }

    private var startingStep: Int = dailySteps.value
    private val internalZoneCompleted = MutableStateFlow(false)
    val zoneCompleted: StateFlow<Boolean> = internalZoneCompleted
    val job: Job

    init {
        Log.i(TAG, "Creating ActionAdventure")
        job = CoroutineScope(Dispatchers.Default).launch {
            dailySteps.collect{
                Log.i(TAG, "Step emitted: $it")
                checkSteps()
            }
        }
    }

    fun end() {
        job.cancel()
    }

    fun stepsTowardsGoal(): Int {
        Log.i(TAG, "Steps towards goal: ${dailySteps.value - startingStep}")
        return dailySteps.value - startingStep
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

    private fun checkSteps() {
        if(dailySteps.value - startingStep >= adventures[currentZone].steps && !internalZoneCompleted.value) {
            internalZoneCompleted.value = true
            service.notifyZoneCompletion(context)
        }
    }

    fun finishZone(moveToNext: Boolean) {
        val goalSteps = adventures[currentZone].steps
        startingStep = startingStep!! + goalSteps
        if(moveToNext) {
            currentZone = (currentZone + 1) % adventures.size
        }
        internalZoneCompleted.value = false
        Handler.createAsync(Looper.getMainLooper()).postDelayed(this::checkSteps, 500)
    }
}