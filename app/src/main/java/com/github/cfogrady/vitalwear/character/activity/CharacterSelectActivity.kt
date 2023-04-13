package com.github.cfogrady.vitalwear.character.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import java.io.File

const val TAG = "CharacterSelectActivity"
const val LOADING_TEXT = "Loading..."

class CharacterSelectActivity : ComponentActivity() {

    lateinit var characterManager : CharacterManager
    lateinit var previewCharacterManager: PreviewCharacterManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        characterManager = (application as VitalWearApp).characterManager
        previewCharacterManager = (application as VitalWearApp).previewCharacterManager
        val intent = Intent(applicationContext, NewCardActivity::class.java)
        val contract = ActivityResultContracts.StartActivityForResult()
        contract.createIntent(applicationContext, intent)
        val newCharacterLauncher = registerForActivityResult(contract) {result ->
            if(newCharacterWasSelected(result)) {
                finish()
            }
        }
        setContent {
            buildScreen() {
                newCharacterLauncher.launch(intent)
            }
        }
    }

    @Composable
    fun buildScreen(newCharacterLauncher: () -> Unit) {
        var loaded by remember { mutableStateOf(false) }
        var characters by remember { mutableStateOf(ArrayList<File>() as List<CharacterPreview>) }
        if(!loaded) {
            Loading() {
                /* TODO: Changes this to just be a database call.
                   Do card load into a cache for images on each individual item.
                   This should keep somebody with 100+ backups from 100+ card images from having
                   a ridiculously long load just to see the backups
                 */
                characters = previewCharacterManager.previewCharacters()
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
                    Row(horizontalArrangement = Arrangement.SpaceAround, modifier=Modifier.fillMaxWidth().background(Color.LightGray, RoundedCornerShape(10.dp)).padding(5.dp)) {
                        Image(bitmap = character.idle.asImageBitmap(), contentDescription = "Character")
                        Button(onClick = {
                            characterManager.swapToCharacter(character)
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
        Log.i(TAG, "data " + result.data!!.getBooleanExtra(NEW_CHARACTER_SELECTED_FLAG, false))
        return result.data!!.getBooleanExtra(NEW_CHARACTER_SELECTED_FLAG, false)
    }
}