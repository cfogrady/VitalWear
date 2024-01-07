package com.github.cfogrady.vitalwear.training

import android.hardware.SensorEventListener
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalTime

abstract class TrainingProgressTracker : SensorEventListener {

    abstract val trainingType: TrainingType

    private var greats = 0
    private var goods = 0
    private var fails = 0

    private var debugReps = mutableListOf<Pair<String, String>>()
    abstract fun progressFlow(): StateFlow<Float>

    abstract fun unregister()

    abstract fun getPoints(): Int

    protected abstract fun reset()

    // TODO: Someday this should be synced so we don't take any sensor readings in the middle of this
    fun finishRep() {
        val points = getPoints()
        reset()
        if(points == 4) {
            greats++
        } else if(points > 0) {
            goods++
        } else {
            fails++
        }
        debugReps.add(Pair("Rep End At: ${LocalTime.now()}", "$points"))
    }

    fun results(): BackgroundTrainingResults {
        return BackgroundTrainingResults(greats, goods, fails, trainingType, debugReps)
    }
}