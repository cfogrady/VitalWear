package com.github.cfogrady.vitalwear.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.background.BackgroundSelectionActivity
import com.github.cfogrady.vitalwear.common.util.ActivityHelper
import com.github.cfogrady.vitalwear.debug.DebugActivity
import com.github.cfogrady.vitalwear.debug.TinyLogTree

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsComposables = (application as VitalWearApp).settingsComposableFactory
        val settingsActivityLauncher = buildSettingsActivityLauncher()
        setContent {
            settingsComposables.SettingsMenu(activityLauncher = settingsActivityLauncher)
        }
    }

    private fun buildSettingsActivityLauncher(): SettingsActivityLauncher {
        val activityHelper = ActivityHelper(this)
        return SettingsActivityLauncher (
            activityHelper.getActivityLauncher(DebugActivity::class.java),
            getSendLogEmailActivityLauncher(),
            { text ->
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
            },
            activityHelper.getActivityLauncherWithResultHandling(BackgroundSelectionActivity::class.java) {

            },
        )
    }

    private fun getSendLogEmailActivityLauncher(): () -> Unit {
        val mostRecentLog = TinyLogTree.getMostRecentLogFile(this)
        return {
            if(mostRecentLog == null) {
                Toast.makeText(this, "No log files found. Enable logging", Toast.LENGTH_SHORT).show()
            } else {
                val emailIntent = Intent(Intent.ACTION_SEND)
                emailIntent.setType("text/plain")
                emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("cfogrady@gmail.com"))
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "VitalWear Log")
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Please take a look at these logs to help diagnose the below issue:\n")
                emailIntent.putExtra(Intent.EXTRA_STREAM, mostRecentLog.toURI())
                startActivity(Intent.createChooser(emailIntent, "Pick and email provider"))
            }
        }
    }
}