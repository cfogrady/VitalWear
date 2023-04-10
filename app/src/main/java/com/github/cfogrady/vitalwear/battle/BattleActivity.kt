package com.github.cfogrady.vitalwear.battle

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.github.cfogrady.vb.dim.card.BemCard
import com.github.cfogrady.vb.dim.card.Card
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vb.dim.character.BemCharacterStats.BemCharacterStatEntry
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.data.CardLoader
import java.util.Random

class BattleActivity : ComponentActivity() {
    companion object {
        const val PRE_SELECTED_TARGET = "Preselected Target"
        const val TAG = "BattleActivity"
    }

    lateinit var cardLoader: CardLoader
    lateinit var characterManager: CharacterManager
    val random = Random()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        cardLoader = (application as VitalWearApp).cardLoader
        characterManager = (application as VitalWearApp).characterManager
        val preselectedTarget = intent.getBooleanExtra(PRE_SELECTED_TARGET, false)
        setContent {
            FightRandomTarget()
        }
    }

    @Composable
    fun FightRandomTarget() {
        val target by remember { mutableStateOf(false) }
        if(!target) {
            Loading {
                val target = loadRandomTarget()
            }
        }
    }

    fun loadRandomTarget() {
        val character = characterManager.getActiveCharacter().value!!
        val file = character.characterStats.cardFile
        val card = cardLoader.loadCard(file)
        val opponentSpeciesIdx = assignRandomTargetFromCard(character.speciesStats.stage, card)

    }

    private fun assignRandomTargetFromCard(activeStage: Int, card: Card<*, *, *, *, *, *>): Int {
        var roll = random.nextInt(100)
        while(roll >= 0) {
            for((index, species) in card.characterStats.characterEntries.withIndex()) {
                if(activeStage < 4) {
                    if(species.firstPoolBattleChance != DimReader.NONE_VALUE) {
                        roll -= species.firstPoolBattleChance
                    }
                } else if(activeStage < 6 || !(species is BemCharacterStatEntry)) {
                    if(species.secondPoolBattleChance != DimReader.NONE_VALUE) {
                        roll -= species.secondPoolBattleChance
                    }
                } else {
                    if(species.thirdPoolBattleChance != DimReader.NONE_VALUE) {
                        roll -= species.thirdPoolBattleChance
                    }
                }
                if(roll < 0) {
                    return index
                }
            }
            Log.w(TAG, "Rolled for an invalid species... This is a bad card image." +
                    "Running through the options until our roll is reduced to 0.")
        }
        throw java.lang.IllegalStateException("If we get here then we somehow failed to return the species that brought out role under 0.")
    }
}