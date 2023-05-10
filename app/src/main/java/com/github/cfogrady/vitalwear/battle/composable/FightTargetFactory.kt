package com.github.cfogrady.vitalwear.battle.composable

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.github.cfogrady.vitalwear.battle.data.PostBattleModel
import com.github.cfogrady.vitalwear.battle.data.BattleResult
import com.github.cfogrady.vitalwear.battle.data.BattleService
import com.github.cfogrady.vitalwear.battle.data.PreBattleModel
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory

class FightTargetFactory(
    private val battleService: BattleService,
    private val vitalBoxFactory: VitalBoxFactory,
    private val opponentSplashFactory: OpponentSplashFactory,
    private val opponentNameScreenFactory: OpponentNameScreenFactory,
    private val readyScreenFactory: ReadyScreenFactory,
    private val goScreenFactory: GoScreenFactory,
    private val attackScreenFactory: AttackScreenFactory,
    private val hpCompareFactory: HPCompareFactory,
    private val endFightReactionFactory: EndFightReactionFactory,
    private val endFightVitalsFactory: EndFightVitalsFactory,
    ) {
    @Composable
    fun FightTarget(battleModel: PreBattleModel, activityFinished: () -> Unit) {
        var state by remember { mutableStateOf(FightTargetState.OPPONENT_SPLASH) }
        var battleConclusion by remember { mutableStateOf(BattleResult.RETREAT) }
        var postBattle by remember { mutableStateOf(null as PostBattleModel?) }
        val context = LocalContext.current
        val stateUpdater = {newState: FightTargetState -> state = newState}
        vitalBoxFactory.VitalBox {
            when(state) {
                //TODO: Add Battle splash screen
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
                    BackHandler {
                        stateUpdater.invoke(FightTargetState.END_FIGHT)
                    }
                    goScreenFactory.GoScreen(battleModel = battleModel) {
                        state = FightTargetState.ATTACKING
                        postBattle = battleService.performBattle(context, battleModel)
                        battleConclusion = postBattle!!.battle.battleResult
                    }
                }
                FightTargetState.ATTACKING -> {
                    BackHandler {
                        stateUpdater.invoke(FightTargetState.END_FIGHT)
                    }
                    attackScreenFactory.AttackScreen(
                        battleModel = postBattle!!
                    ) {
                        state = FightTargetState.HP_COMPARE
                    }
                }
                FightTargetState.HP_COMPARE -> {
                    BackHandler {
                        state = FightTargetState.END_FIGHT
                    }
                    hpCompareFactory.HPCompare(battleModel = postBattle!!) {
                        state = FightTargetState.END_FIGHT
                    }
                }
                FightTargetState.END_FIGHT -> {
                    //back to normal background
                    endFightReactionFactory.EndFightReaction(battleResult = battleConclusion, battleModel.background) {
                        state = FightTargetState.VITALS
                    }
                }
                FightTargetState.VITALS -> {
                    //back to normal background
                    endFightVitalsFactory.EndFightVitals(postBattleModel = postBattle!!) {
                        activityFinished.invoke()
                    }
                }
            }
        }
    }
}