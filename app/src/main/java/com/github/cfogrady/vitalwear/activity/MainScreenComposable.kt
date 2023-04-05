package com.github.cfogrady.vitalwear.activity

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.cfogrady.vitalwear.BackgroundManager
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.data.Firmware
import com.github.cfogrady.vitalwear.data.FirmwareManager

class MainScreenComposable(val characterManager: CharacterManager, val firmwareManager: FirmwareManager, val backgroundManager: BackgroundManager) {
    @Composable
    fun mainScreen(newCharacterLauncher: () -> Unit) {
        var loaded by remember { mutableStateOf(false) }
        var activeCharacter by remember { mutableStateOf(MutableLiveData<BEMCharacter>() as LiveData<BEMCharacter>) }
        var firmware by remember { mutableStateOf(MutableLiveData<Firmware>() as LiveData<Firmware>) }
        var background by remember { mutableStateOf(MutableLiveData<Bitmap>() as LiveData<Bitmap>) }
        //var firmware by
        if(!loaded) {
            Loading() {
                activeCharacter = characterManager.getActiveCharacter()
                firmware = firmwareManager.getFirmware()
                background = backgroundManager.selectedBackground
                loaded = true
            }
        }
        everythingLoadedScreen(firmwareData = firmware, activeCharacterData = activeCharacter, background, newCharacterLauncher)
    }

    @Composable
    fun everythingLoadedScreen(firmwareData: LiveData<Firmware>, activeCharacterData: LiveData<BEMCharacter>, backgroundData: LiveData<Bitmap>, newCharacterLauncher: () -> Unit) {
        val firmware by firmwareData.observeAsState()
        val character by activeCharacterData.observeAsState()
        val background by backgroundData.observeAsState()
        if(firmware == null || character == null) {
            Loading {}
        } else if(background == null) {
            Loading {
                // TODO: Change to loadCurrent or similar
                backgroundManager.loadDefault()
            }
        } else if(character!!.characterStats.id == BEMCharacter.DEFAULT_CHARACTER.characterStats.id) {
            newCharacterLauncher.invoke()
        } else {
            dailyScreen(firmware!!, character = character!!, background!!)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun dailyScreen(firmware: Firmware, character: BEMCharacter, background: Bitmap) {
        Box() {
            Image(bitmap = background.asImageBitmap(), contentDescription = "Background", alignment = Alignment.BottomCenter)
            VerticalPager(pageCount = 2) {page ->
                when(page) {
                    0 -> {
                        Image(bitmap = character.sprites[character.activityIdx].asImageBitmap(), contentDescription = "Character", alignment = Alignment.BottomCenter)
                    }
                    1 -> {
                        Image(bitmap = character.sprites[character.activityIdx].asImageBitmap(), contentDescription = "Character", alignment = Alignment.Center)
                    }
                }
            }
        }
    }
}
