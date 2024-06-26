package com.github.cfogrady.vitalwear.character.activity

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.*
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.card.CardMeta
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.CharacterPreview
import com.github.cfogrady.vitalwear.character.data.CharacterState
import com.github.cfogrady.vitalwear.character.data.PreviewCharacterManager
import com.github.cfogrady.vitalwear.common.util.ActivityHelper
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.github.cfogrady.vitalwear.settings.CharacterSettings
import com.github.cfogrady.vitalwear.settings.CharacterSettingsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

const val LOADING_TEXT = "Loading..."

class CharacterSelectActivity : ComponentActivity() {

    lateinit var characterManager : CharacterManager
    lateinit var previewCharacterManager: PreviewCharacterManager

    class NewCharacter(val cardMeta: CardMeta, val slotId: Int)

    lateinit var selectedNewCharacter : NewCharacter

    var loadingNewCharacterFlow = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityHelper = ActivityHelper(this)
        characterManager = (application as VitalWearApp).characterManager
        val firmware = (application as VitalWearApp).firmwareManager.getFirmware().value!!
        previewCharacterManager = (application as VitalWearApp).previewCharacterManager
        val newCharacterSettingsLauncher = activityHelper.getActivityLauncherWithResultHandling(CharacterSettingsActivity::class.java) {
            loadingNewCharacterFlow.value = true
            val settings = it.data?.getParcelableExtra(CharacterSettingsActivity.CHARACTER_SETTINGS) ?: CharacterSettings.defaultSettings()
            CoroutineScope(Dispatchers.IO).launch {
                characterManager.createNewCharacter(applicationContext, selectedNewCharacter.cardMeta, selectedNewCharacter.slotId, settings)
                finish()
            }
        }
        val newCharacterLauncher = activityHelper.getActivityLauncherWithResultHandling(NewCharacterActivity::class.java) {result ->
            Timber.i("Finished from new character")
            if(newCharacterWasSelected(result)) {
                Timber.i("Received new character")
                loadingNewCharacterFlow.value = true
                val cardFromActivity = result.data?.getParcelableExtra<CardMeta>(NewCharacterActivity.CARD_SELECTED)!!
                val slotId = result.data?.getIntExtra(NewCharacterActivity.SLOT_SELECTED, 0)!!
                selectedNewCharacter = NewCharacter(cardFromActivity, slotId)
                newCharacterSettingsLauncher.invoke {
                    it.putExtra(CharacterSettingsActivity.CARD_TYPE, cardFromActivity.cardType)
                }
            }
        }
        setContent {
            BuildScreen(loadingNewCharacterFlow, firmware) {
                newCharacterLauncher.invoke {  }
            }
        }
    }

    @Composable
    fun BuildScreen(loadingNewCharacterState: StateFlow<Boolean>, firmware: Firmware, newCharacterLauncher: () -> Unit) {
        var loaded by remember { mutableStateOf(false) }
        var characters by remember { mutableStateOf(ArrayList<CharacterPreview>() as List<CharacterPreview>) }
        val loadingNewCharacter by loadingNewCharacterState.collectAsState()
        if(loadingNewCharacter) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Setting Up New Character")
            }
        } else if(!loaded) {
            Loading() {
                /* TODO: Changes this to just be a database call.
                   Do card load into a cache for images on each individual item.
                   This should keep somebody with 100+ backups from 100+ card images from having
                   a ridiculously long load just to see the backups
                 */
                characters = previewCharacterManager.previewCharacters(applicationContext).filterNot(CharacterPreview::isActive)
                loaded = true
            }
        } else {
            PagedCharacterSelectionMenu(characters = characters, firmware = firmware, newCharacterLauncher)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun PagedCharacterSelectionMenu(characters: List<CharacterPreview>, firmware: Firmware, newCharacterLauncher: () -> Unit) {
        val options = characters.toMutableStateList()
        val background by (application as VitalWearApp).backgroundManager.selectedBackground.collectAsState()
        (application as VitalWearApp).vitalBoxFactory.VitalBox {
            (application as VitalWearApp).bitmapScaler.ScaledBitmap(
                bitmap = background!!,
                contentDescription = "background"
            )
            val pagerState = rememberPagerState(pageCount = {options.size + 1})
            VerticalPager(state = pagerState) {
                if(it == 0) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = newCharacterLauncher)) {
                        Text(text = "NEW", fontSize = 6.em, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    val character = options[it - 1]
                    PreviewCharacter(support = firmware.characterFirmwareSprites.supportIcon, character = character, onSetSupport =  {
                        CoroutineScope(Dispatchers.IO).launch {
                            characterManager.setToSupport(character)
                            for( i in 0 until options.size) {
                                if(options[i].state == CharacterState.SUPPORT) {
                                    options[i] = CharacterPreview(options[i].cardName, options[i].slotId, options[i].characterId, CharacterState.STORED, options[i].idle)
                                }
                            }
                            options[it - 1] = CharacterPreview(character.cardName, character.slotId, character.characterId, CharacterState.SUPPORT, character.idle)
                        }
                    }, onDelete = {
                        options.removeAt(it-1)
                        characterManager.deleteCharacter(character)
                    })
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun PreviewCharacter(support: Bitmap, character: CharacterPreview, onSetSupport: ()->Unit, onDelete: ()->Unit) {
        var showMenu by remember { mutableStateOf(false) }
        if(showMenu) {
            Column(modifier = Modifier
                .fillMaxSize()
                .clickable { showMenu = false }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceAround) {
                Button(onClick = {
                    swapToCharacter(character)
                }) {
                    Text(text = "Select", Modifier.padding(10.dp))
                }
                Button(onClick = {
                    onSetSupport.invoke()
                    showMenu = false
                }) {
                    Text(text = "Support", Modifier.padding(10.dp))
                }
                Button(onClick = {
                    onDelete.invoke()
                    showMenu = false
                }) {
                    Text(text = "Delete", Modifier.padding(10.dp))
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(onLongClick = {
                        showMenu = true
                    }, onClick = {
                        swapToCharacter(character)
                    })) {
                (application as VitalWearApp).bitmapScaler.ScaledBitmap(
                    bitmap = character.idle,
                    contentDescription = "Character"
                )
                if(character.state == CharacterState.SUPPORT) {
                    (application as VitalWearApp).bitmapScaler.ScaledBitmap(
                        bitmap = support,
                        contentDescription = "Support"
                    )
                }
            }
        }
    }

    private fun newCharacterWasSelected(result: ActivityResult): Boolean {
        if(result.data == null) {
            Timber.i("result has no data?")
            return false
        }
        return result.data!!.getBooleanExtra(NewCharacterActivity.NEW_CHARACTER_SELECTED_FLAG, false)
    }

    private fun swapToCharacter(character: CharacterPreview) {
        CoroutineScope(Dispatchers.IO).launch {
            characterManager.swapToCharacter(applicationContext, character)
            finish()
        }
    }
}