package com.github.cfogrady.vitalwear.stats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import com.github.cfogrady.vitalwear.BackgroundManager
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory

class StatsMenuActivity : ComponentActivity() {

    lateinit var characterManager: CharacterManager
    lateinit var backgroundManager: BackgroundManager
    lateinit var bitmapScaler: BitmapScaler
    lateinit var vitalBoxFactory: VitalBoxFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        characterManager = (application as VitalWearApp).characterManager
        backgroundManager = (application as VitalWearApp).backgroundManager
        bitmapScaler = (application as VitalWearApp).bitmapScaler
        vitalBoxFactory = (application as VitalWearApp).vitalBoxFactory
        setContent {
            statsMenu()
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun statsMenu() {
        val background = remember { backgroundManager.selectedBackground.value!! }
        val partner = remember { characterManager.getActiveCharacter() }
        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(
                bitmap = background,
                contentDescription = "Background",
                alignment = Alignment.BottomCenter
            )
            VerticalPager(pageCount = 1) { page ->
                when (page) {
                    0 -> {
                        
                    }
                }
            }
        }
    }
}