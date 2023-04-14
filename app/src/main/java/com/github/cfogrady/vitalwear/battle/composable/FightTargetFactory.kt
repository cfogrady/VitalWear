package com.github.cfogrady.vitalwear.battle.composable

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import com.github.cfogrady.vitalwear.battle.*
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory

class FightTargetFactory(
    private val vitalBoxFactory: VitalBoxFactory,
    private val opponentSplashFactory: OpponentSplashFactory,
    private val opponentNameScreenFactory: OpponentNameScreenFactory,
    private val readyScreenFactory: ReadyScreenFactory
    ) {
    @Composable
    fun FightTarget(battleModel: BattleModel, activityFinished: () -> Unit) {
        var state by remember { mutableStateOf(BattleState.OPPONENT_SPLASH) }
        var battleConclusion by remember { mutableStateOf(BattleResult.RETREAT) }
        lateinit var battle: Battle
        val stateUpdater = {newState: BattleState -> state = newState}
        vitalBoxFactory.VitalBox {
            when(state) {
                BattleState.OPPONENT_SPLASH -> {
                    opponentSplashFactory.OpponentSplash(battleModel, stateUpdater = stateUpdater)
                }
                BattleState.OPPONENT_NAME -> {
                    opponentNameScreenFactory.OpponentNameScreen(battleModel, stateUpdater)
                }
                BattleState.READY -> {
                    readyScreenFactory.ReadyScreen(battleModel, stateUpdater)
                }
                BattleState.GO -> {
                    battle = remember {battleModel.performBattle()}
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