package com.github.cfogrady.vitalwear.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.R
import com.github.cfogrady.vitalwear.character.PartnerScreen
import com.github.cfogrady.vitalwear.main.MenuOption.ADVENTURE
import com.github.cfogrady.vitalwear.main.MenuOption.BATTLE
import com.github.cfogrady.vitalwear.main.MenuOption.CHARACTER
import com.github.cfogrady.vitalwear.main.MenuOption.PARTNER
import com.github.cfogrady.vitalwear.main.MenuOption.SETTINGS
import com.github.cfogrady.vitalwear.main.MenuOption.SLEEP
import com.github.cfogrady.vitalwear.main.MenuOption.STATS
import com.github.cfogrady.vitalwear.main.MenuOption.TRAINING
import com.github.cfogrady.vitalwear.main.MenuOption.TRANSFER
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList

@Composable
fun DailyScreen(controller: MainScreenController) {
    val vitalBoxFactory = controller.vitalBoxFactory
    val bitmapScaler = controller.bitmapScaler
    val readyToTransform by controller.characterReadyToTransform.collectAsStateWithLifecycle()
    val sleeping by controller.characterSleepStatus.collectAsStateWithLifecycle()
    val phase by controller.characterPhase.collectAsStateWithLifecycle()
    val background by controller.background.collectAsStateWithLifecycle()
    val menuPages = remember(key1 = phase, key2 = sleeping) {
        buildMenuPages(phase, sleeping)
    }
    if (readyToTransform && !sleeping) {
        controller.launchTransformActivity()
    }


    vitalBoxFactory.VitalBox {
        bitmapScaler.ScaledBitmap(bitmap = background, contentDescription = "Background", alignment = Alignment.BottomCenter)
        val pagerState = rememberPagerState(pageCount = {
            menuPages.size
        })
        VerticalPager(state = pagerState) {page ->
            val menuItem = menuPages[page]
            when(menuItem) {
                PARTNER -> {
                    PartnerScreen(
                        controller = controller.partnerScreenController
                    )
                }
                STATS -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        bitmapScaler.ScaledBitmap(bitmap = controller.menuFirmwareSprites.statsIcon, contentDescription = "stats", modifier = Modifier.clickable {
                            controller.launchStatsMenuActivity()
                        })
                    }
                }
                CHARACTER -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        bitmapScaler.ScaledBitmap(bitmap = controller.menuFirmwareSprites.characterSelectorIcon, contentDescription = "Character", modifier = Modifier.clickable {
                            controller.launchCharacterSelectionActivity()
                        })
                    }
                }
                TRAINING -> {
                    vitalBoxFactory.VitalBox {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            bitmapScaler.ScaledBitmap(bitmap = controller.menuFirmwareSprites.trainingIcon, contentDescription = "Training", modifier = Modifier.clickable {
                                controller.launchTrainingMenuActivity()
                            })
                        }
                    }
                }
                ADVENTURE -> {
                    vitalBoxFactory.VitalBox {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            bitmapScaler.ScaledBitmap(bitmap = controller.menuFirmwareSprites.adventureIcon, contentDescription = "Adventure", modifier = Modifier.clickable {
                                controller.launchAdventureActivity()
                            })
                        }
                    }
                }
                TRANSFER -> {
                    vitalBoxFactory.VitalBox {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                controller.launchTransferActivity()
                            }, contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "TRANSFER",  fontWeight = FontWeight.Bold, fontSize = 3.em)
                            }
                        }
                    }
                }
                BATTLE -> {
                    vitalBoxFactory.VitalBox {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                controller.launchBattleActivity()
                            }, contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(painter = painterResource(id = R.drawable.fight_icon), contentDescription = "Battle")
                                Text(text = "BATTLE",  fontWeight = FontWeight.Bold, fontSize = 3.em)
                            }
                        }
                    }
                }
                SLEEP -> {
                    vitalBoxFactory.VitalBox {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            val sleepButton = if(sleeping) controller.menuFirmwareSprites.wakeIcon else controller.menuFirmwareSprites.sleepIcon
                            bitmapScaler.ScaledBitmap(bitmap = sleepButton, contentDescription = "Sleep", modifier = Modifier.clickable {
                                controller.toggleSleep()
//                                sleeping = !sleeping
//                                character.characterStats.sleeping = sleeping
//                                saveService.saveAsync()
                                CoroutineScope(Dispatchers.Main).launch {
                                    pagerState.scrollToPage(0)
                                }
                            })
                        }
                    }
                }
                SETTINGS -> {
                    vitalBoxFactory.VitalBox {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            bitmapScaler.ScaledBitmap(bitmap = controller.menuFirmwareSprites.settingsIcon, contentDescription = "Settings", modifier = Modifier.clickable {
                                controller.launchSettingsActivity()
                            })
                        }
                    }
                }
            }
        }
    }
}

fun buildMenuPages(phase: Int, sleeping: Boolean): ArrayList<MenuOption> {
    val menuPages = ArrayList<MenuOption>()
    menuPages.add(PARTNER)
    menuPages.add(STATS)
    menuPages.add(CHARACTER)
    if(!sleeping) {
        menuPages.add(TRAINING)
        if(phase > 1) {
            menuPages.add(ADVENTURE)
            menuPages.add(BATTLE)
        }
    }
    menuPages.add(TRANSFER)
    menuPages.add(SLEEP)
    menuPages.add(SETTINGS)
    return menuPages
}