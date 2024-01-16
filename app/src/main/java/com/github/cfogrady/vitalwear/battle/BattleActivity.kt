package com.github.cfogrady.vitalwear.battle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.battle.composable.FightTargetFactory
import com.github.cfogrady.vitalwear.battle.data.BattleCharacterInfo
import com.github.cfogrady.vitalwear.battle.data.BattleResult
import com.github.cfogrady.vitalwear.battle.data.PreBattleModel
import com.github.cfogrady.vitalwear.common.composable.util.KeepScreenOn
import kotlinx.coroutines.Dispatchers
import java.util.*

class BattleActivity : ComponentActivity() {
    companion object {
        const val CARD_NAME = "CARD_NAME"
        const val CHARACTER_ID = "CHARACTER_ID"
        const val OPPONENT_BP = "BP"
        const val OPPONENT_HP = "HP"
        const val OPPONENT_AP = "AP"
        const val OPPONENT_ATTACK = "ATTACK"
        const val OPPONENT_CRITICAL = "CRITICAL_ATTACK"
        const val BACKGROUND = "BATTLE_BACKGROUND"
        const val RESULT = "RESULT"
        const val TAG = "BattleActivity"
    }

    private lateinit var battleService: BattleService
    private lateinit var fightTargetFactory: FightTargetFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Fighting Random Target onCreate")
        battleService = (application as VitalWearApp).battleService
        fightTargetFactory = (application as VitalWearApp).fightTargetFactory
        val battleCharacterInfo = buildBattleCharacterInfo(intent)

        setContent {
            Log.i(TAG, "Fighting Random Target Set Content")
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
        val cardName = intent.getStringExtra(CARD_NAME) ?: return null
        val characterId = intent.getIntExtra(CHARACTER_ID, -1)
        if(characterId == -1) {
            return null
        }
        val bp = intent.getIntExtra(OPPONENT_BP, -1)
        val hp = intent.getIntExtra(OPPONENT_HP, -1)
        val ap = intent.getIntExtra(OPPONENT_AP, -1)
        val attack = intent.getIntExtra(OPPONENT_ATTACK, -1)
        val critical = intent.getIntExtra(OPPONENT_CRITICAL, -1)
        val battleBackground = intent.getIntExtra(BACKGROUND, -1)
        return BattleCharacterInfo(
            cardName,
            characterId,
            if(bp == -1) null else bp,
            if(hp == -1) null else hp,
            if(ap == -1) null else ap,
            if(attack == -1) null else attack,
            if(critical == -1) null else critical,
            if(battleBackground == -1) null else battleBackground
            )
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