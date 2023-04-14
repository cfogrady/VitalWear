package com.github.cfogrady.vitalwear.battle

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.battle.composable.FightTargetFactory
import java.util.*

class BattleActivity : ComponentActivity() {
    companion object {
        const val PRE_SELECTED_TARGET = "Preselected Target"
        const val TAG = "BattleActivity"
    }

    lateinit var battleModelFactory: BattleModelFactory
    lateinit var fightTargetFactory: FightTargetFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Fighting Random Target onCreate")
        battleModelFactory = (application as VitalWearApp).battleModelFactory
        fightTargetFactory = (application as VitalWearApp).fightTargetFactory
        val preselectedTarget = intent.getBooleanExtra(PRE_SELECTED_TARGET, false)
        setContent {
            Log.i(TAG, "Fighting Random Target Set Content")
            FightRandomTarget()
        }
    }

    @Composable
    fun FightRandomTarget() {
        var battleModel by remember { mutableStateOf(Optional.empty<BattleModel>()) }
        Log.i(TAG, "Fighting Random Target")
        if(!battleModel.isPresent) {
            Loading {
                Log.i(TAG, "Fighting Random Target Loading")
                battleModel = Optional.of(battleModelFactory.createBattleModel())
            }
        } else {
            fightTargetFactory.FightTarget(battleModel.get()) { finish() }
        }
    }
}