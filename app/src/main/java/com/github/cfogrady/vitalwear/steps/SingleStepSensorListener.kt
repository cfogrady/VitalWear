package com.github.cfogrady.vitalwear.steps

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.github.cfogrady.vitalwear.util.SensorThreadHandler
import kotlinx.coroutines.CompletableDeferred

class SingleStepSensorListener(
    private val sensorManager: SensorManager,
    sensorThreadHandler: SensorThreadHandler,
    private val deferred: CompletableDeferred<Int>) : SensorEventListener {
    companion object {
        const val TAG = "SingleStepSensorListener"
    }

    init {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if(stepSensor == null) {
            deferred.completeExceptionally(java.lang.IllegalStateException("Step counter sensor doesn't exist on device"))
        } else {
            // We want data as fast a possible because this call is really asking what the current reading is.
            // We also want to run on the sensor thread because by default this runs on main and we want to block main for this in certain scenarios
            if(!sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST, sensorThreadHandler.handler)) {
                Log.e(TAG, "Failed to register sensor!")
                deferred.completeExceptionally(java.lang.IllegalStateException("Unable to register sensor"))
            } else {
                Log.i(TAG, "Registered single step sensor reading")
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event != null) {
            Log.i(TAG, "Received step count ${event.values[0].toInt()}. Unregistering from sensor.")
            sensorManager.unregisterListener(this)
            deferred.complete(event.values[0].toInt())
        } else {
            Log.e(TAG, "Received null event for sensor change")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(TAG, "Received accuracy change for step sensor")
    }
}