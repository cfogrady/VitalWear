package com.github.cfogrady.vitalwear.card.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.card.AppCardLoader
import com.github.cfogrady.vitalwear.common.communication.ChannelTypes
import com.github.cfogrady.vitalwear.common.composable.util.KeepScreenOn
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset

class LoadCardActivity : ComponentActivity() {

    companion object {
        const val TAG = "LoadCardActivity"
        const val LOADED_CARD_KEY = "Loaded Card"
    }

    enum class LoadCardState {
        WaitingForCard,
        Loading,
        Imported,
        Error,
    }

    lateinit var cardLoader : AppCardLoader
    lateinit var channelClient: ChannelClient
    var loadCardStateFlow = MutableStateFlow(LoadCardState.WaitingForCard)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        channelClient = Wearable.getChannelClient(this)
        cardLoader = (application as VitalWearApp).cardLoader
        setContent {
            BuildScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        channelClient.registerChannelCallback(channelHandler)
    }

    override fun onPause() {
        super.onPause()
        channelClient.unregisterChannelCallback(channelHandler)
    }

    private val channelHandler = object : ChannelClient.ChannelCallback() {
        override fun onChannelOpened(channel: ChannelClient.Channel) {
            super.onChannelOpened(channel)
            if(channel.path != ChannelTypes.CARD_DATA || loadCardStateFlow.value != LoadCardState.WaitingForCard) {
                return
            }
            receiveCard(channel)
        }
    }

    @Composable
    private fun BuildScreen() {
        KeepScreenOn()
        val loadCardState by loadCardStateFlow.collectAsState()
        when(loadCardState) {
            LoadCardState.WaitingForCard -> WaitingForCard()
            LoadCardState.Loading -> Loading(loadingText = "Loading Card") {}
            LoadCardState.Imported -> {
                LaunchedEffect(true) {
                    Handler(Looper.getMainLooper()!!).postDelayed({
                        val intent = Intent()
                        intent.putExtra(LOADED_CARD_KEY, true)
                        setResult(0, intent)
                        finish()
                    }, 1000)
                }
                Text(text = "Successfully Imported")
            }
            LoadCardState.Error -> {
                LaunchedEffect(true) {
                    Handler(Looper.getMainLooper()!!).postDelayed({
                        val intent = Intent()
                        intent.putExtra(LOADED_CARD_KEY, false)
                        setResult(0, intent)
                        finish()
                    }, 1000)
                }
                Text(text = "Error Importing Card")
            }
        }
    }

    @Preview
    @Composable
    private fun WaitingForCard() {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Import Card On Phone", textAlign = TextAlign.Center)
        }
    }

    private fun receiveCard(channel: ChannelClient.Channel) {
        loadCardStateFlow.value = LoadCardState.Loading
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            channelClient.getInputStream(channel).await().use {cardStream ->
                try {
                    Log.i(TAG, "Reading Card Name")
                    val cardName = getName(cardStream)
                    val test = getName(cardStream)
                    val uniqueSprites = cardStream.read() != 0
                    Log.i(TAG, "Importing card $cardName with $test tag")
                    cardLoader.importCard(applicationContext, cardName, cardStream, uniqueSprites)
                    // (applicationContext as VitalWearApp).notificationChannelManager.sendGenericNotification(applicationContext, "$cardName Import Success", "$cardName imported successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Unable to load received card data", e)
                    loadCardStateFlow.value = LoadCardState.Error
                }
            }
            channelClient.close(channel)
            loadCardStateFlow.value = LoadCardState.Imported
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