package com.github.cfogrady.vitalwear.battle.composable

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import com.github.cfogrady.vitalwear.battle.data.BattleModel
import com.github.cfogrady.vitalwear.battle.data.BattleResult
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory

class FightTargetFactory(
    private val vitalBoxFactory: VitalBoxFactory,
    private val opponentSplashFactory: OpponentSplashFactory,
    private val opponentNameScreenFactory: OpponentNameScreenFactory,
    private val readyScreenFactory: ReadyScreenFactory,
    private val goScreenFactory: GoScreenFactory,
    private val attackScreenFactory: AttackScreenFactory,
    private val hpCompareFactory: HPCompareFactory,
    private val endFightReactionFactory: EndFightReactionFactory,
    ) {
    @Composable
    fun FightTarget(battleModel: BattleModel, activityFinished: () -> Unit) {
        var state by remember { mutableStateOf(FightTargetState.OPPONENT_SPLASH) }
        var battleConclusion by remember { mutableStateOf(BattleResult.RETREAT) }
        val stateUpdater = {newState: FightTargetState -> state = newState}
        val battleConclusionUpdater = {newConclusion: BattleResult -> battleConclusion = newConclusion}
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
                    goScreenFactory.GoScreen(battleModel = battleModel, stateUpdater = stateUpdater)
                }
                FightTargetState.ATTACKING -> {
                    attackScreenFactory.AttackScreen(
                        battleModel = battleModel,
                        stateUpdater = stateUpdater,
                        conclusionUpdater = battleConclusionUpdater
                    )
                }
                FightTargetState.HP_COMPARE -> {
                    BackHandler {
                        state = FightTargetState.END_FIGHT
                    }
                    hpCompareFactory.HPCompare(battleModel = battleModel) {
                        state = FightTargetState.END_FIGHT
                    }
                }
                FightTargetState.END_FIGHT -> {
                    endFightReactionFactory.EndFightReaction(battleModel = battleModel, battleResult = battleConclusion) {
                        state = FightTargetState.VITALS
                    }
                }
                FightTargetState.VITALS -> {
                    activityFinished.invoke()
                }
            }
        }
    }
}