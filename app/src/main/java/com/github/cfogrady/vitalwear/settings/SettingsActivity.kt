package com.github.cfogrady.vitalwear.settings

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.FileProvider
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.background.BackgroundSelectionActivity
import com.github.cfogrady.vitalwear.common.util.ActivityHelper
import com.github.cfogrady.vitalwear.log.TinyLogTree

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
            // getSendLogEmailActivityLauncher(),
            { text ->
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
            },
            activityHelper.getActivityLauncherWithResultHandling(BackgroundSelectionActivity::class.java) {

            },
        )
    }

    // Don't use for now because gmail app on WearOS cannot compose email
    private fun getSendLogEmailActivityLauncher(): () -> Unit {
        val mostRecentLog = TinyLogTree.getMostRecentLogFile(this)
        return {
            if(mostRecentLog == null) {
                Toast.makeText(this, "No log files found. Enable logging", Toast.LENGTH_SHORT).show()
            } else {
                val mostRecentLogUri = FileProvider.getUriForFile(this, "$packageName.provider", mostRecentLog)
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                }
                emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("cfogrady@gmail.com"))
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "VitalWear Log")
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Please take a look at these logs to help diagnose the below issue:\n")
                emailIntent.putExtra(Intent.EXTRA_STREAM, mostRecentLogUri)
                emailIntent.clipData = ClipData.newRawUri("", mostRecentLogUri)
                emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(Intent.createChooser(emailIntent, "Select Email Client"))
            }
        }
    }
}