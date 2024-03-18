package com.github.cfogrady.vitalwear.card

import android.content.Context
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset

class CardReceiver(private val cardLoader: AppCardLoader) {

    private val _cardsImported = MutableStateFlow(0)
    val cardsImported: StateFlow<Int> = _cardsImported

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
                    _cardsImported.value++
                } catch (e: Exception) {
                    Timber.e("Unable to load received card data", e)
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