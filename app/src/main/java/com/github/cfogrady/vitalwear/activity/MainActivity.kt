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
import com.github.cfogrady.vitalwear.stats.StatsMenuActivity
import com.github.cfogrady.vitalwear.steps.SensorStepService
import com.github.cfogrady.vitalwear.steps.StepListener
import com.github.cfogrady.vitalwear.training.TrainingMenuActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

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
        GlobalScope.launch {
            val stepService = (application as VitalWearApp).stepService
            stepListener = stepService.listenDailySteps()
            val sharedPreferences = (application as VitalWearApp).sharedPreferences
            val dailyStepsBeforeShutdown = sharedPreferences.getInt(SensorStepService.DAILY_STEPS_KEY, 0)
            val timeSinceEpoch = sharedPreferences.getLong(SensorStepService.DAY_OF_LAST_READ_KEY, 0)
            val dateFromSave = LocalDate.ofEpochDay(timeSinceEpoch)
            Log.i(TAG, "Saved dailySteps: $dailyStepsBeforeShutdown from date: $dateFromSave")
            val lastMightnight = stepService.getLastMignight()
            Log.i(TAG, "Last mightnight: $lastMightnight")
            val gracefulShutdowns = sharedPreferences.getInt(AppShutdownHandler.GRACEFUL_SHUTDOWNS_KEY, 0)
            Log.i(TAG, "Graceful shutdowns: $gracefulShutdowns")
        }
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
        return ActivityLaunchers({startActivity(statsMenuIntent)}, {startActivity(trainingMenuIntent)}, characterSelector, battle)
    }
}