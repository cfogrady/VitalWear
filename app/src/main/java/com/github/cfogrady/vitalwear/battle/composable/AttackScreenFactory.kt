package com.github.cfogrady.vitalwear.battle.composable

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.battle.data.BattleModel
import com.github.cfogrady.vitalwear.battle.data.BattleResult
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler

class AttackScreenFactory(val bitmapScaler: BitmapScaler, val backgroundHeight: Dp) {

    companion object {
        enum class Attacker {
            PLAYER,
            OPPONENT,
            DONE
        }

        enum class AttackPhase {
            SHOW_HP,
            ATTACK,
            HIT_OPPONENT,
            OPPONENT_HP
        }
    }

    @Composable
    fun AttackScreen(battleModel: BattleModel, stateUpdater: (FightTargetState) -> Unit, conclusionUpdater: (BattleResult) -> Unit) {
        BackHandler {
            stateUpdater.invoke(FightTargetState.END_FIGHT)
        }
        LaunchedEffect(true) {
            battleModel.performBattle()
            conclusionUpdater.invoke(battleModel.battle.battleResult)
        }
        var attacker by remember { mutableStateOf(Attacker.PLAYER) }
        val attackChanger = {newAttacker: Attacker -> attacker = newAttacker}
        var round = remember {0}
        bitmapScaler.ScaledBitmap(bitmap = battleModel.background, contentDescription = "Background")
        if(attacker == Attacker.PLAYER) {
            PartnerAttack(battleModel, round, attackChanger)
        } else if(attacker == Attacker.OPPONENT) {

        } else {
            stateUpdater.invoke(FightTargetState.HP_COMPARE)
        }
    }

    @Composable
    private fun PartnerAttack(battleModel: BattleModel, round: Int, attackChanger: (Attacker) -> Unit) {
        var phase by remember { mutableStateOf(AttackPhase.SHOW_HP) }
        val phaseUpdater = { newPhase: AttackPhase -> phase = newPhase}
        val direction = 1.0f
        when(phase) {
            AttackPhase.SHOW_HP -> {
                val remainingHpPercent = if(round == 0) 1.0f else battleModel.battle.partnerHpAfterRound(round-1).toFloat()/battleModel.startingPartnerHp
                ShowHP(
                    characterImg = battleModel.partnerCharacter.sprites[1],
                    remainingHpPercent = remainingHpPercent,
                    hpRemainingSprites = battleModel.partnerRemainingHpSprites,
                    direction = direction,
                    phaseUpdater = phaseUpdater
                )
            }
            AttackPhase.ATTACK -> {
                Attack(
                    characterIdleImg = battleModel.partnerCharacter.sprites[1],
                    characterAttackImg = battleModel.partnerCharacter.sprites[11],
                    direction = 1.0f,
                    phaseUpdater = phaseUpdater
                )
            }
            AttackPhase.HIT_OPPONENT -> TODO()
            AttackPhase.OPPONENT_HP -> TODO()
        }
    }

    @Composable
    fun ShowHP(characterImg: Bitmap, remainingHpPercent: Float, hpRemainingSprites: List<Bitmap>, direction: Float, phaseUpdater: (AttackPhase) -> Unit) {
        val hpImgIndex = if(remainingHpPercent == 0.0f) 0 else 1 + (remainingHpPercent*5).toInt()
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.ScaledBitmap(
                bitmap = characterImg,
                contentDescription = "Opponent",
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .offset(y = backgroundHeight.times(-.05f))
                    .graphicsLayer(scaleX = direction)
            )
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            bitmapScaler.ScaledBitmap(
                bitmap = hpRemainingSprites[hpImgIndex],
                contentDescription = "Remaining HP",
                alignment = Alignment.TopCenter,
                modifier = Modifier.offset(y = backgroundHeight.times(.1f)))
        }
        Handler(Looper.getMainLooper()!!).postDelayed({
            phaseUpdater.invoke(AttackPhase.ATTACK)
        }, 1000)
    }

    @Composable
    fun Attack(characterIdleImg: Bitmap, characterAttackImg: Bitmap, direction: Float, phaseUpdater: (AttackPhase) -> Unit) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.ScaledBitmap(
                bitmap = characterIdleImg,
                contentDescription = "Opponent",
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .offset(y = backgroundHeight.times(-.05f))
                    .graphicsLayer(scaleX = direction)
            )
        }
//        Handler(Looper.getMainLooper()!!).postDelayed({
//            phaseUpdater.invoke(AttackPhase.ATTACK)
//        }, 500)
    }
}