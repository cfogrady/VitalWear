package com.github.cfogrady.vitalwear.activity

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.activity.CharacterSelectActivity
import com.github.cfogrady.vitalwear.data.FirmwareManager


class MainActivity : ComponentActivity() {
    companion object {
        val TAG = "MainActivity"
    }

    private lateinit var mainScreenComposable: MainScreenComposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainScreenComposable = (application as VitalWearApp).mainScreenComposable
        val intent = Intent(applicationContext, CharacterSelectActivity::class.java)
        val characterSelector = {
            startActivity(intent)
        }
//        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        setContent {
            mainScreenComposable.mainScreen(characterSelector)
        }
    }
}