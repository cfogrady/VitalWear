package com.github.cfogrady.vitalwear.battle

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.cfogrady.vb.dim.card.BemCard
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.activity.ImageScaler
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.data.CardLoader
import java.util.*

class BattleActivity : ComponentActivity() {
    companion object {
        const val PRE_SELECTED_TARGET = "Preselected Target"
        const val TAG = "BattleActivity"

        enum class BattleState {
            OPPONENT_SPLASH,
            OPPONENT_NAME,
            READY,
            GO,
            ATTACKING,
            HP_COMPARE,
            END_FIGHT,
            VITALS,
        }
    }

    lateinit var cardLoader: CardLoader
    lateinit var characterManager: CharacterManager
    lateinit var battleManager: BattleManager
    lateinit var imageScaler: ImageScaler
    lateinit var bitmapScaler: BitmapScaler
    lateinit var battle: Battle

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        cardLoader = (application as VitalWearApp).cardLoader
        characterManager = (application as VitalWearApp).characterManager
        imageScaler = (application as VitalWearApp).imageScaler
        bitmapScaler = (application as VitalWearApp).bitmapScaler
        val preselectedTarget = intent.getBooleanExtra(PRE_SELECTED_TARGET, false)
        setContent {
            FightRandomTarget()
        }
    }

    @Composable
    fun FightRandomTarget() {
        var target by remember { mutableStateOf(Optional.empty<BattleCharacter>()) }
        var background by remember { mutableStateOf(Optional.empty<Bitmap>()) }
        if(!target.isPresent) {
            Loading {
                val character = characterManager.getActiveCharacter().value!!
                val file = character.characterStats.cardFile
                val card = cardLoader.loadCard(file)
                background = Optional.of(battleManager.getBackground(card))
                target = Optional.of(battleManager.loadRandomTarget(card))
            }
        }
        FightTarget(target.get(), background.get())
    }

    @Composable
    fun FightTarget(battleCharacter: BattleCharacter, battleBackground: Bitmap) {
        var state by remember { mutableStateOf(BattleState.OPPONENT_SPLASH) }
        var battleConclusion by remember { mutableStateOf(BattleResult.RETREAT) }
        lateinit var battle: Battle
        val stateUpdater = {newState:BattleState -> state = newState}
        val padding = imageScaler.getPadding()
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.ScaledBitmap(
                bitmap = battleBackground,
                contentDescription = "Background",
                alignment = Alignment.BottomCenter
            )
            when(state) {
                BattleState.OPPONENT_SPLASH -> {
                    OpponentSplash(battleCharacter = battleCharacter, stateUpdater = stateUpdater)
                }
                BattleState.OPPONENT_NAME -> {
                    BackHandler {
                        state = BattleState.END_FIGHT
                    }
                }
                BattleState.READY -> {
                    BackHandler {
                        state = BattleState.END_FIGHT
                    }
                }
                BattleState.GO -> {
                    battle = remember {battleManager.performBattle(battleCharacter)}
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
                BattleState.END_FIGHT -> TODO()
                BattleState.VITALS -> TODO()
            }
        }
    }

    @Composable
    fun OpponentSplash(battleCharacter: BattleCharacter, stateUpdater: (BattleState) -> Unit) {
        var leftScreenEarly = false
        BackHandler {
            leftScreenEarly = true
            stateUpdater.invoke(BattleState.END_FIGHT)
        }
        bitmapScaler.ScaledBitmap(bitmap = battleCharacter.battleSprites.splashBitmap, contentDescription = "Opponent", alignment = Alignment.BottomCenter,
        modifier = Modifier.clickable {
            leftScreenEarly = true
            stateUpdater.invoke(BattleState.READY)
        })
        Handler(Looper.getMainLooper()!!).postDelayed({
            if(!leftScreenEarly) {
                stateUpdater.invoke(BattleState.OPPONENT_NAME)
            }
        }, 500)
    }
}