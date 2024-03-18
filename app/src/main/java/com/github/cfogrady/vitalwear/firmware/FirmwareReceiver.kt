package com.github.cfogrady.vitalwear.firmware

import android.content.Context
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.ChannelClient.Channel
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class FirmwareReceiver(private val firmwareManager: FirmwareManager, private val notificationChannelManager: NotificationChannelManager) {
    private val _firmwareUpdates = MutableStateFlow(0)
    val firmwareUpdates: StateFlow<Int> = _firmwareUpdates

    fun importFirmwareFromChannel(context: Context, channel: Channel) {
        val channelClient = Wearable.getChannelClient(context)
        channelClient.registerChannelCallback(buildChannelCallbackHandler(context, channelClient))
        channelClient.receiveFile(channel, firmwareManager.firmwareUri(context), false)
    }

    fun buildChannelCallbackHandler(context: Context, channelClient: ChannelClient): ChannelClient.ChannelCallback {
        return object: ChannelClient.ChannelCallback() {
            override fun onInputClosed(channel: Channel, closeReason: Int, errorCode: Int) {
                super.onInputClosed(channel, closeReason, errorCode)
                channelClient.close(channel)
                channelClient.unregisterChannelCallback(this)
                firmwareManager.loadFirmware(context)
                Timber.i("Firmware fully received")
                _firmwareUpdates.value++
                notificationChannelManager.sendGenericNotification(context, "New Firmware Loaded", "")
            }
        }
    }
}