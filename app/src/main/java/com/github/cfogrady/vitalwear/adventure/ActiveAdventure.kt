package com.github.cfogrady.vitalwear.adventure

import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import com.github.cfogrady.vb.dim.sprite.SpriteData.Sprite
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity

class ActiveAdventure(private val adventureService: AdventureService, private val adventures: List<AdventureEntity>, private val backgrounds: List<Bitmap>, private var currentZone: Int) : SensorEventListener {
    companion object {
        const val TAG = "ActiveAdventure"
    }

    var startingStep: Int? = null
    var currentStep: Int = 0
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            currentStep = event.values[0].toInt()
            if (startingStep == null) {
                startingStep =  - 1
            }
            if (currentStep - startingStep!! >= adventures[currentZone].steps) {

            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(TAG, "step sensor accuracy changed to: $accuracy")
    }
}