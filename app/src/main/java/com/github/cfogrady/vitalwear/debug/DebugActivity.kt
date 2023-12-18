package com.github.cfogrady.vitalwear.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Button
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

    enum class DebugScreens {
        DEBUG_MENU,
        STEP_DEBUG_INFO,
        CHARACTER_DEBUG_INFO,
        HEART_RATE_DEBUG_INFO,
        VITALS_DEBUG_INFO,
    }

    @Composable
    fun Debug() {
        LaunchedEffect(key1 = true) {
            (application as VitalWearApp).exceptionService.logOutExceptions()
        }
        var debugMenuSelection by remember { mutableStateOf(DebugScreens.DEBUG_MENU) }
        val debugOptions = remember { DebugScreens.values().filter { it != DebugScreens.DEBUG_MENU } }
        when(debugMenuSelection) {
            DebugScreens.DEBUG_MENU -> {
                ScalingLazyColumn(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                    items(items = debugOptions) {
                        Button(onClick = { debugMenuSelection = it }) {
                            Text(text = it.name)
                        }
                    }
                }
            }
            DebugScreens.STEP_DEBUG_INFO -> DebugItems(loader = (application as VitalWearApp).stepService::debug) {
                debugMenuSelection = DebugScreens.DEBUG_MENU
            }
            DebugScreens.CHARACTER_DEBUG_INFO -> DebugItems(loader = (application as VitalWearApp).characterManager.getCurrentCharacter()::debug) {
                debugMenuSelection = DebugScreens.DEBUG_MENU
            }
            DebugScreens.HEART_RATE_DEBUG_INFO -> DebugItems(loader = (application as VitalWearApp).heartRateService::debug) {
                debugMenuSelection = DebugScreens.DEBUG_MENU
            }
            DebugScreens.VITALS_DEBUG_INFO -> DebugItems(loader = (application as VitalWearApp).vitalService::debug) {
                debugMenuSelection = DebugScreens.DEBUG_MENU
            }
        }
    }

    @Composable
    private fun DebugItems(loader: () -> List<Pair<String, String>>, onBack: () -> Unit) {
        var loaded by remember { mutableStateOf(false) }
        var debugItems by remember { mutableStateOf(ArrayList<Pair<String, String>>() as List<Pair<String, String>>) }
        if(!loaded) {
            Loading {
                debugItems = loader.invoke()
                loaded = true
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                Button(onClick = onBack) {
                    Text(text = "Back")
                }
                ScalingLazyColumn(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize(),) {
                    items(items = debugItems) {entry ->
                        Text(text = "${entry.first}: ${entry.second}")
                    }
                }
            }
        }
    }
}