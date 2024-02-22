package com.github.cfogrady.vitalwear.settings

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.common.util.ActivityHelper
import com.github.cfogrady.vitalwear.debug.DebugActivity

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsComposables = (application as VitalWearApp).settingsComposableFactory
        val settingsActivityLauncher = buildSettingsActivityLauncher()
        setContent {
            settingsComposables.SettingsMenu(activityLauncher = settingsActivityLauncher) {
                finish()
            }
        }
    }

    private fun buildSettingsActivityLauncher(): SettingsActivityLauncher {
        val activityHelper = ActivityHelper(this)
        return SettingsActivityLauncher (
            activityHelper.getActivityLauncherWithResultHandling(DebugActivity::class.java) {
            }
        ) { text ->
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }
    }
}