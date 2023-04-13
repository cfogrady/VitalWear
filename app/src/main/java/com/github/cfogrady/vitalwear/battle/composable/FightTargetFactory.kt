package com.github.cfogrady.vitalwear.battle.composable

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.github.cfogrady.vitalwear.activity.ImageScaler
import com.github.cfogrady.vitalwear.battle.*
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory

class FightTargetFactory(
    private val imageScaler: ImageScaler,
    private val battleManager: BattleManager,
    private val vitalBoxFactory: VitalBoxFactory,
    private val opponentSplashFactory: OpponentSplashFactory,
    private val opponentNameScreenFactory: OpponentNameScreenFactory
    ) {
    @Composable
    fun FightTarget(battleCharacter: BattleCharacter, battleBackground: Bitmap, activityFinished: () -> Unit) {
        var state by remember { mutableStateOf(BattleState.OPPONENT_SPLASH) }
        var battleConclusion by remember { mutableStateOf(BattleResult.RETREAT) }
        lateinit var battle: Battle
        val stateUpdater = {newState: BattleState -> state = newState}
        val padding = imageScaler.getPadding()
        vitalBoxFactory.VitalBox {
            when(state) {
                BattleState.OPPONENT_SPLASH -> {
                    opponentSplashFactory.OpponentSplash(battleCharacter = battleCharacter, battleBackground, stateUpdater = stateUpdater)
                }
                BattleState.OPPONENT_NAME -> {
                    opponentNameScreenFactory.OpponentNameScreen(battleCharacter, battleBackground, stateUpdater)
                }
                BattleState.READY -> {
                    activityFinished.invoke()
                    BackHandler {
                        state = BattleState.END_FIGHT
                    }
                }
                BattleState.GO -> {
                    battle = remember {battleManager.performBattle(battleCharacter)}
                    BackHandler {
                        battleConclusion = battle.battleResult
                        state = BattleState.END_FIGHT
                    }
                }
                BattleState.ATTACKING -> {
                    BackHandler {
                        battleConclusion = battle.battleResult
                        state = BattleState.END_FIGHT
                    }
                }
                BattleState.HP_COMPARE -> {
                    BackHandler {
                        battleConclusion = battle.battleResult
                        state = BattleState.END_FIGHT
                    }
                }
                BattleState.END_FIGHT -> {
                    activityFinished.invoke()
                }
                BattleState.VITALS -> TODO()
            }
        }
    }
}