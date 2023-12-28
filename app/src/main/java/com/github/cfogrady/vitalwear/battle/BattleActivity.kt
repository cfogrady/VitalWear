package com.github.cfogrady.vitalwear.battle

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.battle.composable.FightTargetFactory
import com.github.cfogrady.vitalwear.battle.data.PreBattleModel
import com.github.cfogrady.vitalwear.common.composable.util.KeepScreenOn
import kotlinx.coroutines.Dispatchers
import java.util.*

class BattleActivity : ComponentActivity() {
    companion object {
        const val PRE_SELECTED_TARGET = "Preselected Target"
        const val TAG = "BattleActivity"
    }

    private lateinit var battleService: BattleService
    private lateinit var fightTargetFactory: FightTargetFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Fighting Random Target onCreate")
        battleService = (application as VitalWearApp).battleService
        fightTargetFactory = (application as VitalWearApp).fightTargetFactory
        // TODO: Allow for preselecting a target
        // val preselectedTarget = intent.getBooleanExtra(PRE_SELECTED_TARGET, false)

        setContent {
            Log.i(TAG, "Fighting Random Target Set Content")
            KeepScreenOn()
            FightRandomTarget()
        }
    }

    @Composable
    fun FightRandomTarget() {
        var battleModel by remember { mutableStateOf(Optional.empty<PreBattleModel>()) }
        Log.i(TAG, "Fighting Random Target")
        if(!battleModel.isPresent) {
            Loading(scope = Dispatchers.IO) {
                Log.i(TAG, "Fighting Random Target Loading")
                battleModel = Optional.of(battleService.createBattleModel(this))
            }
        } else {
            fightTargetFactory.FightTarget(battleModel.get()) { finish() }
        }
    }
}