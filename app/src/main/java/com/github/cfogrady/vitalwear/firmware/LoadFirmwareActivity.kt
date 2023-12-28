package com.github.cfogrady.vitalwear.firmware

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
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

    lateinit var firmwareManager: FirmwareManager
    lateinit var channelClient: ChannelClient

    var loading = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firmwareManager = (application as VitalWearApp).firmwareManager
        channelClient = Wearable.getChannelClient(this)
        setContent {
            KeepScreenOn()
            Loading(loadingText = "Import Firmware From Phone"){}
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
                Log.i(TAG, "Receiving firmware file")
                channelClient.receiveFile(channel, firmwareManager.firmwareUri(applicationContext), false)
            }
        }

        override fun onInputClosed(channel: ChannelClient.Channel, closeReason: Int, errorCode: Int) {
            super.onInputClosed(channel, closeReason, errorCode)
            Log.i(TAG, "Firmware file received")
            channelClient.close(channel)
            finish()
        }
    }
}