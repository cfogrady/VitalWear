package com.github.cfogrady.vitalwear.battle.composable

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.cfogrady.vitalwear.activity.ImageScaler
import com.github.cfogrady.vitalwear.battle.BattleCharacter
import com.github.cfogrady.vitalwear.battle.BattleState
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.google.common.collect.Lists

class OpponentNameScreenFactory(private val bitmapScaler: BitmapScaler, private val imageScaler: ImageScaler, vitalBoxFactory: VitalBoxFactory, private val backgroundHeight: Dp) {
    @Composable
    fun OpponentNameScreen(battleCharacter: BattleCharacter, background: Bitmap, stateUpdater: (BattleState) -> Unit) {
        var leftScreenEarly = remember { false }
        var characterFrames = remember {Lists.newArrayList(
            battleCharacter.battleSprites.idleBitmaps[0],
            battleCharacter.battleSprites.attackBitmap)}
        BackHandler {
            leftScreenEarly = true
            stateUpdater.invoke(BattleState.END_FIGHT)
        }
        bitmapScaler.ScaledBitmap(
            bitmap = background,
            contentDescription = "Background",
            alignment = Alignment.BottomCenter
        )
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.AnimatedScaledBitmap(
                bitmaps = characterFrames,
                startIdx = 0,
                frames = 2,
                contentDescription = "Opponent",
                alignment = Alignment.BottomCenter,
                modifier = Modifier.offset(y = backgroundHeight.times(-.05f)).graphicsLayer(scaleX = -1.0f)
            )
        }
        NameBox(battleCharacter = battleCharacter)
//        Handler(Looper.getMainLooper()!!).postDelayed({
//            if(!leftScreenEarly) {
//                stateUpdater.invoke(BattleState.READY)
//            }
//        }, 5000)
    }
    
    @Composable
    private fun NameBox(battleCharacter: BattleCharacter) {
        var animation by remember { mutableStateOf(0.dp) }
        var scaledWidth = remember { imageScaler.scaledDpValueFromPixels((imageScaler.getScaling() * 80).toInt()) }
        Box(modifier = Modifier.fillMaxSize().padding(vertical =backgroundHeight.times(.1f)), contentAlignment = Alignment.TopCenter) {
            bitmapScaler.ScaledBitmap(
                bitmap = battleCharacter.battleSprites.nameBitmap,
                contentDescription = "OpponentName",
                modifier = Modifier.offset(x = animation))
        }
        Handler(Looper.getMainLooper()!!).postDelayed({
            animation -= 5.dp
        }, 100)
    }
}