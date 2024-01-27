package com.github.cfogrady.vitalwear.communication

import android.util.Log
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.common.communication.ChannelTypes
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset

class PhoneCommunicationService  : WearableListenerService() {
    companion object {
        private const val TAG = "PhoneCommunicationService"
    }

    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        when(channel.path) {
            ChannelTypes.CARD_DATA -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val result = (application as VitalWearApp).cardReceiver.importCardFromChannel(applicationContext, channel)
                    val notificationChannelManager = (application as VitalWearApp).notificationChannelManager
                    if(result.success) {
                        notificationChannelManager.sendGenericNotification(applicationContext, "${result.cardName} Import Successful", "")
                    } else {
                        val notificationCardName = result.cardName ?: "Card"
                        notificationChannelManager.sendGenericNotification(applicationContext, "$notificationCardName Import Failed", "")
                    }
                }
            }
            else -> {
                Log.i(TAG, "Unknown channel: ${channel.path}")
            }
        }
    }
}