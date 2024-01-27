package com.github.cfogrady.vitalwear.card

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset

class CardReceiver(val cardLoader: AppCardLoader) {

    companion object {
        const val TAG = "CardReceiver"
    }

    class ImportCardResult(val success: Boolean, val cardName: String?)

    suspend fun importCardFromChannel(context: Context, channel: ChannelClient.Channel): ImportCardResult {
        val channelClient = Wearable.getChannelClient(context)
        var cardName: String? = null
        var success = false
        withContext(Dispatchers.IO) {
            channelClient.getInputStream(channel).await().use {cardStream ->
                try {
                    cardName = getName(cardStream)
                    val uniqueSprites = cardStream.read() != 0
                    cardLoader.importCard(context, cardName!!, cardStream, uniqueSprites)
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "Unable to load received card data", e)
                }
            }
            channelClient.close(channel)
        }
        return ImportCardResult(success, cardName)
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