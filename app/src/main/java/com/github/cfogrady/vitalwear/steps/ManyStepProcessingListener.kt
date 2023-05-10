package com.github.cfogrady.vitalwear.steps

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.github.cfogrady.vitalwear.util.SensorThreadHandler
import kotlinx.coroutines.flow.*

internal class ManyStepProcessingListener (
    dailyStepsAtStart: Int,
    private val stepProcessor: (Int) -> Int,
    private val sensorManager: SensorManager,
    sensorThreadHandler: SensorThreadHandler) : SensorEventListener, ManyStepListener {
    companion object {
        const val TAG = "ManyStepListener"
    }

    private val _dailyStepObserver = MutableStateFlow(dailyStepsAtStart)
    override val dailyStepObserver = _dailyStepObserver.asStateFlow()

    init {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if(stepSensor == null) {
            Log.e(TAG, "Step counter sensor doesn't exist on device")
        } else {
            if(!sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI, sensorThreadHandler.handler)) {
                Log.e(SingleStepSensorListener.TAG, "Failed to register sensor!")
            } else {
                Log.i(SingleStepSensorListener.TAG, "Registered many step sensor reading")
            }
        }
    }

    override fun unregister() {
        sensorManager.unregisterListener(this)
        Log.i(TAG, "Unregistered ManyStepListener")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.values != null) {
            val stepCount = event.values[event.values.size-1].toInt()
            val dailySteps = stepProcessor.invoke(stepCount)
            if(!_dailyStepObserver.tryEmit(dailySteps)) {
                Log.e(TAG, "Failed to emit daily steps")
            }
        } else {
            Log.w(TAG, "Received null event or null values")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Accuracy changed")
    }
}