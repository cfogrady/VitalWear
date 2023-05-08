package com.github.cfogrady.vitalwear.activity

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.*
import com.github.cfogrady.vitalwear.R
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.steps.SensorStepService
import java.time.LocalDate
import java.time.LocalDateTime

class MainScreenComposable(
    private val characterManager: CharacterManager,
    private val saveService: SaveService,
    private val firmwareManager: FirmwareManager,
    private val backgroundManager: BackgroundManager,
    private val imageScaler: ImageScaler,
    private val bitmapScaler: BitmapScaler,
    private val partnerScreenComposable: PartnerScreenComposable,
    private val vitalBoxFactory: VitalBoxFactory,
) {
    companion object {
        val TAG = "MainScreenComposable"
    }
    @Composable
    fun mainScreen(activityLaunchers: ActivityLaunchers) {
        val characterManagerInitialized by characterManager.initialized.observeAsState()
        if(!characterManagerInitialized!!) {
            Log.i(TAG, "Loading in mainScreen")
            Loading() {}
        } else {
            val activeCharacter = characterManager.getLiveCharacter()
            val firmware = firmwareManager.getFirmware()
            val background = backgroundManager.selectedBackground
            everythingLoadedScreen(firmwareData = firmware, activeCharacterData = activeCharacter, background, activityLaunchers)
        }
    }

    @Composable
    fun everythingLoadedScreen(firmwareData: LiveData<Firmware>, activeCharacterData: LiveData<BEMCharacter>, backgroundData: LiveData<Bitmap>, activityLaunchers: ActivityLaunchers) {
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
            activityLaunchers.characterSelectionLauncher.invoke()
        } else {
            dailyScreen(firmware!!, character = character!!, background!!, activityLaunchers)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun dailyScreen(firmware: Firmware, character: BEMCharacter, background: Bitmap, activityLaunchers: ActivityLaunchers) {
        val padding = imageScaler.getPadding()
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.ScaledBitmap(bitmap = background, contentDescription = "Background", alignment = Alignment.BottomCenter)
            VerticalPager(pageCount = 7) {page ->
                when(page) {
                    0 -> {
                        partnerScreenComposable.PartnerScreen(
                            character = character,
                            firmware = firmware.characterFirmwareSprites
                        )
                    }
                    1 -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            bitmapScaler.ScaledBitmap(bitmap = firmware.menuFirmwareSprites.statsIcon, contentDescription = "stats", modifier = Modifier.clickable {
                                activityLaunchers.statsMenuLauncher.invoke()
                            })
                        }
                    }
                    2 -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            bitmapScaler.ScaledBitmap(bitmap = firmware.menuFirmwareSprites.characterSelectorIcon, contentDescription = "Character", modifier = Modifier.clickable {
                                activityLaunchers.characterSelectionLauncher.invoke()
                            })
                        }
                    }
                    3 -> {
                        vitalBoxFactory.VitalBox {
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .clickable { activityLaunchers.battleLauncher.invoke() }, contentAlignment = Alignment.Center) {
                                bitmapScaler.ScaledBitmap(bitmap = firmware.menuFirmwareSprites.trainingIcon, contentDescription = "Training", modifier = Modifier.clickable {
                                    activityLaunchers.trainingMenuLauncher.invoke()
                                })
                            }
                        }
                    }
                    4 -> {
                        vitalBoxFactory.VitalBox {
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .clickable { activityLaunchers.battleLauncher.invoke() }, contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(painter = painterResource(id = R.drawable.fight_icon), contentDescription = "Battle")
                                    Text(text = "BATTLE",  fontWeight = FontWeight.Bold, fontSize = 3.em)
                                }
                            }
                        }
                    }
                    5 -> {
                        vitalBoxFactory.VitalBox {
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .clickable { activityLaunchers.debugActivityLauncher.invoke() }, contentAlignment = Alignment.Center) {
                                Text(text = "DEBUG",  fontWeight = FontWeight.Bold, fontSize = 3.em)
                            }
                        }
                    }
                    6 -> {
                        vitalBoxFactory.VitalBox {
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    saveService.saveBlocking()
                                }, contentAlignment = Alignment.Center) {
                                Text(text = "SAVE",  fontWeight = FontWeight.Bold, fontSize = 3.em)
                            }
                        }
                    }
                }
            }
        }
    }
}
