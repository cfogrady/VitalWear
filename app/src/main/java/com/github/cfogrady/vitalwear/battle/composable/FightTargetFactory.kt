package com.github.cfogrady.vitalwear.battle.composable

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import com.github.cfogrady.vitalwear.battle.data.Battle
import com.github.cfogrady.vitalwear.battle.data.BattleModel
import com.github.cfogrady.vitalwear.battle.data.BattleResult
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory

class FightTargetFactory(
    private val vitalBoxFactory: VitalBoxFactory,
    private val opponentSplashFactory: OpponentSplashFactory,
    private val opponentNameScreenFactory: OpponentNameScreenFactory,
    private val readyScreenFactory: ReadyScreenFactory
    ) {
    @Composable
    fun FightTarget(battleModel: BattleModel, activityFinished: () -> Unit) {
        var state by remember { mutableStateOf(FightTargetState.OPPONENT_SPLASH) }
        var battleConclusion by remember { mutableStateOf(BattleResult.RETREAT) }
        lateinit var battle: Battle
        val stateUpdater = {newState: FightTargetState -> state = newState}
        vitalBoxFactory.VitalBox {
            when(state) {
                FightTargetState.OPPONENT_SPLASH -> {
                    opponentSplashFactory.OpponentSplash(battleModel, stateUpdater = stateUpdater)
                }
                FightTargetState.OPPONENT_NAME -> {
                    opponentNameScreenFactory.OpponentNameScreen(battleModel, stateUpdater)
                }
                FightTargetState.READY -> {
                    readyScreenFactory.ReadyScreen(battleModel, stateUpdater)
                }
                FightTargetState.GO -> {
                    battle = remember {battleModel.performBattle()}
                    BackHandler {
                        battleConclusion = battle.battleResult
                        state = FightTargetState.END_FIGHT
                    }
                }
                FightTargetState.ATTACKING -> {
                    BackHandler {
                        battleConclusion = battle.battleResult
                        state = FightTargetState.END_FIGHT
                    }
                }
                FightTargetState.HP_COMPARE -> {
                    BackHandler {
                        battleConclusion = battle.battleResult
                        state = FightTargetState.END_FIGHT
                    }
                }
                FightTargetState.END_FIGHT -> {
                    activityFinished.invoke()
                }
                FightTargetState.VITALS -> TODO()
            }
        }
    }
}