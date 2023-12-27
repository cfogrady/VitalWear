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
import com.github.cfogrady.vitalwear.card.CardSpritesIO
import com.github.cfogrady.vitalwear.card.NewCardLoader
import com.github.cfogrady.vitalwear.card.activity.LoadCardActivity
import com.github.cfogrady.vitalwear.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.common.util.ActivityHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


const val NEW_CHARACTER_SELECTED_FLAG = "newCharacterStarted"

/**
 * NewCardActivity is used to start a new character.
 */
class NewCharacterActivity : ComponentActivity() {

    lateinit var cardLoader : NewCardLoader
    lateinit var characterManager : CharacterManager
    lateinit var cardSpritesIO: CardSpritesIO
    lateinit var cardMetaEntityDao: CardMetaEntityDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cardLoader = (application as VitalWearApp).newCardLoader
        characterManager = (application as VitalWearApp).characterManager
        cardSpritesIO = (application as VitalWearApp).cardSpriteIO
        cardMetaEntityDao = (application as VitalWearApp).cardMetaEntityDao
        val activityHelper = ActivityHelper(this)
        val importCardActivity = activityHelper.getActivityLauncherWithResultHandling(LoadCardActivity::class.java) {}
        setContent {
            buildScreen(importCardActivityLauncher = importCardActivity)
        }
    }

    @Composable
    fun buildScreen(importCardActivityLauncher: () -> Unit) {
        var loaded by remember { mutableStateOf(false) }
        var cards by remember { mutableStateOf(ArrayList<CardMetaEntity>() as List<CardMetaEntity>) }
        if(!loaded) {
            Text(text = LOADING_TEXT)
            LaunchedEffect(key1 = loaded) {
                cards = loadCards()
                loaded = true
            }
        } else {
            ScalingLazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Button(onClick = {
                        importCardActivityLauncher.invoke()
                    }) {
                        Text(text = "Import Card", modifier = Modifier.padding(10.dp))
                    }
                }
                items(items = cards) { card ->
                    Button(onClick = {
                        GlobalScope.launch {
                            withContext(Dispatchers.Default) {
                                characterManager.createNewCharacter(applicationContext, card)
                            }
                        }
                        val intent = Intent()
                        intent.putExtra(NEW_CHARACTER_SELECTED_FLAG, true)
                        setResult(0, intent)
                        finish()
                    }) {
                        Text(text = card.cardName, modifier = Modifier.padding(10.dp))
                    }
                }
            }
        }
    }

    private fun loadCards() : List<CardMetaEntity> {
        return cardMetaEntityDao.getAll()
    }
}