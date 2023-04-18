package com.github.cfogrady.vitalwear.battle.composable

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.battle.data.BattleCharacter
import com.github.cfogrady.vitalwear.battle.data.PreBattleModel
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.PositionOffsetRatios
import com.google.common.collect.Lists

class ReadyScreenFactory(val bitmapScaler: BitmapScaler, val backgroundHeight: Dp) {
    @Composable
    fun ReadyScreen(battleModel: PreBattleModel, stateUpdater: (FightTargetState) -> Unit) {
        var leftScreenEarly = remember { false }
        var onOpponent by remember { mutableStateOf(true) }
        BackHandler {
            Log.i("ReadyScreenFactory", "BackHandler Triggered")
            leftScreenEarly = true
            stateUpdater.invoke(FightTargetState.END_FIGHT)
        }
        bitmapScaler.ScaledBitmap(bitmap = battleModel.background, contentDescription = "Background")
        if(onOpponent) {
            Ready(battleModel.opponent, battleModel.readySprite, -1.0f)
            Handler(Looper.getMainLooper()!!).postDelayed({
                onOpponent = false
            }, 2000)
        } else {
            Ready(battleModel.partnerCharacter, battleModel.readySprite, 1.0f)
            Handler(Looper.getMainLooper()!!).postDelayed({
                if(!leftScreenEarly) {
                    stateUpdater.invoke(FightTargetState.GO)
                }
            }, 2500)
        }
    }

    @Composable
    private fun Ready(battleCharacter: BattleCharacter, readyIcon: Bitmap, direction: Float = 1.0f) {
        var characterFrames = remember {
            Lists.newArrayList(
                battleCharacter.battleSprites.idleBitmap,
                battleCharacter.battleSprites.attackBitmap)}
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.AnimatedScaledBitmap(
                bitmaps = characterFrames,
                startIdx = 0,
                frames = 2,
                contentDescription = "Opponent",
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .offset(y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM))
                    .graphicsLayer(scaleX = direction)
            )
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            bitmapScaler.ScaledBitmap(
                bitmap = readyIcon,
                contentDescription = "Ready",
                alignment = Alignment.TopCenter,
                modifier = Modifier.offset(y = backgroundHeight.times(.05f))
            )
        }
    }
}