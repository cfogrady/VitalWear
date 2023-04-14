package com.github.cfogrady.vitalwear.battle.composable

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.battle.BattleCharacter
import com.github.cfogrady.vitalwear.battle.BattleState
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.google.common.collect.Lists

class OpponentNameScreenFactory(private val bitmapScaler: BitmapScaler, private val backgroundHeight: Dp) {
    companion object {
        const val TAG = "OpponentNameScreenFactory"
    }

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
            alignment = Alignment.BottomCenter,
            modifier = Modifier.clickable {
                Log.i(TAG, "Continuing")
                leftScreenEarly = true
                stateUpdater.invoke(BattleState.READY)
            }
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
        Handler(Looper.getMainLooper()!!).postDelayed({
            if(!leftScreenEarly) {
                stateUpdater.invoke(BattleState.READY)
            }
        }, 5000)
    }
    
    @Composable
    private fun NameBox(battleCharacter: BattleCharacter) {
        val state = rememberScrollState()
        // Seems like there is probably a better way to do this, but this will work for now.
        LaunchedEffect(Unit) { state.animateScrollTo(240, tween(durationMillis = 3000, delayMillis = 500, easing = LinearEasing)) }
        Box(modifier = Modifier.fillMaxSize().padding(vertical =backgroundHeight.times(.1f)), contentAlignment = Alignment.TopCenter) {
            bitmapScaler.ScaledBitmap(
                bitmap = battleCharacter.battleSprites.nameBitmap,
                contentDescription = "OpponentName",
                modifier = Modifier.horizontalScroll(state).background(color = Color.Black))
        }
    }
}