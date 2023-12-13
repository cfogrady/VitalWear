package com.github.cfogrady.vitalwear.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.battle.BattleActivity
import com.github.cfogrady.vitalwear.character.activity.CharacterSelectActivity
import com.github.cfogrady.vitalwear.debug.DebugActivity
import com.github.cfogrady.vitalwear.firmware.LoadFirmwareActivity
import com.github.cfogrady.vitalwear.stats.StatsMenuActivity
import com.github.cfogrady.vitalwear.training.TrainingMenuActivity
import com.github.cfogrady.vitalwear.util.ActivityHelper
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

    override fun onStart() {
        super.onStart()
        val stepService = (application as VitalWearApp).stepService
        val characterManager = (application as VitalWearApp).characterManager
        if(characterManager.activeCharacterIsPresent()) {
            characterManager.getCurrentCharacter().characterStats.updateTimeStamps(LocalDateTime.now())
        }
    }

    override fun onStop() {
        super.onStop()
    }

    private fun buildActivityLaunchers(): ActivityLaunchers {
        val activityHelper = ActivityHelper(this)
        return ActivityLaunchers(
            activityHelper.getActivityLauncher(LoadFirmwareActivity::class.java),
            activityHelper.getActivityLauncher(StatsMenuActivity::class.java),
            activityHelper.getActivityLauncher(TrainingMenuActivity::class.java),
            activityHelper.getActivityLauncher(CharacterSelectActivity::class.java),
            activityHelper.getActivityLauncher(BattleActivity::class.java),
            activityHelper.getActivityLauncher(DebugActivity::class.java))
    }
}