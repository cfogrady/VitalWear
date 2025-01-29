package com.github.cfogrady.vitalwear.battle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.battle.composable.FightTargetFactory
import com.github.cfogrady.vitalwear.battle.data.BattleResult
import com.github.cfogrady.vitalwear.battle.data.PreBattleModel
import com.github.cfogrady.vitalwear.common.composable.util.KeepScreenOn
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import java.util.*

class BattleActivity : ComponentActivity() {
    companion object {
        const val BATTLE_CHARACTER_INFO = "BATTLE_CHARACTER"
        const val RESULT = "RESULT"
    }

    private lateinit var battleService: BattleService
    private lateinit var fightTargetFactory: FightTargetFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("Fighting Random Target onCreate")
        battleService = (application as VitalWearApp).battleService
        fightTargetFactory = (application as VitalWearApp).fightTargetFactory
        val battleCharacterInfo = buildBattleCharacterInfo(intent)

        setContent {
            Timber.i("Fighting Random Target Set Content")
            KeepScreenOn()
            if(battleCharacterInfo != null) {
                FightTarget(battleCharacterInfo) {
                    val intent = Intent()
                    intent.putExtra(RESULT, it.ordinal)
                    setResult(0, intent)
                    finish()
                }
            } else {
                FightRandomTarget()
            }
        }
    }

    private fun buildBattleCharacterInfo(intent: Intent): BattleCharacterInfo? {
        val battleCharacterBytes = intent.getByteArrayExtra(BATTLE_CHARACTER_INFO)
        battleCharacterBytes?.let {
            return BattleCharacterInfo.parseFrom(it)
        }
        return null
    }

    @Composable
    fun FightRandomTarget() {
        var battleModel by remember { mutableStateOf(Optional.empty<PreBattleModel>()) }
        Timber.i("Fighting Random Target")
        if(!battleModel.isPresent) {
            Loading(scope = Dispatchers.IO) {
                Timber.i("Fighting Random Target Loading")
                battleModel = Optional.of(battleService.createBattleModel(this))
            }
        } else {
            fightTargetFactory.FightTarget(battleModel.get()) { finish() }
        }
    }

    @Composable
    fun FightTarget(battleCharacterInfo: BattleCharacterInfo, onFinish: (BattleResult) -> Unit) {
        var battleModel by remember { mutableStateOf(Optional.empty<PreBattleModel>()) }
        if(!battleModel.isPresent) {
            Loading(scope = Dispatchers.IO) {
                battleModel = Optional.of(battleService.createBattleModel(this, battleCharacterInfo))
            }
        } else {
            fightTargetFactory.FightTarget(battleModel.get(), onFinish::invoke)
        }
    }
}