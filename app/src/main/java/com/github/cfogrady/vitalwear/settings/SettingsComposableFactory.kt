package com.github.cfogrady.vitalwear.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.background.BackgroundManager
import com.github.cfogrady.vitalwear.SaveService
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsComposableFactory(private val backgroundManager: BackgroundManager, private val vitalBoxFactory: VitalBoxFactory, private val bitmapScaler: BitmapScaler, private val saveService: SaveService) {

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
                                activityLauncher.backgroundSelection.invoke()
                            }, contentAlignment = Alignment.Center) {
                            Text(text = "BACKGROUND", fontWeight = FontWeight.Bold, fontSize = 2.5.em)
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
        Debug,
        Save
    }

    fun buildSettingMenuPages(): List<SettingsMenuOption> {
        return listOf(SettingsMenuOption.Background,
            SettingsMenuOption.Debug,
            SettingsMenuOption.Save)
    }
}