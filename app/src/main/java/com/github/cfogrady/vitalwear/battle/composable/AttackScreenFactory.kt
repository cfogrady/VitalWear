package com.github.cfogrady.vitalwear.battle.composable

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
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
import com.github.cfogrady.vitalwear.battle.data.PostBattleModel
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.PositionOffsetRatios
import com.google.common.collect.Lists

class AttackScreenFactory(val bitmapScaler: BitmapScaler, val backgroundHeight: Dp) {

    companion object {
        const val TAG = "AttackScreenFactory"

        const val ATTACK_TIME_PER_SCREEN_HEIGHT = -1500
        const val ADDITIONAL_OFFSET_TO_LEAVE_SCREEN = -.275f
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
    fun AttackScreen(battleModel: PostBattleModel, finished: () -> Unit) {
        var attacker by remember { mutableStateOf(Attacker.PLAYER) }
        var round by remember { mutableStateOf(0) }
        bitmapScaler.ScaledBitmap(bitmap = battleModel.background, contentDescription = "Background")
        if(attacker == Attacker.PLAYER) {
            Attack(battleModel, true, round) {
                attacker = Attacker.OPPONENT
            }
        } else if(attacker == Attacker.OPPONENT) {
            Attack(battleModel = battleModel, isPartnerAttacking = false, round = round) {
                if(round == battleModel.battle.finalRound()) {
                    finished.invoke()
                } else {
                    round++;
                    attacker = Attacker.PLAYER
                }
            }
        }
    }

    @Composable
    private fun Attack(battleModel: PostBattleModel, isPartnerAttacking: Boolean, round: Int, finisher: () -> Unit) {
        var phase by remember { mutableStateOf(AttackPhase.SHOW_HP) }
        val direction = if(isPartnerAttacking) 1.0f else -1.0f
        val attackingCharacter = if(isPartnerAttacking) battleModel.partnerCharacter else battleModel.opponent
        val defendingCharacter = if(isPartnerAttacking) battleModel.opponent else battleModel.partnerCharacter
        val critical = round == attackingCharacter.battleStats.type
        val attackHit = if(isPartnerAttacking) battleModel.battle.partnerLandedHitOnRound(round) else battleModel.battle.enemyLandedHitOnRound(round)
        val isSupportRound = battleModel.roundHasSupportAttack(round) && isPartnerAttacking
        val wasSupported = battleModel.roundHasSupportAttack(round) && !isPartnerAttacking
        when(phase) {
            AttackPhase.SHOW_HP -> {
                val remainingHpSprite = attackerHpRemainingSprite(battleModel, isPartnerAttacking, round)
                ShowHP(
                    characterImg = attackingCharacter.battleSprites.idleBitmap,
                    hpRemainingSprite = remainingHpSprite,
                    direction = direction) {
                    phase = if(isSupportRound) {
                        AttackPhase.CUT_IN
                    } else {
                        AttackPhase.JUMP_BACK
                    }
                }
            }
            AttackPhase.JUMP_BACK -> {
                JumpBack(
                    characterAttackImg = attackingCharacter.battleSprites.attackBitmap,
                    supportAttackImg = if(isSupportRound) battleModel.supportCharacter?.attackBitmap else null,
                    direction = direction,
                    critical = critical) {
                    phase = if(critical) {
                        AttackPhase.CUT_IN
                    } else {
                        AttackPhase.ATTACK
                    }
                }
            }
            AttackPhase.CUT_IN -> {
                val characterSplash: Bitmap
                val nextPhase: AttackPhase
                if(isSupportRound) {
                    characterSplash = battleModel.supportCharacter!!.splashBitmap
                    nextPhase = AttackPhase.JUMP_BACK
                }
                else {
                    characterSplash = attackingCharacter.battleSprites.splashBitmap
                    nextPhase = AttackPhase.ATTACK
                }
                CutIn(cutIn = characterSplash) {
                    phase = nextPhase
                }
            }
            AttackPhase.ATTACK -> {
                Attack(
                    characterIdleImg = attackingCharacter.battleSprites.idleBitmap,
                    characterAttackImg = attackingCharacter.battleSprites.attackBitmap,
                    attack = if(critical || isSupportRound) attackingCharacter.battleSprites.strongProjectileBitmap else attackingCharacter.battleSprites.projectileBitmap,
                    supportIdleImg = if(isSupportRound) battleModel.supportCharacter?.idleBitmap else null,
                    supportAttackImg = if(isSupportRound) battleModel.supportCharacter?.attackBitmap else null,
                    supportAttack = if(isSupportRound) battleModel.supportCharacter?.strongProjectileBitmap else null,
                    direction = direction) {
                    phase = AttackPhase.OPPONENT_RECEIVES_ATTACK
                }
            }
            AttackPhase.OPPONENT_RECEIVES_ATTACK -> {
                OpponentReceivesAttack(
                    characterIdleSprite = defendingCharacter.battleSprites.idleBitmap,
                    characterDodgeSprite = defendingCharacter.battleSprites.dodgeBitmap,
                    supportIdleSprite = if(wasSupported) battleModel.supportCharacter?.idleBitmap else null,
                    attackSprite = if(critical || isSupportRound) attackingCharacter.battleSprites.strongProjectileBitmap else attackingCharacter.battleSprites.projectileBitmap,
                    supportAttack = if(isSupportRound) battleModel.supportCharacter?.strongProjectileBitmap else null,
                    hitSprites = defendingCharacter.battleSprites.hits,
                    wasHit = attackHit,
                    direction = direction*-1) {
                    phase = AttackPhase.OPPONENT_HP
                }
            }
            AttackPhase.OPPONENT_HP -> {
                val oldHpSprite = if(round == 0) fullHpSprite(battleModel, !isPartnerAttacking) else defenderHpRemainingSprite(battleModel, isPartnerAttacking, round-1)
                val newHpSprite = defenderHpRemainingSprite(battleModel, isPartnerAttacking, round)
                OpponentHp(characterSprite = defendingCharacter.battleSprites.idleBitmap,
                    oldHpSprite = oldHpSprite,
                    newHpSprite = newHpSprite,
                    wasHit = attackHit,
                    direction = direction*-1) {
                    finisher.invoke()
                }
            }
        }
    }

    private fun attackerHpRemainingSprite(battleModel: PostBattleModel, isPartnerAttacking: Boolean, round: Int): Bitmap {
        if(isPartnerAttacking) {
            return if(round == 0) battleModel.partnerRemainingHpSprites[6] else battleModel.partnerHpSprite(round-1)
        } else {
            return battleModel.opponentHpSprite(round)
        }
    }

    private fun defenderHpRemainingSprite(battleModel: PostBattleModel, isPartnerAttacking: Boolean, round: Int): Bitmap {
        if(isPartnerAttacking) {
            return battleModel.opponentHpSprite(round)
        } else {
            return battleModel.partnerHpSprite(round)
        }
    }

    private fun fullHpSprite(battleModel: PostBattleModel, isPartner: Boolean): Bitmap {
        if(isPartner) {
            return battleModel.partnerRemainingHpSprites[6]
        } else {
            return battleModel.opponentRemainingHpSprites[6]
        }
    }

    @Composable
    fun ShowHP(characterImg: Bitmap, hpRemainingSprite: Bitmap, direction: Float, onFinish: () -> Unit) {
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
            onFinish.invoke()
        }, 1000)
    }

    @Composable
    fun JumpBack(characterAttackImg: Bitmap, supportAttackImg: Bitmap?, direction: Float, critical: Boolean, onFinish: () -> Unit) {
        var verticalTarget by remember { mutableStateOf(0f)}
        var horizontalTarget by remember { mutableStateOf(0f) }
        val verticalOffset by animateFloatAsState(
            targetValue = verticalTarget,
            animationSpec = tween(durationMillis = 300, easing = LinearEasing),
            finishedListener = {final -> verticalTarget = 0f}, label = ""
        )
        val horizontalOffset by animateFloatAsState(
            targetValue = horizontalTarget,
            animationSpec = tween(durationMillis = 600, easing = LinearEasing), label = ""
        )
        LaunchedEffect(true) {
            verticalTarget = 1f
            horizontalTarget = 1f
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            if(supportAttackImg != null) {
                bitmapScaler.ScaledBitmap(
                    bitmap = supportAttackImg,
                    contentDescription = "Supporter",
                    alignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .offset(
                            y = backgroundHeight.times(PositionOffsetRatios.SUPPORT_CHARACTER_OFFSET_FROM_BOTTOM + (-.15f * verticalOffset)),
                            x = backgroundHeight.times(direction * (PositionOffsetRatios.CHARACTER_JUMP_BACK_POSITION * horizontalOffset))
                        )
                        .graphicsLayer(scaleX = direction)
                )
            }
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
            onFinish.invoke()
        }, 800)
    }

    @Composable
    fun CutIn(cutIn: Bitmap, onFinish: () -> Unit) {
        Handler(Looper.getMainLooper()!!).postDelayed({
            onFinish.invoke()
        }, 1000)
        bitmapScaler.ScaledBitmap(bitmap = cutIn, contentDescription = "Cut In")
    }

    @Composable
    fun Attack(characterIdleImg: Bitmap, characterAttackImg: Bitmap, attack: Bitmap, supportIdleImg: Bitmap?, supportAttackImg: Bitmap?, supportAttack: Bitmap?, direction: Float, onFinish: () -> Unit) {
        var idle by remember { mutableStateOf(true) }
        var supportIdle by remember { mutableStateOf(true) }
        var targetAttackOffset by remember { mutableStateOf(0f) }
        var supportTargetAttackOffset by remember { mutableStateOf(0f) }
        val attackSpeed = remember {ADDITIONAL_OFFSET_TO_LEAVE_SCREEN * ATTACK_TIME_PER_SCREEN_HEIGHT}
        val attackOffset by animateFloatAsState(
            targetValue = targetAttackOffset,
            animationSpec = tween(durationMillis = attackSpeed.toInt(), easing = LinearEasing),
            label = ""
        ) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                if(supportAttack != null) {
                    supportIdle = false
                    supportTargetAttackOffset = 1f
                } else {
                    onFinish.invoke()
                }
            }, 0)
        }
        val supportAttackOffset by animateFloatAsState(
            targetValue = supportTargetAttackOffset,
            animationSpec = tween(durationMillis = attackSpeed.toInt(), easing = LinearEasing),
            label = ""
        ) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                onFinish.invoke()
            }, 0)
        }
        Handler(Looper.getMainLooper()!!).postDelayed({
            idle = false
            targetAttackOffset = 1f
        }, 500)

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            if(supportIdleImg != null && supportAttackImg != null) {
                bitmapScaler.ScaledBitmap(
                    bitmap = if(supportIdle) supportIdleImg else supportAttackImg,
                    contentDescription = "Supporter",
                    alignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .offset(
                            y = backgroundHeight.times(PositionOffsetRatios.SUPPORT_CHARACTER_OFFSET_FROM_BOTTOM),
                            x = backgroundHeight.times(direction * PositionOffsetRatios.CHARACTER_JUMP_BACK_POSITION)
                        )
                        .graphicsLayer(scaleX = direction)
                )
            }
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
                            x = backgroundHeight.times(direction * (PositionOffsetRatios.ATTACK_OFFSET_FROM_CENTER + ADDITIONAL_OFFSET_TO_LEAVE_SCREEN * attackOffset))
                        )
                        .graphicsLayer(scaleX = direction)
                )
            }
            if(!supportIdle) {
                bitmapScaler.ScaledBitmap(
                    bitmap = supportAttack!!,
                    contentDescription = "support attack",
                    modifier = Modifier
                        .offset(
                            y = backgroundHeight.times(PositionOffsetRatios.SUPPORT_ATTACK_OFFSET_FROM_BOTTOM),
                            x = backgroundHeight.times(direction * (PositionOffsetRatios.ATTACK_OFFSET_FROM_CENTER + ADDITIONAL_OFFSET_TO_LEAVE_SCREEN * supportAttackOffset))
                        )
                        .graphicsLayer(scaleX = direction)
                )
            }
        }
    }

    @Composable
    fun OpponentReceivesAttack(characterIdleSprite: Bitmap, characterDodgeSprite: Bitmap, supportIdleSprite: Bitmap?, attackSprite: Bitmap, supportAttack: Bitmap?, hitSprites: List<Bitmap>, direction: Float, wasHit: Boolean, onFinish: () -> Unit) {
        val screenExitLocation = remember { ADDITIONAL_OFFSET_TO_LEAVE_SCREEN + PositionOffsetRatios.ATTACK_OFFSET_FROM_CENTER}
        var targetAttackOffset by remember { mutableStateOf(ADDITIONAL_OFFSET_TO_LEAVE_SCREEN + PositionOffsetRatios.ATTACK_OFFSET_FROM_CENTER) }
        var targetSupportAttackOffset by remember { mutableStateOf(ADDITIONAL_OFFSET_TO_LEAVE_SCREEN + PositionOffsetRatios.ATTACK_OFFSET_FROM_CENTER) }
        var attackSpeed by remember { mutableStateOf(0) }
        var attackHitting by remember { mutableStateOf(false) }
        var supportAttackLaunched by remember { mutableStateOf(false) }
        var dodging by remember { mutableStateOf(false) }
        var dodgingOffsetTarget by remember { mutableStateOf(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM) }
        val attackOffset by animateFloatAsState(
            targetValue = targetAttackOffset,
            animationSpec = tween(durationMillis = attackSpeed, easing = LinearEasing),
            finishedListener = { _ ->
                if(wasHit) {
                    attackHitting = true
                }
            }, label = "main attack"
        )
        val supportAttackOffset by animateFloatAsState(
            targetValue = targetSupportAttackOffset,
            animationSpec = tween(durationMillis = attackSpeed, easing = LinearEasing),
            finishedListener = { _ ->
                if(wasHit) {
                    attackHitting = true
                }
            }, label = "support attack"
        )
        val dodgeOffset by animateFloatAsState(
            targetValue = dodgingOffsetTarget,
            animationSpec = tween(durationMillis = attackSpeed/2),
            finishedListener = {finished ->
                if(finished == PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM) {
                    dodging = false;
                    Handler(Looper.getMainLooper()!!).postDelayed({
                        onFinish.invoke()
                    }, 500)
                } else {
                    dodgingOffsetTarget = PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM
                }
            }, label = ""
        )
        LaunchedEffect(true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                if(wasHit) {
                    attackSpeed = (ATTACK_TIME_PER_SCREEN_HEIGHT * (screenExitLocation)).toInt()
                    targetAttackOffset = 0f
                } else {
                    attackSpeed = (ATTACK_TIME_PER_SCREEN_HEIGHT * (screenExitLocation)).toInt() * 2
                    targetAttackOffset = -1 * (ADDITIONAL_OFFSET_TO_LEAVE_SCREEN + PositionOffsetRatios.ATTACK_OFFSET_FROM_CENTER)
                    dodging = true
                    dodgingOffsetTarget = -0.2f
                }
            }, 500)
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            if(!attackHitting) {
                if(supportIdleSprite != null) {
                    bitmapScaler.ScaledBitmap(
                        bitmap = supportIdleSprite,
                        contentDescription = "Receiver Support",
                        alignment = Alignment.BottomCenter,
                        modifier = Modifier
                            .offset(
                                y = backgroundHeight.times(PositionOffsetRatios.SUPPORT_CHARACTER_OFFSET_FROM_BOTTOM),
                            )
                            .graphicsLayer(scaleX = direction)
                    )
                }
                if(wasHit) {
                    bitmapScaler.ScaledBitmap(
                        bitmap = if(dodging) characterDodgeSprite else characterIdleSprite,
                        contentDescription = "Attack Receiver",
                        alignment = Alignment.BottomCenter,
                        modifier = Modifier
                            .offset(
                                y = backgroundHeight.times(dodgeOffset),
                            )
                            .graphicsLayer(scaleX = direction)
                    )
                    if(!supportAttackLaunched) {
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
                            bitmap = supportAttack!!,
                            contentDescription = "support attack",
                            modifier = Modifier
                                .offset(
                                    y = backgroundHeight.times(PositionOffsetRatios.ATTACK_OFFSET_FROM_BOTTOM),
                                    x = backgroundHeight.times(direction * supportAttackOffset)
                                )
                                .graphicsLayer(scaleX = direction * -1.0f)
                        )
                    }
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
                    if(supportAttack == null) {
                        onFinish.invoke()
                    } else if (supportAttackLaunched) {
                        onFinish.invoke()
                    } else {
                        attackHitting = false
                        targetSupportAttackOffset = 0f
                        supportAttackLaunched = true
                    }
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