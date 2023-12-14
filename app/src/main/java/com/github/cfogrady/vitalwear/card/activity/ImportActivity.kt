package com.github.cfogrady.vitalwear.card.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.card.CardLoaderObserver
import com.github.cfogrady.vitalwear.card.NewCardLoader
import com.github.cfogrady.vitalwear.character.CharacterManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

class ImportActivity : ComponentActivity() {
    private val LOADING_TEXT = "Loading..."

    private lateinit var cardLoader : NewCardLoader
    lateinit var characterManager : CharacterManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cardLoader = (application as VitalWearApp).newCardLoader
        setContent {
            BuildScreen()
        }
    }

    @Composable
    private fun BuildScreen() {
        var loaded by remember { mutableStateOf(false) }
        var files by remember { mutableStateOf(ArrayList<File>() as List<File>) }
        lateinit var cardLoaderObserver: CardLoaderObserver
        lateinit var cardsBeingLoaded: Set<String>
        if(!loaded) {
            Text(text = LOADING_TEXT)
            LaunchedEffect(key1 = loaded) {
                loaded = true
                cardLoaderObserver = cardLoader.observerCardLoading {
                    files = loadFiles()
                }
                cardsBeingLoaded = cardLoaderObserver.cardsBeingLoaded
                files = loadFiles()
            }
        } else {
            DisposableEffect(true) {
                onDispose {
                    cardLoaderObserver.stopObserving()
                }
            }
            ScalingLazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = files) { file ->
                    val loading = cardsBeingLoaded.contains(file.name)
                    Button(enabled = !loading, onClick = {
                        GlobalScope.launch {
                            withContext(Dispatchers.IO) {
                                FileInputStream(file).use {inputStream ->
                                    cardLoader.importCardImage(super.getApplicationContext(), file.name, inputStream)
                                }
                                file.delete()
                            }
                        }
                        val intent = Intent()
                        setResult(0, intent)
                        finish()
                    }) {
                        Text(text = file.name, modifier = Modifier.padding(10.dp))
                        if(loading) {
                            Text(text = "Loading", modifier = Modifier.padding(10.dp))
                        }
                    }
                }
            }
        }
    }

    private fun loadFiles() : List<File> {
        return cardLoader.listImportDir(applicationContext)
    }
}