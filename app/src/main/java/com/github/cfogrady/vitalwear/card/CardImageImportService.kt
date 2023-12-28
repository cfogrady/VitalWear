package com.github.cfogrady.vitalwear.card

import android.util.Log
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.common.communication.ChannelTypes
import com.google.android.gms.wearable.Channel
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset

class CardImageImportService  : WearableListenerService() {

    override fun onChannelOpened(p0: Channel) {
        super.onChannelOpened(p0)
        Log.i(TAG, "Old Channel opened")
    }

    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        Log.i(TAG, "Card channel opened 0")
        if (channel.path != ChannelTypes.CARD_DATA) {
            return
        }
        Log.i(TAG, "Card channel opened 1")
        val channelClient = Wearable.getChannelClient(this)
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            channelClient.getInputStream(channel).await().use {cardStream ->
                try {
                    Log.i(TAG, "Reading Card Name")
                    val cardName = getName(cardStream)
                    val uniqueSprites = cardStream.read() != 0
                    Log.i(TAG, "Importing card $cardName")
                    (application as VitalWearApp).cardLoader.importCardImage(applicationContext, cardName, cardStream, uniqueSprites)
                    (applicationContext as VitalWearApp).notificationChannelManager.sendGenericNotification(applicationContext, "$cardName Import Success", "$cardName imported successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Unable to load received card data", e)
                }
            }
            channelClient.close(channel)
        }
    }

    companion object {
        private const val TAG = "FileReceiverListenerService"
    }

    private fun getName(inputStream: InputStream): String {
        var lastReadByte: Int
        val nameBytes = ByteArrayOutputStream()
        do {
            lastReadByte = inputStream.read()
            if(lastReadByte != 0) {
                nameBytes.write(lastReadByte)
            }
        } while(lastReadByte != 0)
        return nameBytes.toString(Charset.defaultCharset().name())
    }
}