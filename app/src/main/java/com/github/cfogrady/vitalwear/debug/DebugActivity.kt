package com.github.cfogrady.vitalwear.debug

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import com.github.cfogrady.vitalwear.AppShutdownHandler
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.activity.MainActivity

class DebugActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Debug()
        }
    }

    @Composable
    fun Debug() {
        var loaded by remember { mutableStateOf(false) }
        var debugItems by remember { mutableStateOf(ArrayList<Pair<String, String>>() as List<Pair<String, String>>) }
        if(!loaded) {
            Loading {
                debugItems = getDebugItems()
                loaded = true
            }
        } else {
            ScalingLazyColumn(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize(),) {
                items(items = debugItems) {entry ->
                    Text(text = "${entry.first}: ${entry.second}")
                }
            }
        }
    }

    private fun getDebugItems(): List<Pair<String, String>> {
        val list = ArrayList<Pair<String, String>>()
        val gracefulShutdowns = (application as VitalWearApp).sharedPreferences.getInt(AppShutdownHandler.GRACEFUL_SHUTDOWNS_KEY, 0)
        list.add(Pair("Graceful shutdowns:", "$gracefulShutdowns"))
        list.addAll((application as VitalWearApp).stepService.debug())
        list.addAll((application as VitalWearApp).characterManager.getCurrentCharacter().debug())
        list.addAll((application as VitalWearApp).heartRateService.debug())
        return list
    }
}