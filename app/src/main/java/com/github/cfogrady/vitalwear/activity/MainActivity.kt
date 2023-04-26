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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    companion object {
        val TAG = "MainActivity"
    }

    private lateinit var mainScreenComposable: MainScreenComposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainScreenComposable = (application as VitalWearApp).mainScreenComposable
        (application as VitalWearApp).stepService.listenDailySteps(this)
        val activityLaunchers = buildActivityLaunchers()
        setContent {
            mainScreenComposable.mainScreen(activityLaunchers)
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
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