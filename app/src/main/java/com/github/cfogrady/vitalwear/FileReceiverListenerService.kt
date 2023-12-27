package com.github.cfogrady.vitalwear

import android.util.Log
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FileReceiverListenerService  : WearableListenerService() {

    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        if (channel != null) {
            var channelClient = Wearable.getChannelClient(this)
            GlobalScope.launch {
                channelClient.getInputStream(channel).await().use { cardStream ->
                    try {
                        (application as VitalWearApp).cardLoader.importCardImage(applicationContext, "cardName", cardStream)
                    } catch (e: Exception) {
                        Log.e(TAG, "Unable to load received card data", e)
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "FileReceiverListenerService"
    }
}