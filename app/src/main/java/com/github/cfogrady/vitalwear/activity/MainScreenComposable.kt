package com.github.cfogrady.vitalwear.activity

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.github.cfogrady.vitalwear.adventure.AdventureScreenFactory
import com.github.cfogrady.vitalwear.background.BackgroundManager
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.training.BackgroundTrainingScreenFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.ArrayList

class MainScreenComposable(
    private val gameStateFlow: StateFlow<GameState>,
    private val characterManager: CharacterManager,
    private val saveService: SaveService,
    private val firmwareManager: FirmwareManager,
    private val backgroundManager: BackgroundManager,
    private val backgroundTrainingScreenFactory: BackgroundTrainingScreenFactory,
    private val imageScaler: ImageScaler,
    private val bitmapScaler: BitmapScaler,
    private val partnerScreenComposable: PartnerScreenComposable,
    private val vitalBoxFactory: VitalBoxFactory,
    private val adventureScreenFactory: AdventureScreenFactory,
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
    fun EverythingLoadedScreen(firmwareData: StateFlow<Firmware?>, activeCharacterData: StateFlow<VBCharacter?>, backgroundData: LiveData<Bitmap>, activityLaunchers: ActivityLaunchers) {
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
            BackgroundTraining(firmware = firmware!!, character = character!!, background = background!!, activityLaunchers = activityLaunchers)
        } else if (gameState == GameState.ADVENTURE) {
            adventureScreenFactory.AdventureScreen(activityLaunchers.context, activityLaunchers.adventureActivityLauncher, firmware!!, character!!)
        } else {
            DailyScreen(firmware!!, character = character!!, background!!, gameState, activityLaunchers)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun BackgroundTraining(firmware: Firmware, character: VBCharacter, background: Bitmap, activityLaunchers: ActivityLaunchers) {
        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(bitmap = background, contentDescription = "Background", alignment = Alignment.BottomCenter)
            val pagerState = rememberPagerState(pageCount = {
                2
            })
            VerticalPager(state = pagerState) { page ->
                when (page) {
                    0 -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            backgroundTrainingScreenFactory.BackgroundTraining(character, firmware)
                        }
                    }
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    activityLaunchers.stopBackgroundTrainingLauncher.invoke()
                                },
                            verticalArrangement = Arrangement.SpaceAround,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            bitmapScaler.ScaledBitmap(
                                bitmap = firmware.menuFirmwareSprites.stopText,
                                contentDescription = "stop")
                            bitmapScaler.ScaledBitmap(
                                bitmap = firmware.menuFirmwareSprites.stopIcon,
                                contentDescription = "training")
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun DailyScreen(firmware: Firmware, character: VBCharacter, background: Bitmap, gameState: GameState, activityLaunchers: ActivityLaunchers) {
        val readyToTransform by character.readyToTransform.collectAsState()
        if (readyToTransform != null) {
            activityLaunchers.transformLauncher.invoke()
        }
        val padding = imageScaler.getPadding()
        val menuPages = remember(key1 = character.speciesStats.phase, key2 = gameState) {
            buildMenuPages(character.speciesStats.phase, gameState)
        }

        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.ScaledBitmap(bitmap = background, contentDescription = "Background", alignment = Alignment.BottomCenter)
            val pagerState = rememberPagerState(pageCount = {
                menuPages.size
            })
            VerticalPager(state = pagerState) {page ->
                val menuItem = menuPages[page]
                when(menuItem) {
                    MenuOption.PARTNER -> {
                        partnerScreenComposable.PartnerScreen(
                            character = character,
                            firmware = firmware.characterFirmwareSprites,
                        )
                    }
                    MenuOption.STATS -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            bitmapScaler.ScaledBitmap(bitmap = firmware.menuFirmwareSprites.statsIcon, contentDescription = "stats", modifier = Modifier.clickable {
                                activityLaunchers.statsMenuLauncher.invoke()
                            })
                        }
                    }
                    MenuOption.CHARACTER -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            bitmapScaler.ScaledBitmap(bitmap = firmware.menuFirmwareSprites.characterSelectorIcon, contentDescription = "Character", modifier = Modifier.clickable {
                                activityLaunchers.characterSelectionLauncher.invoke()
                            })
                        }
                    }
                    MenuOption.TRAINING -> {
                        vitalBoxFactory.VitalBox {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                bitmapScaler.ScaledBitmap(bitmap = firmware.menuFirmwareSprites.trainingIcon, contentDescription = "Training", modifier = Modifier.clickable {
                                    activityLaunchers.trainingMenuLauncher.invoke()
                                })
                            }
                        }
                    }
                    MenuOption.ADVENTURE -> {
                        vitalBoxFactory.VitalBox {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                bitmapScaler.ScaledBitmap(bitmap = firmware.menuFirmwareSprites.adventureIcon, contentDescription = "Adventure", modifier = Modifier.clickable {
                                    activityLaunchers.adventureActivityLauncher.launchMenu.invoke()
                                })
                            }
                        }
                    }
                    MenuOption.BATTLE -> {
                        vitalBoxFactory.VitalBox {
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    activityLaunchers.battleLauncher.invoke()
                                }, contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(painter = painterResource(id = R.drawable.fight_icon), contentDescription = "Battle")
                                    Text(text = "BATTLE",  fontWeight = FontWeight.Bold, fontSize = 3.em)
                                }
                            }
                        }
                    }
                    MenuOption.SLEEP -> {
                        vitalBoxFactory.VitalBox {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                val sleepButton = if(gameState == GameState.SLEEPING) firmware.menuFirmwareSprites.wakeIcon else firmware.menuFirmwareSprites.sleepIcon
                                bitmapScaler.ScaledBitmap(bitmap = sleepButton, contentDescription = "Sleep", modifier = Modifier.clickable {
                                    activityLaunchers.sleepToggle.invoke()
                                    CoroutineScope(Dispatchers.Main).launch {
                                        pagerState.scrollToPage(0)
                                    }
                                })
                            }
                        }
                    }
                    MenuOption.SETTINGS -> {
                        vitalBoxFactory.VitalBox {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                bitmapScaler.ScaledBitmap(bitmap = firmware.menuFirmwareSprites.settingsIcon, contentDescription = "Settings", modifier = Modifier.clickable {
                                    activityLaunchers.settingsActivityLauncher.invoke()
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    enum class MenuOption {
        PARTNER,
        STATS,
        CHARACTER,
        TRAINING,
        ADVENTURE,
        BATTLE,
        SLEEP,
        SETTINGS,
    }

    fun buildMenuPages(phase: Int, gameState: GameState): ArrayList<MenuOption> {
        val menuPages = ArrayList<MenuOption>()
        menuPages.add(MenuOption.PARTNER)
        menuPages.add(MenuOption.STATS)
        menuPages.add(MenuOption.CHARACTER)
        if(gameState != GameState.SLEEPING) {
            menuPages.add(MenuOption.TRAINING)
            if(phase > 1) {
                menuPages.add(MenuOption.ADVENTURE)
                menuPages.add(MenuOption.BATTLE)
            }
        }
        menuPages.add(MenuOption.SLEEP)
        menuPages.add(MenuOption.SETTINGS)
        return menuPages
    }
}
