package com.github.cfogrady.vitalwear.communication

import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.common.communication.ChannelTypes
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class PhoneCommunicationService  : WearableListenerService() {

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