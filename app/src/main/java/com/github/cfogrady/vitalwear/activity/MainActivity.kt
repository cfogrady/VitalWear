package com.github.cfogrady.vitalwear.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.adventure.AdventureActivityLauncher
import com.github.cfogrady.vitalwear.battle.BattleActivity
import com.github.cfogrady.vitalwear.character.activity.CharacterSelectActivity
import com.github.cfogrady.vitalwear.character.transformation.TransformationActivity
import com.github.cfogrady.vitalwear.firmware.LoadFirmwareActivity
import com.github.cfogrady.vitalwear.stats.StatsMenuActivity
import com.github.cfogrady.vitalwear.training.TrainingMenuActivity
import com.github.cfogrady.vitalwear.common.util.ActivityHelper
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.settings.SettingsActivity
import com.github.cfogrady.vitalwear.training.StopBackgroundTrainingActivity
import com.github.cfogrady.vitalwear.transfer.TransferActivity
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
            InitialScreen(InitialScreenController.buildInitialScreenController(application as VitalWearApp, activityLaunchers, this.lifecycleScope))
        }
    }

    override fun onStart() {
        super.onStart()
        val stepService = (application as VitalWearApp).stepService
        val characterManager = (application as VitalWearApp).characterManager
        characterManager.getCurrentCharacter()?.characterStats?.updateTimeStamps(LocalDateTime.now())
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
            activityHelper.getActivityLauncher(TransferActivity::class.java),
            activityHelper.getActivityLauncher(TransformationActivity::class.java),
            activityHelper.getActivityLauncher(SettingsActivity::class.java),
            activityHelper.getActivityLauncher(StopBackgroundTrainingActivity::class.java),
            {text -> Toast.makeText(this, text, Toast.LENGTH_SHORT).show() },
            AdventureActivityLauncher.buildFromContextAndActivityHelper(application, activityHelper),
            this,
        )
    }
}