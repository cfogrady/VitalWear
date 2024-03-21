package com.github.cfogrady.vitalwear.settings

import android.content.SharedPreferences
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.RadioButton
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.background.BackgroundManager
import com.github.cfogrady.vitalwear.SaveService
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.background.BackgroundSelectionActivity
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.debug.LogSettings
import com.github.cfogrady.vitalwear.debug.TinyLogTree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class SettingsComposableFactory(private val backgroundManager: BackgroundManager, private val vitalBoxFactory: VitalBoxFactory, private val bitmapScaler: BitmapScaler, private val logSettings: LogSettings, private val saveService: SaveService) {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun SettingsMenu(activityLauncher: SettingsActivityLauncher) {
        val background by backgroundManager.selectedBackground.collectAsState()
        val menuPages = remember { buildSettingMenuPages() }
        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(bitmap = background!!, contentDescription = "Background", alignment = Alignment.BottomCenter)
            val pagerState = rememberPagerState(pageCount = {menuPages.size})
            VerticalPager(state = pagerState) {
                when(menuPages[it]) {
                    SettingsMenuOption.Background -> {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                activityLauncher.backgroundSelection.invoke{}
                            }, contentAlignment = Alignment.Center) {
                            Text(text = "BACKGROUND", fontWeight = FontWeight.Bold, fontSize = 2.1.em)
                        }
                    }
                    SettingsMenuOption.BattleBackground -> {
                        val battleBackgroundType by backgroundManager.battleBackgroundOption.collectAsState()
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "BATTLE", fontSize = 2.1.em, fontWeight = FontWeight.Bold)
                            Text(text = "BACKGROUND", fontSize = 2.1.em, fontWeight = FontWeight.Bold)
                            val radioScale = .5f
                            val fontSize = 1.7.em
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                                .padding(0.dp, 10.dp, 0.dp, 0.dp)
                                .clickable {
                                    backgroundManager.setBattleBackgroundPartner()
                                }) {
                                RadioButton(selected = battleBackgroundType == BackgroundManager.BattleBackgroundType.PartnerCard,
                                    modifier = Modifier.scale(radioScale))
                                Text(text = "Partner Card Battle Background", fontSize = fontSize)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                                .padding(0.dp, 5.dp, 0.dp, 0.dp)
                                .clickable {
                                    backgroundManager.setBattleBackgroundOpponent()
                                }) {
                                RadioButton(selected = battleBackgroundType == BackgroundManager.BattleBackgroundType.OpponentCard,
                                    modifier = Modifier.scale(radioScale))
                                Text(text = "Opponent Card Battle Background", fontSize = fontSize)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                                .padding(0.dp, 5.dp, 0.dp, 0.dp)
                                .clickable {
                                    activityLauncher.backgroundSelection{
                                        it.putExtra(BackgroundSelectionActivity.BACKGROUND_TYPE, BackgroundManager.BackgroundType.Battle.ordinal)
                                    }
                                }) {
                                RadioButton(selected = battleBackgroundType == BackgroundManager.BattleBackgroundType.Static,
                                    modifier = Modifier.scale(radioScale))
                                Text(text = "Static Battle Background", fontSize = fontSize)
                            }
                        }
                    }
                    SettingsMenuOption.Debug -> {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                activityLauncher.launchDebug.invoke()
                            }, contentAlignment = Alignment.Center) {
                            Text(text = "DEBUG", fontWeight = FontWeight.Bold, fontSize = 3.em)
                        }
                    }
                    SettingsMenuOption.ToggleLogging -> {
                        var loggingEnabled by remember { mutableStateOf(logSettings.loggingEnabled()) }
                        val text = if(loggingEnabled) "DISABLE\nLOGS" else "ENABLE\nLOGS"
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                loggingEnabled = logSettings.toggleLogging()
                            }, contentAlignment = Alignment.Center) {
                            Text(text = text, fontWeight = FontWeight.Bold, fontSize = 3.em, textAlign = TextAlign.Center)
                        }
                    }
                    SettingsMenuOption.Save -> {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                CoroutineScope(Dispatchers.IO).launch {
                                    saveService.save()
                                    withContext(Dispatchers.Main) {
                                        activityLauncher.toast.invoke("Save Completed")
                                    }
                                }
                            }, contentAlignment = Alignment.Center) {
                            Text(text = "SAVE", fontWeight = FontWeight.Bold, fontSize = 3.em)
                        }
                    }
                }
            }
        }
    }

    enum class SettingsMenuOption {
        Background,
        BattleBackground,
        Debug,
        ToggleLogging,
        Save
    }

    fun buildSettingMenuPages(): List<SettingsMenuOption> {
        return listOf(SettingsMenuOption.Background,
            SettingsMenuOption.BattleBackground,
            SettingsMenuOption.ToggleLogging,
            SettingsMenuOption.Debug,
            SettingsMenuOption.Save)
    }
}