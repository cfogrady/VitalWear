package com.github.cfogrady.vitalwear.battle

import android.graphics.Bitmap
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.battle.composable.FightTargetFactory
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.data.CardLoader
import java.util.*

class BattleActivity : ComponentActivity() {
    companion object {
        const val PRE_SELECTED_TARGET = "Preselected Target"
        const val TAG = "BattleActivity"
    }

    lateinit var cardLoader: CardLoader
    lateinit var characterManager: CharacterManager
    lateinit var battleManager: BattleManager
    lateinit var fightTargetFactory: FightTargetFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Fighting Random Target onCreate")
        cardLoader = (application as VitalWearApp).cardLoader
        characterManager = (application as VitalWearApp).characterManager
        battleManager = (application as VitalWearApp).battleManager
        fightTargetFactory = (application as VitalWearApp).fightTargetFactory
        val preselectedTarget = intent.getBooleanExtra(PRE_SELECTED_TARGET, false)
        setContent {
            Log.i(TAG, "Fighting Random Target Set Content")
            FightRandomTarget()
        }
    }

    @Composable
    fun FightRandomTarget() {
        var target by remember { mutableStateOf(Optional.empty<BattleCharacter>()) }
        var background by remember { mutableStateOf(Optional.empty<Bitmap>()) }
        Log.i(TAG, "Fighting Random Target")
        if(!target.isPresent) {
            Loading {
                Log.i(TAG, "Fighting Random Target Loading")
                val character = characterManager.getActiveCharacter().value!!
                val file = character.characterStats.cardFile
                val card = cardLoader.loadCard(file)
                background = Optional.of(battleManager.getBackground(card))
                target = Optional.of(battleManager.loadRandomTarget(card))
            }
        } else {
            fightTargetFactory.FightTarget(target.get(), background.get()) { finish() }
        }
    }
}