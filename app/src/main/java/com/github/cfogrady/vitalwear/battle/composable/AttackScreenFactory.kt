package com.github.cfogrady.vitalwear.battle.composable

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.battle.data.BattleModel
import com.github.cfogrady.vitalwear.battle.data.BattleResult
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.PositionOffsetRatios
import com.google.common.collect.Lists

class AttackScreenFactory(val bitmapScaler: BitmapScaler, val backgroundHeight: Dp) {

    companion object {
        const val TAG = "AttackScreenFactory"
        enum class Attacker {
            PLAYER,
            OPPONENT,
            DONE
        }

        enum class AttackPhase {
            SHOW_HP,
            JUMP_BACK,
            CUT_IN,
            ATTACK,
            OPPONENT_RECEIVES_ATTACK,
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
        var round by remember { mutableStateOf(0) }
        bitmapScaler.ScaledBitmap(bitmap = battleModel.background, contentDescription = "Background")
        //TODO: Converter to create a BattleCharacter from the partnerCharacter and consolidate PartnerAttack and OpponentAttack into single method
        if(attacker == Attacker.PLAYER) {
            PartnerAttack(battleModel, round, attackChanger)
        } else if(attacker == Attacker.OPPONENT) {
            OpponentAttack(battleModel = battleModel, round = round) {
                if(round == battleModel.battle.finalRound()) {
                    stateUpdater.invoke(FightTargetState.HP_COMPARE)
                } else {
                    round++;
                    attacker = Attacker.PLAYER
                }
            }
        }
    }

    @Composable
    private fun PartnerAttack(battleModel: BattleModel, round: Int, attackChanger: (Attacker) -> Unit) {
        var phase by remember { mutableStateOf(AttackPhase.SHOW_HP) }
        val phaseUpdater = { newPhase: AttackPhase -> phase = newPhase}
        val direction = 1.0f
        val critical = round == battleModel.partnerCharacter.speciesStats.type
        when(phase) {
            AttackPhase.SHOW_HP -> {
                val remainingHpSprite = if(round == 0) battleModel.partnerRemainingHpSprites[6] else battleModel.partnerHpSprite(round-1)
                ShowHP(
                    characterImg = battleModel.partnerCharacter.sprites[1],
                    hpRemainingSprite = remainingHpSprite,
                    direction = direction,
                    phaseUpdater = phaseUpdater
                )
            }
            AttackPhase.JUMP_BACK -> {
                JumpBack(
                    characterAttackImg = battleModel.partnerCharacter.sprites[11],
                    direction = direction,
                    critical = critical,
                    phaseUpdater = phaseUpdater
                )
            }
            AttackPhase.CUT_IN -> {
                CutIn(cutIn = battleModel.partnerCharacter.sprites[13], phaseUpdater = phaseUpdater)
            }
            AttackPhase.ATTACK -> {
                Attack(
                    characterIdleImg = battleModel.partnerCharacter.sprites[1],
                    characterAttackImg = battleModel.partnerCharacter.sprites[11],
                    attack = if(critical) battleModel.partnerLargeAttack else battleModel.partnerAttack ,
                    direction = direction,
                    phaseUpdater = phaseUpdater
                )
            }
            AttackPhase.OPPONENT_RECEIVES_ATTACK -> {
                OpponentReceivesAttack(
                    characterIdleSprite = battleModel.opponent.battleSprites.idleBitmaps[0],
                    characterDodgeSprite = battleModel.opponent.battleSprites.dodgeBitmap,
                    attackSprite = if(critical) battleModel.partnerLargeAttack else battleModel.partnerAttack ,
                    hitSprites = battleModel.opponent.battleSprites.hits,
                    wasHit = battleModel.battle.partnerLandedHitOnRound(round),
                    direction = direction*-1,
                    phaseUpdater = phaseUpdater,
                )
            }
            AttackPhase.OPPONENT_HP -> {
                val oldHpSprite = if(round == 0) battleModel.opponentRemainingHpSprites[6] else battleModel.opponentHpSprite(round-1)
                val newHpSprite = battleModel.opponentHpSprite(round)
                OpponentHp(characterSprite = battleModel.opponent.battleSprites.idleBitmaps[0],
                    oldHpSprite = oldHpSprite,
                    newHpSprite = newHpSprite,
                    wasHit = battleModel.battle.partnerLandedHitOnRound(round),
                    direction = direction*-1) {
                    attackChanger.invoke(Attacker.OPPONENT)
                }
            }
        }
    }

    @Composable
    private fun OpponentAttack(battleModel: BattleModel, round: Int, roundFinished: () -> Unit) {
        var phase by remember { mutableStateOf(AttackPhase.SHOW_HP) }
        val phaseUpdater = { newPhase: AttackPhase -> phase = newPhase}
        val direction = -1.0f
        val critical = round == battleModel.opponent.battleStats.type
        when(phase) {
            AttackPhase.SHOW_HP -> {
                ShowHP(
                    characterImg = battleModel.opponent.battleSprites.idleBitmaps[0],
                    hpRemainingSprite = battleModel.opponentHpSprite(round),
                    direction = direction,
                    phaseUpdater = phaseUpdater
                )
            }
            AttackPhase.JUMP_BACK -> {
                JumpBack(
                    characterAttackImg = battleModel.opponent.battleSprites.attackBitmap,
                    direction = direction,
                    critical = critical,
                    phaseUpdater = phaseUpdater
                )
            }
            AttackPhase.CUT_IN -> {
                CutIn(cutIn = battleModel.opponent.battleSprites.splashBitmap, phaseUpdater = phaseUpdater)
            }
            AttackPhase.ATTACK -> {
                Attack(
                    characterIdleImg = battleModel.opponent.battleSprites.idleBitmaps[0],
                    characterAttackImg = battleModel.opponent.battleSprites.attackBitmap,
                    attack = if(critical) battleModel.opponent.battleSprites.strongProjectileBitmap else battleModel.opponent.battleSprites.projectileBitmap,
                    direction = direction,
                    phaseUpdater = phaseUpdater
                )
            }
            AttackPhase.OPPONENT_RECEIVES_ATTACK -> {
                OpponentReceivesAttack(
                    characterIdleSprite = battleModel.partnerCharacter.sprites[1],
                    characterDodgeSprite = battleModel.partnerCharacter.sprites[12],
                    attackSprite = if(critical) battleModel.opponent.battleSprites.strongProjectileBitmap else battleModel.opponent.battleSprites.projectileBitmap,
                    hitSprites = battleModel.partnerHits,
                    wasHit = battleModel.battle.enemyLandedHitOnRound(round),
                    direction = direction*-1,
                    phaseUpdater = phaseUpdater,
                )
            }
            AttackPhase.OPPONENT_HP -> {
                val oldHpSprite = if(round == 0) battleModel.partnerRemainingHpSprites[6] else battleModel.partnerHpSprite(round-1)
                val newHpSprite = battleModel.partnerHpSprite(round)
                OpponentHp(characterSprite = battleModel.partnerCharacter.sprites[1],
                    oldHpSprite = oldHpSprite,
                    newHpSprite = newHpSprite,
                    wasHit = battleModel.battle.enemyLandedHitOnRound(round),
                    direction = direction*-1) {
                    roundFinished.invoke()
                }
            }
        }
    }

    @Composable
    fun ShowHP(characterImg: Bitmap, hpRemainingSprite: Bitmap, direction: Float, phaseUpdater: (AttackPhase) -> Unit) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.ScaledBitmap(
                bitmap = characterImg,
                contentDescription = "Attacker",
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .offset(y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM))
                    .graphicsLayer(scaleX = direction)
            )
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            bitmapScaler.ScaledBitmap(
                bitmap = hpRemainingSprite,
                contentDescription = "Remaining HP",
                alignment = Alignment.TopCenter,
                modifier = Modifier.offset(y = backgroundHeight.times(PositionOffsetRatios.HEALTH_OFFSET_FROM_TOP)))
        }
        Handler(Looper.getMainLooper()!!).postDelayed({
            phaseUpdater.invoke(AttackPhase.JUMP_BACK)
        }, 1000)
    }

    @Composable
    fun JumpBack(characterAttackImg: Bitmap, direction: Float, critical: Boolean ,phaseUpdater: (AttackPhase) -> Unit) {
        var verticalTarget by remember { mutableStateOf(0f)}
        var horizontalTarget by remember { mutableStateOf(0f) }
        val verticalOffset by animateFloatAsState(
            targetValue = verticalTarget,
            animationSpec = tween(durationMillis = 500),
            finishedListener = {final -> verticalTarget = 0f}
        )
        val horizontalOffset by animateFloatAsState(
            targetValue = horizontalTarget,
            animationSpec = tween(durationMillis = 1200)
        )
        LaunchedEffect(true) {
            verticalTarget = 1f
            horizontalTarget = 1f
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.ScaledBitmap(
                bitmap = characterAttackImg,
                contentDescription = "Attacker",
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .offset(
                        y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM + (-.15f * verticalOffset)),
                        x = backgroundHeight.times(direction * (PositionOffsetRatios.CHARACTER_JUMP_BACK_POSITION * horizontalOffset))
                    )
                    .graphicsLayer(scaleX = direction)
            )
        }
        Handler(Looper.getMainLooper()!!).postDelayed({
            if(critical) {
                phaseUpdater.invoke(AttackPhase.CUT_IN)
            } else {
                phaseUpdater.invoke(AttackPhase.ATTACK)
            }
        }, 1300)
    }

    @Composable
    fun CutIn(cutIn: Bitmap, phaseUpdater: (AttackPhase) -> Unit) {
        Handler(Looper.getMainLooper()!!).postDelayed({
            phaseUpdater.invoke(AttackPhase.ATTACK)
        }, 1000)
        bitmapScaler.ScaledBitmap(bitmap = cutIn, contentDescription = "Cut In")
    }

    @Composable
    fun Attack(characterIdleImg: Bitmap, characterAttackImg: Bitmap, attack: Bitmap, direction: Float, phaseUpdater: (AttackPhase) -> Unit) {
        var idle by remember { mutableStateOf(true) }
        var targetAttackOffset by remember { mutableStateOf(0f) }
        val attackOffset by animateFloatAsState(
            targetValue = targetAttackOffset,
            animationSpec = tween(durationMillis = 1500)
        )
        Handler(Looper.getMainLooper()!!).postDelayed({
            idle = false
            targetAttackOffset = 1f
        }, 500)
        Handler(Looper.getMainLooper()!!).postDelayed({
            phaseUpdater.invoke(AttackPhase.OPPONENT_RECEIVES_ATTACK)
        }, 2000)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.ScaledBitmap(
                bitmap = if(idle) characterIdleImg else characterAttackImg,
                contentDescription = "Attacker",
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .offset(
                        y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM),
                        x = backgroundHeight.times(direction * PositionOffsetRatios.CHARACTER_JUMP_BACK_POSITION)
                    )
                    .graphicsLayer(scaleX = direction)
            )
            if(!idle) {
                bitmapScaler.ScaledBitmap(
                    bitmap = attack,
                    contentDescription = "attack",
                    modifier = Modifier
                        .offset(
                            y = backgroundHeight.times(PositionOffsetRatios.ATTACK_OFFSET_FROM_BOTTOM),
                            x = backgroundHeight.times(direction * (PositionOffsetRatios.ATTACK_OFFSET_FROM_CENTER + -.45f * attackOffset))
                        )
                        .graphicsLayer(scaleX = direction)
                )
            }
        }
    }

    @Composable
    fun OpponentReceivesAttack(characterIdleSprite: Bitmap, characterDodgeSprite: Bitmap, attackSprite: Bitmap, hitSprites: List<Bitmap>, direction: Float, wasHit: Boolean, phaseUpdater: (AttackPhase) -> Unit) {
        var targetAttackOffset by remember { mutableStateOf(-0.6f) }
        var attackSpeed by remember { mutableStateOf(1000) }
        var attackHitting by remember { mutableStateOf(false) }
        var dodging by remember { mutableStateOf(false) }
        var dodgingOffsetTarget by remember { mutableStateOf(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM) }
        val attackOffset by animateFloatAsState(
            targetValue = targetAttackOffset,
            animationSpec = tween(durationMillis = attackSpeed, easing = LinearEasing),
            finishedListener = {fnished ->
                if(wasHit) {
                    attackHitting = true
                } else {
                    dodgingOffsetTarget = PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM
                }
            }
        )
        val dodgeOffset by animateFloatAsState(
            targetValue = dodgingOffsetTarget,
            animationSpec = tween(durationMillis = attackSpeed/2),
            finishedListener = {finished ->
                if(finished == PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM) {
                    dodging = false;
                    Handler(Looper.getMainLooper()!!).postDelayed({
                        phaseUpdater.invoke(AttackPhase.OPPONENT_HP)
                    }, 500)
                }
            }
        )
        LaunchedEffect(true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                if(wasHit) {
                    attackSpeed = 750
                    targetAttackOffset = 0f
                } else {
                    attackSpeed = 1500
                    targetAttackOffset = 0.6f
                    dodging = true
                    dodgingOffsetTarget = -0.4f
                }
            }, 500)
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            if(!attackHitting) {
                if(wasHit) {
                    bitmapScaler.ScaledBitmap(
                        bitmap = if(dodging) characterDodgeSprite else characterIdleSprite,
                        contentDescription = "AttackReceiver",
                        alignment = Alignment.BottomCenter,
                        modifier = Modifier
                            .offset(
                                y = backgroundHeight.times(dodgeOffset),
                            )
                            .graphicsLayer(scaleX = direction)
                    )
                    bitmapScaler.ScaledBitmap(
                        bitmap = attackSprite,
                        contentDescription = "attack",
                        modifier = Modifier
                            .offset(
                                y = backgroundHeight.times(PositionOffsetRatios.ATTACK_OFFSET_FROM_BOTTOM),
                                x = backgroundHeight.times(direction * attackOffset)
                            )
                            .graphicsLayer(scaleX = direction * -1.0f)
                    )
                } else {
                    bitmapScaler.ScaledBitmap(
                        bitmap = attackSprite,
                        contentDescription = "attack",
                        modifier = Modifier
                            .offset(
                                y = backgroundHeight.times(PositionOffsetRatios.ATTACK_OFFSET_FROM_BOTTOM),
                                x = backgroundHeight.times(direction * attackOffset)
                            )
                            .graphicsLayer(scaleX = direction * -1.0f)
                    )
                    bitmapScaler.ScaledBitmap(
                        bitmap = if(dodging) characterDodgeSprite else characterIdleSprite,
                        contentDescription = "AttackReceiver",
                        alignment = Alignment.BottomCenter,
                        modifier = Modifier
                            .offset(
                                y = backgroundHeight.times(dodgeOffset),
                            )
                            .graphicsLayer(scaleX = direction)
                    )
                }

            } else {
                Handler(Looper.getMainLooper()!!).postDelayed({
                    phaseUpdater.invoke(AttackPhase.OPPONENT_HP)
                }, 1750)
                bitmapScaler.AnimatedScaledBitmap(
                    bitmaps = hitSprites,
                    startIdx = 0,
                    frames = 3,
                    contentDescription = "Hit",
                    msPerFrame = 300,
                )
            }
        }
    }

    @Composable
    fun OpponentHp(characterSprite: Bitmap, wasHit: Boolean, oldHpSprite: Bitmap, newHpSprite: Bitmap, direction: Float, roundEnd: () -> Unit) {
        val attacks = remember { Lists.newArrayList(oldHpSprite, newHpSprite) }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.ScaledBitmap(
                bitmap = characterSprite,
                contentDescription = "Attacker",
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .offset(y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM))
                    .graphicsLayer(scaleX = direction)
            )
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            if(wasHit) {
                bitmapScaler.AnimatedScaledBitmap(
                    bitmaps = attacks,
                    startIdx = 0,
                    frames = 2,
                    contentDescription = "Remaining HP",
                    msPerFrame = 500,
                    modifier = Modifier.offset(y = backgroundHeight.times(PositionOffsetRatios.HEALTH_OFFSET_FROM_TOP))
                )
            } else {
                bitmapScaler.ScaledBitmap(bitmap = oldHpSprite, contentDescription = "Remaining HP",
                modifier = Modifier.offset(y = backgroundHeight.times(.1f)))
            }
        }
        Handler(Looper.getMainLooper()!!).postDelayed({
            roundEnd.invoke()
        }, if(wasHit) 1500 else 500)
    }

}