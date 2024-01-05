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
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.*
import com.github.cfogrady.vitalwear.R
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import kotlinx.coroutines.flow.StateFlow

class MainScreenComposable(
    private val gameStateFlow: StateFlow<GameState>,
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
    fun MainScreen(activityLaunchers: ActivityLaunchers) {
        val characterManagerInitialized by characterManager.initialized.collectAsState()
        val firmwareState by firmwareManager.firmwareState.collectAsState()
        if(!characterManagerInitialized || firmwareState == FirmwareManager.FirmwareState.Loading) {
            Log.i(TAG, "Loading in mainScreen")
            Log.i(TAG, "Character Manager Initialized: $characterManagerInitialized")
            Log.i(TAG, "Firmware Manager Initialized: $firmwareState")
            Loading(loadingText = "Initializing") {}
        } else if(firmwareState == FirmwareManager.FirmwareState.Missing) {
            activityLaunchers.firmwareLoadingLauncher.invoke()
        } else {
            val activeCharacter = characterManager.getCharacterFlow()
            val firmware = firmwareManager.getFirmware()
            val background = backgroundManager.selectedBackground
            EverythingLoadedScreen(firmwareData = firmware, activeCharacterData = activeCharacter, background, activityLaunchers)
        }
    }

    @Composable
    fun EverythingLoadedScreen(firmwareData: StateFlow<Firmware?>, activeCharacterData: StateFlow<BEMCharacter?>, backgroundData: LiveData<Bitmap>, activityLaunchers: ActivityLaunchers) {
        val firmware by firmwareData.collectAsState()
        val character by activeCharacterData.collectAsState()
        val background by backgroundData.observeAsState()
        val gameState by gameStateFlow.collectAsState()
        if(background == null) {
            Log.i(TAG, "Loading in everythingLoadedScreen background is null")
            Loading {
                // TODO: Change to loadCurrent or similar
                backgroundManager.loadDefault()
            }
        } else if(character == null) {
            activityLaunchers.characterSelectionLauncher.invoke()
        } else if(gameState == GameState.TRAINING) {

        } else {
            DailyScreen(firmware!!, character = character!!, background!!, activityLaunchers)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun BackgroundTraining(firmware: Firmware, character: BEMCharacter, background: Bitmap, activityLaunchers: ActivityLaunchers) {
        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(bitmap = background, contentDescription = "Background", alignment = Alignment.BottomCenter)
            VerticalPager(pageCount = 2) { page ->
                when (page) {
                    0 -> {
                        partnerScreenComposable.PartnerScreen(
                            character = character,
                            firmware = firmware.characterFirmwareSprites,
                        )
                    }
                    1 -> {
                        Column(
                            modifier = Modifier.fillMaxSize().clickable {
                                activityLaunchers.trainingLauncher.invoke()
                            },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            bitmapScaler.ScaledBitmap(
                                bitmap = firmware.menuFirmwareSprites.trainingIcon,
                                contentDescription = "training")
                            bitmapScaler.ScaledBitmap(
                                bitmap = firmware.menuFirmwareSprites.stopIcon,
                                contentDescription = "stop")
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun DailyScreen(firmware: Firmware, character: BEMCharacter, background: Bitmap, activityLaunchers: ActivityLaunchers) {
        val readyToTransform by character.readyToTransform.collectAsState()
        if (readyToTransform.isPresent) {
            activityLaunchers.transformLauncher.invoke()
        }
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
                            firmware = firmware.characterFirmwareSprites,
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
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                                    saveService.saveAsync()
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
