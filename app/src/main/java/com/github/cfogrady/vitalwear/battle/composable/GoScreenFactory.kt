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
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.battle.data.PreBattleModel
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.PositionOffsetRatios

class GoScreenFactory(val bitmapScaler: BitmapScaler, val backgroundHeight: Dp) {
    @Composable
    fun GoScreen(battleModel: PreBattleModel, finished: () -> Unit) {
        bitmapScaler.ScaledBitmap(bitmap = battleModel.background, contentDescription = "Background")
        GoPartner(battleModel)
        Handler(Looper.getMainLooper()!!).postDelayed({
            finished.invoke()
        }, 500)
    }

    @Composable
    private fun GoPartner(battleModel: PreBattleModel) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.ScaledBitmap(
                bitmap = battleModel.partnerCharacter.battleSprites.attackBitmap,
                contentDescription = "Opponent",
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .offset(y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM))
            )
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            bitmapScaler.ScaledBitmap(
                bitmap = battleModel.goSprite,
                contentDescription = "Go",
                alignment = Alignment.TopCenter,
                modifier = Modifier.offset(y = backgroundHeight.times(.05f))
            )
        }
    }
}