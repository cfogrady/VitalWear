package com.github.cfogrady.vitalwear.battle.composable

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
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
import com.github.cfogrady.vitalwear.composable.util.PositionOffsetRatios

class HPCompareFactory(val bitmapScaler: BitmapScaler, val backgroundHeight: Dp) {
    @Composable
    fun HPCompare(battleModel: BattleModel, finished: () -> Unit) {
        var showPlayer by remember { mutableStateOf(true) }
        remember {
            Handler(Looper.getMainLooper()!!).postDelayed({
                showPlayer = false
            }, 1000)
            Handler(Looper.getMainLooper()!!).postDelayed({
                finished.invoke()
            }, 2000)
        }
        val direction = if(showPlayer) 1.0f else -1.0f
        bitmapScaler.ScaledBitmap(bitmap = battleModel.background, contentDescription = "Background")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.ScaledBitmap(bitmap = character(battleModel, showPlayer), contentDescription = "Model",
            modifier = Modifier.offset(y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM))
                .graphicsLayer(scaleX = direction))
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            bitmapScaler.ScaledBitmap(bitmap = remainingHp(battleModel, showPlayer), contentDescription = "Remaining HP",
            modifier = Modifier.offset(y = backgroundHeight.times(PositionOffsetRatios.HEALTH_OFFSET_FROM_TOP)))
        }
    }

    fun character(battleModel: BattleModel, player: Boolean): Bitmap {
        if(player) {
            return battleModel.partnerCharacter.sprites[1]
        }
        return battleModel.opponent.battleSprites.idleBitmaps[0]
    }

    fun remainingHp(battleModel: BattleModel, player: Boolean): Bitmap {
        val finalRound = battleModel.battle.finalRound()
        if(player) {
            return battleModel.partnerHpSprite(finalRound)
        }
        return battleModel.opponentHpSprite(finalRound)
    }
}