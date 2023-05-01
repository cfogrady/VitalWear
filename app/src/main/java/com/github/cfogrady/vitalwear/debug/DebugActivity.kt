package com.github.cfogrady.vitalwear.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp

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
        Loading {
            debugItems = getDebugItems()
            loaded = true
        }
        ScalingLazyColumn(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize(),) {
            items(items = debugItems) {entry ->
                Text(text = "${entry.first}:${entry.second}")
            }
        }
    }

    fun getDebugItems(): List<Pair<String, String>> {
        val list = ArrayList<Pair<String, String>>()
        list.addAll((application as VitalWearApp).stepService.debug())
        list.addAll((application as VitalWearApp).characterManager.getCurrentCharacter().debug())
        list.addAll()
        return list
    }
}