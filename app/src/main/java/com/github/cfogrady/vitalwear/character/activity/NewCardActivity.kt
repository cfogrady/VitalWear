package com.github.cfogrady.vitalwear.character.activity

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
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.data.CardLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


const val NEW_CHARACTER_SELECTED_FLAG = "newCharacterStarted"

class NewCardActivity : ComponentActivity() {
    val LOADING_TEXT = "Loading..."

    lateinit var cardLoader : CardLoader
    lateinit var characterManager : CharacterManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cardLoader = (application as VitalWearApp).cardLoader
        characterManager = (application as VitalWearApp).characterManager
        setContent {
            buildScreen()
        }
    }

    @Composable
    fun buildScreen() {
        var loaded by remember { mutableStateOf(false) }
        var files by remember { mutableStateOf(ArrayList<File>() as List<File>) }
        if(!loaded) {
            Text(text = LOADING_TEXT)
            LaunchedEffect(key1 = loaded) {
                files = loadFiles()
                loaded = true
            }
        } else {
            ScalingLazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = files) { file ->
                    Button(onClick = {
                        GlobalScope.launch {
                            withContext(Dispatchers.Default) {
                                characterManager.createNewCharacter(file)
                            }
                        }
                        val intent = Intent()
                        intent.putExtra(NEW_CHARACTER_SELECTED_FLAG, true)
                        setResult(0, intent)
                        finish()
                    }) {
                        Text(text = file.name, modifier = Modifier.padding(10.dp))
                    }
                }
            }
        }
    }

    fun loadFiles() : List<File> {
        return cardLoader.listLibrary(applicationContext)
    }
}