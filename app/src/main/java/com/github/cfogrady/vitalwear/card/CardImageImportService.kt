package com.github.cfogrady.vitalwear.card

import android.util.Log
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.common.communication.ChannelTypes
import com.google.android.gms.common.util.IOUtils
import com.google.android.gms.wearable.Channel
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset

class CardImageImportService  : WearableListenerService() {
    companion object {
        private const val TAG = "FileReceiverListenerService"
    }
    override fun onCreate() {
        Log.i(TAG, "Create CardImportService")
        super.onCreate()
    }

    fun onMessageChannel(channel: ChannelClient.Channel) {
        val channelClient = Wearable.getChannelClient(this)
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            channelClient.getInputStream(channel).await().use {
                val data = it.readBytes()
                val message = String(data, Charset.defaultCharset())
                Log.i(TAG, "Received message: $message")
            }
            channelClient.close(channel)
        }
    }

    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        if (channel.path == ChannelTypes.CARD_DATA) {
            onCardChannel(channel)
        } else if(channel.path == ChannelTypes.TEST_MESSAGE) {
            onMessageChannel(channel)
        }
        Log.i(TAG, "Unknown channel: ${channel.path}")
    }

    fun onCardChannel(channel: ChannelClient.Channel) {
        Log.i(TAG, "Card channel opened")
        val channelClient = Wearable.getChannelClient(this)
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            channelClient.getInputStream(channel).await().use {cardStream ->
                try {
                    Log.i(TAG, "Reading Card Name")
                    val cardName = getName(cardStream)
                    val uniqueSprites = cardStream.read() != 0
                    Log.i(TAG, "Importing card $cardName")
                    (application as VitalWearApp).cardLoader.importCard(applicationContext, cardName, cardStream, uniqueSprites)
                    (applicationContext as VitalWearApp).notificationChannelManager.sendGenericNotification(applicationContext, "$cardName Import Success", "$cardName imported successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Unable to load received card data", e)
                }
            }
            channelClient.close(channel)
        }
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