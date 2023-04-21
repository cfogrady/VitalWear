package com.github.cfogrady.vitalwear.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.battle.BattleActivity
import com.github.cfogrady.vitalwear.character.activity.CharacterSelectActivity
import com.github.cfogrady.vitalwear.stats.StatsMenuActivity
import com.github.cfogrady.vitalwear.training.TrainingMenuActivity

class MainActivity : ComponentActivity() {
    companion object {
        val TAG = "MainActivity"
    }

    private lateinit var mainScreenComposable: MainScreenComposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainScreenComposable = (application as VitalWearApp).mainScreenComposable
        val activityLaunchers = buildActivityLaunchers()
//        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        setContent {
            mainScreenComposable.mainScreen(activityLaunchers)
        }
    }

    fun buildActivityLaunchers(): ActivityLaunchers {
        val trainingMenuIntent = Intent(applicationContext, TrainingMenuActivity::class.java)
        val statsMenuIntent = Intent(applicationContext, StatsMenuActivity::class.java)
        val characterSelectorIntent = Intent(applicationContext, CharacterSelectActivity::class.java)
        val characterSelector = {
            startActivity(characterSelectorIntent)
        }
        val battleIntent = Intent(applicationContext, BattleActivity::class.java)
        val battle = {
            startActivity(battleIntent)
        }
        return ActivityLaunchers({startActivity(statsMenuIntent)}, {startActivity(trainingMenuIntent)}, characterSelector, battle)
    }
}