package com.github.cfogrady.vitalwear.settings

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
        var allowedBattles by remember { mutableStateOf(settings.allowedBattles) }
        ScalingLazyColumn(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally){
            item {
                Row {
                    Text(text = "Background Training:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(5.dp))
                    Checkbox(checked = trainInBackground, onCheckedChange = {trainInBackground = it})
                }
            }
            item {
                var expanded by remember { mutableStateOf(false) }
                Text(text = allowedBattles.descr, modifier = Modifier.clickable { expanded = true })
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    CharacterSettingsEntity.AllowedBattles.values().forEachIndexed { index, allowedBattlesOption ->
                        DropdownMenuItem(text = {
                            Text(text = allowedBattlesOption.descr)
                        }, onClick = {
                            allowedBattles = allowedBattlesOption
                            expanded = false
                        })
                    }
                }
            }
            item {
                Button(onClick = {
                    settings.trainInBackground = trainInBackground
                    settings.allowedBattles = allowedBattles
                    finish()
                }) {
                    Text(text = "Continue", modifier = Modifier.padding(5.dp))
                }
            }
        }
    }
}