package com.github.cfogrady.vitalwear.stats

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.cfogrady.vitalwear.BackgroundManager
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StatsMenuActivity : ComponentActivity() {

    companion object {
        const val TAG = "StatsMenuActivity"
    }

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
        val partner = remember { characterManager.getLiveCharacter().value!! }
        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(
                bitmap = background,
                contentDescription = "Background",
                alignment = Alignment.BottomCenter
            )
            VerticalPager(pageCount = 1) { page ->
                when (page) {
                    0 -> {
                        NameLimitRank(partner)
                    }
                }
            }
        }
    }

    @Composable
    private fun NameLimitRank(partner: BEMCharacter) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            ScrollingName(name = partner.sprites[0])
        }
    }

    @Composable
    private fun ScrollingName(name: Bitmap) {
        val backgroundHeight = (application as VitalWearApp).backgroundHeight
        val scaledNameWidth = remember {bitmapScaler.scaledDimension(name.width)}
        val nameScroll = remember {Animatable(0f)}
        var running by remember { mutableStateOf(true)}
        LaunchedEffect(running) {
            nameScroll.animateTo(1f, tween(2000 * name.width / 80 , easing = LinearEasing))
        }
        // only do this when animation running state changes
        LaunchedEffect(key1 = nameScroll.isRunning) {
            //only do this if we aren't running and we aren't at the start state
            if(!nameScroll.isRunning && nameScroll.value > 0f) {
                running = false
                Handler(Looper.getMainLooper()!!).postDelayed({
                    GlobalScope.launch {
                        nameScroll.snapTo(0f)
                        running = true
                    }
                }, 500)
            }
        }
        val offset = backgroundHeight.div(2)
        val distance = backgroundHeight.div(2).plus(scaledNameWidth)
        Box(modifier = Modifier.horizontalScroll(ScrollState(0)), contentAlignment = Alignment.TopStart) {
            bitmapScaler.ScaledBitmap(bitmap = name, contentDescription = "Name", modifier = Modifier.offset(x = offset - distance.times(nameScroll.value)))
        }
    }
}