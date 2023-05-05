package com.github.cfogrady.vitalwear.battle.composable

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
import com.github.cfogrady.vitalwear.battle.data.BattleCharacter
import com.github.cfogrady.vitalwear.battle.data.PreBattleModel
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.PositionOffsetRatios
import com.github.cfogrady.vitalwear.composable.util.ScrollingNameFactory
import com.google.common.collect.Lists

class OpponentNameScreenFactory(private val bitmapScaler: BitmapScaler, private val backgroundHeight: Dp, private val scrollingNameFactory: ScrollingNameFactory) {
    companion object {
        const val TAG = "OpponentNameScreenFactory"
    }

    @Composable
    fun OpponentNameScreen(battleModel: PreBattleModel, stateUpdater: (FightTargetState) -> Unit) {
        var leftScreenEarly = remember { false }
        val battleCharacter = battleModel.opponent
        val nameSprite = battleCharacter.battleSprites.nameBitmap
        val nameAnimationTime = remember(nameSprite) { 500 + (2000 * nameSprite.width.toLong()/80) }
        var characterFrames = remember {Lists.newArrayList(
            battleCharacter.battleSprites.idleBitmap,
            battleCharacter.battleSprites.attackBitmap)}
        BackHandler {
            leftScreenEarly = true
            stateUpdater.invoke(FightTargetState.END_FIGHT)
        }
        bitmapScaler.ScaledBitmap(
            bitmap = battleModel.background,
            contentDescription = "Background",
            alignment = Alignment.BottomCenter,
            modifier = Modifier.clickable {
                Log.i(TAG, "Continuing")
                leftScreenEarly = true
                stateUpdater.invoke(FightTargetState.READY)
            }
        )
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.AnimatedScaledBitmap(
                bitmaps = characterFrames,
                startIdx = 0,
                frames = 2,
                contentDescription = "Opponent",
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .offset(y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM))
                    .graphicsLayer(scaleX = -1.0f)
            )
        }
        NameBox(battleCharacter = battleCharacter)
        Handler(Looper.getMainLooper()!!).postDelayed({
            if(!leftScreenEarly) {
                stateUpdater.invoke(FightTargetState.READY)
            }
        }, nameAnimationTime*2)
    }
    
    @Composable
    private fun NameBox(battleCharacter: BattleCharacter) {

        Box(modifier = Modifier.fillMaxWidth().padding(vertical = backgroundHeight.times(.1f)).background(Color.Black)) {
            scrollingNameFactory.ScrollingName(name = battleCharacter.battleSprites.nameBitmap)
        }
    }
}