package com.github.cfogrady.vitalwear.activity

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.*
import com.github.cfogrady.vitalwear.R
import com.github.cfogrady.vitalwear.adventure.AdventureScreenFactory
import com.github.cfogrady.vitalwear.background.BackgroundManager
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.training.BackgroundTrainingScreenFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList

class MainScreenComposable(
    private val saveService: SaveService,
    private val firmwareManager: FirmwareManager,
    private val backgroundManager: BackgroundManager,
    private val backgroundTrainingScreenFactory: BackgroundTrainingScreenFactory,
    private val bitmapScaler: BitmapScaler,
    private val partnerScreenComposable: PartnerScreenComposable,
    private val vitalBoxFactory: VitalBoxFactory,
    private val adventureScreenFactory: AdventureScreenFactory,
) {
    companion object {
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun DailyScreen(firmware: Firmware, character: VBCharacter, background: Bitmap, activityLaunchers: ActivityLaunchers) {
        val readyToTransform by character.readyToTransform.collectAsState()
        var sleeping by remember { mutableStateOf(character.characterStats.sleeping) }
        val menuPages = remember(key1 = character.speciesStats.phase, key2 = sleeping) {
            buildMenuPages(character.speciesStats.phase, sleeping)
        }
        if (readyToTransform != null && !character.characterStats.sleeping) {
            activityLaunchers.transformLauncher.invoke()
        }


        vitalBoxFactory.VitalBox {
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
                    MenuOption.TRANSFER -> {
                        vitalBoxFactory.VitalBox {
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    activityLaunchers.transferLauncher.invoke()
                                }, contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "TRANSFER",  fontWeight = FontWeight.Bold, fontSize = 3.em)
                                }
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
                                val sleepButton = if(sleeping) firmware.menuFirmwareSprites.wakeIcon else firmware.menuFirmwareSprites.sleepIcon
                                bitmapScaler.ScaledBitmap(bitmap = sleepButton, contentDescription = "Sleep", modifier = Modifier.clickable {
                                    sleeping = !sleeping
                                    character.characterStats.sleeping = sleeping
                                    saveService.saveAsync()
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
        TRANSFER,
        BATTLE,
        SLEEP,
        SETTINGS,
    }

    private fun buildMenuPages(phase: Int, sleeping: Boolean): ArrayList<MenuOption> {
        val menuPages = ArrayList<MenuOption>()
        menuPages.add(MenuOption.PARTNER)
        menuPages.add(MenuOption.STATS)
        menuPages.add(MenuOption.CHARACTER)
        if(!sleeping) {
            menuPages.add(MenuOption.TRAINING)
            if(phase > 1) {
                menuPages.add(MenuOption.ADVENTURE)
                menuPages.add(MenuOption.BATTLE)
            }
        }
        menuPages.add(MenuOption.TRANSFER)
        menuPages.add(MenuOption.SLEEP)
        menuPages.add(MenuOption.SETTINGS)
        return menuPages
    }
}
