package com.github.cfogrady.vitalwear.firmware

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.VitalWearApp

class LoadFirmwareActivity  : ComponentActivity() {
    companion object {
        val TAG = "LoadFirmwareActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firmwareReceiver = (application as VitalWearApp).firmwareReceiver
        setContent {
            val firmwareUploads by firmwareReceiver.firmwareUpdates.collectAsState()
            LaunchedEffect(firmwareUploads) {
                if(firmwareUploads > 0) {
                    finish()
                }
            }
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Import Firmware From Phone To Continue", textAlign = TextAlign.Center)
            }
        }
    }
}