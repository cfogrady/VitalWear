package com.github.cfogrady.vitalwear.communication

import com.github.cfogrady.vitalwear.VitalWearCompanion
import com.github.cfogrady.vitalwear.common.communication.ChannelTypes
import com.github.cfogrady.vitalwear.logs.LogService
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchCommunicationService : WearableListenerService() {
    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        when(channel.path) {
            ChannelTypes.LOGS_DATA -> {
                val logService = (application as VitalWearCompanion).logService
                logService.receiveFile(this, channel)
            }
            else -> {
                Timber.i("Unknown channel: ${channel.path}")
            }
        }
    }
}