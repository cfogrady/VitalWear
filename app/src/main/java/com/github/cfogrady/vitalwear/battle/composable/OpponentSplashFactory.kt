package com.github.cfogrady.vitalwear.battle.composable

import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.cfogrady.vitalwear.battle.data.BattleModel
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler

class OpponentSplashFactory(private val bitmapScaler: BitmapScaler) {

    @Composable
    fun OpponentSplash(battleModel: BattleModel, stateUpdater: (FightTargetState) -> Unit) {
        var leftScreenEarly = remember { false }
        BackHandler {
            leftScreenEarly = true
            stateUpdater.invoke(FightTargetState.END_FIGHT)
        }
        bitmapScaler.ScaledBitmap(
            bitmap = battleModel.background,
            contentDescription = "Background",
            alignment = Alignment.BottomCenter
        )
        val battleCharacter = battleModel.opponent
        bitmapScaler.ScaledBitmap(bitmap = battleCharacter.battleSprites.splashBitmap, contentDescription = "Opponent", alignment = Alignment.BottomCenter,
            modifier = Modifier.clickable {
                leftScreenEarly = true
                stateUpdater.invoke(FightTargetState.READY)
            })
        Handler(Looper.getMainLooper()!!).postDelayed({
            if(!leftScreenEarly) {
                stateUpdater.invoke(FightTargetState.OPPONENT_NAME)
            }
        }, 1000)
    }
}