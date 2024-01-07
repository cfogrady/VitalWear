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
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.common.card.CardLoader
import com.github.cfogrady.vitalwear.card.activity.LoadCardActivity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.common.util.ActivityHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


const val NEW_CHARACTER_SELECTED_FLAG = "newCharacterStarted"

/**
 * NewCardActivity is used to start a new character.
 */
class NewCharacterActivity : ComponentActivity() {

    lateinit var characterManager : CharacterManager
    lateinit var cardSpritesIO: CardSpritesIO
    lateinit var cardMetaEntityDao: CardMetaEntityDao

    var newCardLoads = MutableStateFlow(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        characterManager = (application as VitalWearApp).characterManager
        cardSpritesIO = (application as VitalWearApp).cardSpriteIO
        cardMetaEntityDao = (application as VitalWearApp).cardMetaEntityDao
        val activityHelper = ActivityHelper(this)
        val importCardActivity = activityHelper.getActivityLauncherWithResultHandling(LoadCardActivity::class.java) {
            val cardAdded = it.data?.extras?.getBoolean(LoadCardActivity.LOADED_CARD_KEY, false)
            if(cardAdded != null && cardAdded) {
                newCardLoads.value++
            }
        }
        setContent {
            BuildScreen(importCardActivityLauncher = importCardActivity)
        }
    }

    @Composable
    fun BuildScreen(importCardActivityLauncher: ((Intent) -> Unit) -> Unit) {
        val cardLoads by newCardLoads.collectAsState()
        var loaded by remember { mutableStateOf(false) }
        var cards by remember { mutableStateOf(ArrayList<CardMetaEntity>() as List<CardMetaEntity>) }
        LaunchedEffect(cardLoads) {
            loaded = false
            withContext(Dispatchers.IO) {
                cards = loadCards()
                loaded = true
            }
        }
        if(!loaded) {
            Text(text = LOADING_TEXT)
        } else {
            ScalingLazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Button(onClick = {
                        importCardActivityLauncher.invoke {

                        }
                    }) {
                        Text(text = "Import Card", modifier = Modifier.padding(10.dp))
                    }
                }
                items(items = cards) { card ->
                    Button(onClick = {
                        CoroutineScope(Dispatchers.Default).launch {
                            characterManager.createNewCharacter(applicationContext, card)
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