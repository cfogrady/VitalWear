package com.github.cfogrady.vitalwear.steps

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.github.cfogrady.vitalwear.util.SensorThreadHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal class ManyStepProcessingListener (
    override val dailyStepsAtStart: Int,
    private val stepProcessor: (Int) -> Int,
    private val sensorManager: SensorManager,
    sensorThreadHandler: SensorThreadHandler) : SensorEventListener, ManyStepListener {
    companion object {
        const val TAG = "ManyStepListener"
    }

    private val _dailyStepObserver = MutableSharedFlow<Int>()
    override val dailyStepObserver = _dailyStepObserver.asSharedFlow()

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
            GlobalScope.launch {
                _dailyStepObserver.emit(stepProcessor.invoke(stepCount))
            }
        } else {
            Log.w(TAG, "Received null event or null values")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Accuracy changed")
    }
}