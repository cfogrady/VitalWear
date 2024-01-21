package com.github.cfogrady.vitalwear.character.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.*
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.CharacterPreview
import com.github.cfogrady.vitalwear.character.data.CharacterState
import com.github.cfogrady.vitalwear.character.data.PreviewCharacterManager
import com.github.cfogrady.vitalwear.common.util.ActivityHelper
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.github.cfogrady.vitalwear.settings.SettingsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val TAG = "CharacterSelectActivity"
const val LOADING_TEXT = "Loading..."

class CharacterSelectActivity : ComponentActivity() {

    lateinit var characterManager : CharacterManager
    lateinit var previewCharacterManager: PreviewCharacterManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityHelper = ActivityHelper(this)
        characterManager = (application as VitalWearApp).characterManager
        val firmware = (application as VitalWearApp).firmwareManager.getFirmware().value!!
        previewCharacterManager = (application as VitalWearApp).previewCharacterManager
        val newCharacterSettingsLauncher = activityHelper.getActivityLauncherWithResultHandling(SettingsActivity::class.java) {
            Log.i(TAG, "Finished from settings")
            finish()
        }
        val newCharacterLauncher = activityHelper.getActivityLauncherWithResultHandling(NewCharacterActivity::class.java) {result ->
            Log.i(TAG, "Finished from new character")
            if(newCharacterWasSelected(result)) {
                Log.i(TAG, "Received new character")
                newCharacterSettingsLauncher.invoke {  }
            }
        }
        setContent {
            BuildScreen(firmware) {
                newCharacterLauncher.invoke {  }
            }
        }
    }

    @Composable
    fun BuildScreen(firmware: Firmware, newCharacterLauncher: () -> Unit) {
        var loaded by remember { mutableStateOf(false) }
        var characters by remember { mutableStateOf(ArrayList<CharacterPreview>() as List<CharacterPreview>) }
        if(!loaded) {
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
        (application as VitalWearApp).vitalBoxFactory.VitalBox {
            val background = (application as VitalWearApp).backgroundManager.selectedBackground.value!!
            (application as VitalWearApp).bitmapScaler.ScaledBitmap(
                bitmap = background,
                contentDescription = "background"
            )
            VerticalPager(pageCount = options.size + 1) {
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
            Column(modifier = Modifier.fillMaxSize().clickable { showMenu = false }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceAround) {
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
                    .fillMaxWidth()
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
            Log.i(TAG, "result has no data?")
            return false
        }
        return result.data!!.getBooleanExtra(NEW_CHARACTER_SELECTED_FLAG, false)
    }

    private fun swapToCharacter(character: CharacterPreview) {
        CoroutineScope(Dispatchers.IO).launch {
            characterManager.swapToCharacter(applicationContext, character)
            finish()
        }
    }
}