package com.github.cfogrady.vitalwear.battle.composable

import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.battle.data.BattleModel
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.google.common.collect.Lists

class ReadyScreenFactory(val bitmapScaler: BitmapScaler, val backgroundHeight: Dp) {
    @Composable
    fun ReadyScreen(battleModel: BattleModel, stateUpdater: (FightTargetState) -> Unit) {
        var leftScreenEarly = remember { false }
        var onOpponent by remember { mutableStateOf(true) }
        BackHandler {
            leftScreenEarly = true
            stateUpdater.invoke(FightTargetState.END_FIGHT)
        }
        bitmapScaler.ScaledBitmap(bitmap = battleModel.background, contentDescription = "Background")
        if(onOpponent) {
            ReadyEnemy(battleModel)
            Handler(Looper.getMainLooper()!!).postDelayed({
                onOpponent = false
            }, 2000)
        } else {
            ReadyPartner(battleModel)
            Handler(Looper.getMainLooper()!!).postDelayed({
                if(!leftScreenEarly) {
                    stateUpdater.invoke(FightTargetState.GO)
                }
            }, 2500)
        }
    }

    @Composable
    private fun ReadyEnemy(battleModel: BattleModel) {
        var characterFrames = remember {
            Lists.newArrayList(
                battleModel.opponent.battleSprites.idleBitmaps[0],
                battleModel.opponent.battleSprites.attackBitmap)}
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.AnimatedScaledBitmap(
                bitmaps = characterFrames,
                startIdx = 0,
                frames = 2,
                contentDescription = "Opponent",
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .offset(y = backgroundHeight.times(-.05f))
                    .graphicsLayer(scaleX = -1.0f)
            )
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            bitmapScaler.ScaledBitmap(
                bitmap = battleModel.readySprite,
                contentDescription = "Ready",
                alignment = Alignment.TopCenter,
                modifier = Modifier.offset(y = backgroundHeight.times(.05f))
            )
        }
    }

    @Composable
    private fun ReadyPartner(battleModel: BattleModel) {
        var characterFrames = remember {
            Lists.newArrayList(
                battleModel.partnerCharacter.sprites[1],
                battleModel.partnerCharacter.sprites[11])}
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.AnimatedScaledBitmap(
                bitmaps = characterFrames,
                startIdx = 0,
                frames = 2,
                contentDescription = "Opponent",
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .offset(y = backgroundHeight.times(-.05f))
            )
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            bitmapScaler.ScaledBitmap(
                bitmap = battleModel.readySprite,
                contentDescription = "Ready",
                alignment = Alignment.TopCenter,
                modifier = Modifier.offset(y = backgroundHeight.times(.05f))
            )
        }
    }
}