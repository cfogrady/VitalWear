package com.github.cfogrady.vitalwear.character.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.CharacterPreview
import com.github.cfogrady.vitalwear.character.data.PreviewCharacterManager
import com.github.cfogrady.vitalwear.common.util.ActivityHelper
import com.github.cfogrady.vitalwear.settings.SettingsActivity
import kotlinx.coroutines.GlobalScope
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
            BuildScreen() {
                newCharacterLauncher.invoke {  }
            }
        }
    }

    @Composable
    fun BuildScreen(newCharacterLauncher: () -> Unit) {
        var loaded by remember { mutableStateOf(false) }
        var characters by remember { mutableStateOf(ArrayList<CharacterPreview>() as List<CharacterPreview>) }
        if(!loaded) {
            Loading() {
                /* TODO: Changes this to just be a database call.
                   Do card load into a cache for images on each individual item.
                   This should keep somebody with 100+ backups from 100+ card images from having
                   a ridiculously long load just to see the backups
                 */
                characters = previewCharacterManager.previewCharacters(applicationContext)
                loaded = true
            }
        } else {
            ScalingLazyColumn(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize(),) {
                item {
                    Button(onClick = {
                        newCharacterLauncher.invoke()
                    }) {
                        Text(text = "New...", modifier = Modifier.padding(10.dp))
                    }
                }
                items(items = characters) { character ->
                    Row(horizontalArrangement = Arrangement.SpaceAround, modifier= Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, RoundedCornerShape(10.dp))
                        .padding(5.dp)) {
                        Image(bitmap = character.idle.asImageBitmap(), contentDescription = "Character")
                        Button(onClick = {
                            GlobalScope.launch {
                                // TODO: Apply loading screen
                                characterManager.swapToCharacter(applicationContext, character)
                            }
                            finish()
                        }) {
                            Text(text = "Select", modifier = Modifier.padding(5.dp))
                        }
                        Button(onClick = {
                            characterManager.deleteCharacter(character)
                            finish()
                        }) {
                            Text(text = "Delete", modifier = Modifier.padding(5.dp))
                        }
                    }
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
}