package com.github.cfogrady.vitalwear.settings

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.VitalWearApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    companion object {
        const val TAG = "SettingsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val characterManager = (application as VitalWearApp).characterManager
        val character = characterManager.getCurrentCharacter()!!
        val settings = character.settings
        Log.i(TAG, "Configure Settings")
        setContent {
            BuildScreen(settings = settings)
        }
    }

    override fun onPause() {
        super.onPause()
        CoroutineScope(Dispatchers.IO).launch {
            Log.i(TAG, "Update Settings")
            (application as VitalWearApp).characterManager.updateSettings()
        }
    }

    @Composable
    private fun BuildScreen(settings: CharacterSettingsEntity) {
        var trainInBackground by remember { mutableStateOf(settings.trainInBackground) }
        ScalingLazyColumn{
            item {
                Row {
                    Text(text = "Background Training:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(5.dp))
                    Checkbox(checked = trainInBackground, onCheckedChange = {trainInBackground = it})
                }
            }
            item {
                Button(onClick = {
                    settings.trainInBackground = trainInBackground
                    finish()
                }) {
                    Text(text = "Continue", modifier = Modifier.padding(5.dp))
                }
            }
        }
    }
}