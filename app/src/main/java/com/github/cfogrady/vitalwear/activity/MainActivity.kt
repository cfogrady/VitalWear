package com.github.cfogrady.vitalwear.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.cfogrady.vitalwear.AppShutdownHandler
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.battle.BattleActivity
import com.github.cfogrady.vitalwear.character.activity.CharacterSelectActivity
import com.github.cfogrady.vitalwear.debug.DebugActivity
import com.github.cfogrady.vitalwear.stats.StatsMenuActivity
import com.github.cfogrady.vitalwear.steps.SensorStepService
import com.github.cfogrady.vitalwear.steps.StepListener
import com.github.cfogrady.vitalwear.training.TrainingMenuActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    companion object {
        val TAG = "MainActivity"
    }

    private lateinit var mainScreenComposable: MainScreenComposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainScreenComposable = (application as VitalWearApp).mainScreenComposable
        val activityLaunchers = buildActivityLaunchers()
        setContent {
            mainScreenComposable.mainScreen(activityLaunchers)
        }
    }

    lateinit var stepListener: StepListener

    override fun onStart() {
        super.onStart()
        val stepService = (application as VitalWearApp).stepService
        val characterManager = (application as VitalWearApp).characterManager
        if(characterManager.activeCharacterIsPresent()) {
            characterManager.getCurrentCharacter().characterStats.updateTimeStamps(LocalDateTime.now())
        }
        stepListener = stepService.listenDailySteps()
    }

    override fun onStop() {
        super.onStop()
        stepListener.unregsiter()
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
        val debugActivityLauncher = {
            startActivity(Intent(applicationContext, DebugActivity::class.java))
        }
        return ActivityLaunchers({startActivity(statsMenuIntent)}, {startActivity(trainingMenuIntent)}, characterSelector, battle, debugActivityLauncher)
    }
}