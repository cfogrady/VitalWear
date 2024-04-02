package com.github.cfogrady.vitalwear.communication

import android.net.Uri
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.common.communication.ChannelTypes
import com.github.cfogrady.vitalwear.common.log.TinyLogTree
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class PhoneCommunicationService  : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        if(messageEvent.path==ChannelTypes.SEND_LOGS_REQUEST) {
            val mostRecentLog = TinyLogTree.getMostRecentLogFile(this)
            val channelClient = Wearable.getChannelClient(this)
            CoroutineScope(Dispatchers.IO).launch {
                val channel = channelClient.openChannel(messageEvent.sourceNodeId, ChannelTypes.LOGS_DATA).await()
                channelClient.sendFile(channel, Uri.fromFile(mostRecentLog)).apply {
                    addOnFailureListener {
                        Timber.e(it, "Failed to send log to phone")
                    }
                }
            }
        }
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
            ChannelTypes.FIRMWARE_DATA -> {
                CoroutineScope(Dispatchers.IO).launch {
                    (application as VitalWearApp).firmwareReceiver.importFirmwareFromChannel(applicationContext, channel)
                }
            }
            else -> {
                Timber.i("Unknown channel: ${channel.path}")
            }
        }
    }
}