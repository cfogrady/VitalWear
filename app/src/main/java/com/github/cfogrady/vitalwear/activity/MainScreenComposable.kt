package com.github.cfogrady.vitalwear.activity

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.cfogrady.vitalwear.BackgroundManager
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.data.Firmware
import com.github.cfogrady.vitalwear.data.FirmwareManager

class MainScreenComposable(val characterManager: CharacterManager, val firmwareManager: FirmwareManager, val backgroundManager: BackgroundManager, val imageScaler: ImageScaler) {
    companion object {
        val TAG = "MainScreenComposable"
    }

    val bitmapScaler = BitmapScaler(imageScaler)
    val partnerScreenComposable = PartnerScreenComposable(bitmapScaler)
    @Composable
    fun mainScreen(newCharacterLauncher: () -> Unit) {
        var loaded by remember { mutableStateOf(false) }
        var activeCharacter by remember { mutableStateOf(MutableLiveData<BEMCharacter>() as LiveData<BEMCharacter>) }
        var firmware by remember { mutableStateOf(MutableLiveData<Firmware>() as LiveData<Firmware>) }
        var background by remember { mutableStateOf(MutableLiveData<Bitmap>() as LiveData<Bitmap>) }
        //var firmware by
        if(!loaded) {
            Log.i(TAG, "Loading in mainScreen")
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
            Log.i(TAG, "Loading in everythingLoadedScreen. Firmware null: ${firmware == null}; character null: ${character == null}")
            Loading {}
        } else if(background == null) {
            Log.i(TAG, "Loading in everythingLoadedScreen background is null")
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
        val scale = imageScaler.getScaling()
        val padding = imageScaler.getPadding()
        Log.i(TAG, "Scale: $scale, Padding: $padding")
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            val backgroundHeight = imageScaler.scaledDpValueFromPixels(background.height)
            Image(bitmap = background.asImageBitmap(), contentDescription = "Background", modifier = Modifier.size(backgroundHeight))
            VerticalPager(pageCount = 2) {page ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    when(page) {
                        0 -> {
                            partnerScreenComposable.PartnerScreen(
                                character = character,
                                firmware = firmware,
                                backgroundHeight = backgroundHeight
                            )
                        }
                        1 -> {
                            Image(bitmap = character.sprites[character.activityIdx].asImageBitmap(), contentDescription = "Character", alignment = Alignment.Center, modifier = Modifier.scale(scale))
                        }
                    }
                }
            }
        }
    }
}
