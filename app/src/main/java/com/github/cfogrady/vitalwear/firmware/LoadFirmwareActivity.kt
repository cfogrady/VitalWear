package com.github.cfogrady.vitalwear.firmware

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.common.communication.ChannelTypes
import com.github.cfogrady.vitalwear.common.composable.util.KeepScreenOn
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class LoadFirmwareActivity  : ComponentActivity() {
    companion object {
        val TAG = "FileExploreActivity"
    }

    enum class LoadFirmwareState{
        WaitingForPhone,
        Loading,
    }

    lateinit var firmwareManager: FirmwareManager
    lateinit var channelClient: ChannelClient

    var loadFirmwareState = MutableStateFlow(LoadFirmwareState.WaitingForPhone)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firmwareManager = (application as VitalWearApp).firmwareManager
        channelClient = Wearable.getChannelClient(this)
        setContent {
            val state by loadFirmwareState.collectAsState()
            KeepScreenOn()
            when(state) {
                LoadFirmwareState.WaitingForPhone -> {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Import Firmware From Phone To Continue", textAlign = TextAlign.Center)
                    }
                }
                LoadFirmwareState.Loading -> {
                    KeepScreenOn()
                    Loading(loadingText = "Importing Firmware"){}
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        channelClient.registerChannelCallback(channelOpenHandler)
    }

    override fun onPause() {
        super.onPause()
        channelClient.unregisterChannelCallback(channelOpenHandler)
    }

    private val channelOpenHandler = object: ChannelClient.ChannelCallback() {
        override fun onChannelOpened(channel: ChannelClient.Channel) {
            super.onChannelOpened(channel)
            Log.i(TAG, "Channel opened!")
            if(channel.path != ChannelTypes.FIRMWARE_DATA) {
                return
            }
            lifecycleScope.launch(Dispatchers.IO) {
                loadFirmwareState.value = LoadFirmwareState.Loading
                Log.i(TAG, "Receiving firmware file")
                channelClient.receiveFile(channel, firmwareManager.firmwareUri(applicationContext), false)
            }
        }

        override fun onInputClosed(channel: ChannelClient.Channel, closeReason: Int, errorCode: Int) {
            super.onInputClosed(channel, closeReason, errorCode)
            Log.i(TAG, "Firmware file received")
            channelClient.close(channel)
            firmwareManager.loadFirmware(applicationContext).invokeOnCompletion {
                finish()
            }
        }
    }
}