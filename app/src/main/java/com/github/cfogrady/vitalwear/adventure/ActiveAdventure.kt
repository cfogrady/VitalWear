package com.github.cfogrady.vitalwear.adventure

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import com.github.cfogrady.vitalwear.battle.BattleCharacterInfo
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ActiveAdventure(private val context: Context, private val service: AdventureService, private val adventures: List<AdventureEntity>, private val backgrounds: List<Bitmap>, private var currentZone: Int, val partner: VBCharacter, val dailySteps: StateFlow<Int>) {

    private var startingStep: Int = dailySteps.value
    private val internalZoneCompleted = MutableStateFlow(false)
    val zoneCompleted: StateFlow<Boolean> = internalZoneCompleted

    val internalGoal = MutableStateFlow(getCurrentGoal())
    val goal: StateFlow<Int> = internalGoal

    val internalBackground = MutableStateFlow(currentBackground())
    val currentBackground: StateFlow<Bitmap> = internalBackground
    val job: Job

    init {
        Timber.i("Creating ActionAdventure")
        job = CoroutineScope(Dispatchers.Default).launch {
            dailySteps.collect{
                Timber.i("Step emitted: $it")
                checkSteps()
            }
        }
    }

    private fun getCurrentGoal(): Int {
        return adventures[currentZone].steps
    }

    fun end() {
        job.cancel()
    }

    fun stepsTowardsGoal(currentStepValue: Int = dailySteps.value): Int {
        return currentStepValue - startingStep
    }

    private fun currentBackground(): Bitmap {
        return backgrounds[adventures[currentZone].walkingBackgroundId]
    }

    fun currentAdventureEntity(): AdventureEntity {
        return adventures[currentZone]
    }

    private fun checkSteps() {
        if(dailySteps.value - startingStep >= adventures[currentZone].steps && !internalZoneCompleted.value) {
            internalZoneCompleted.value = true
            service.notifyZoneCompletion(context)
        }
    }

    fun finishZone(moveToNext: Boolean) {
        val goalSteps = adventures[currentZone].steps
        startingStep = startingStep!! + goalSteps
        if(moveToNext) {
            currentZone = (currentZone + 1) % adventures.size
            internalGoal.value = getCurrentGoal()
            internalBackground.value = currentBackground()
        }
        internalZoneCompleted.value = false
        Handler.createAsync(Looper.getMainLooper()).postDelayed(this::checkSteps, 500)
    }
}

fun AdventureEntity.toBattleCharacterInfo(): BattleCharacterInfo {
    var builder = BattleCharacterInfo.newBuilder()
        .setCardName(this.cardName)
        .setCharacterId(this.characterId)
        .setBackground(this.bossBackgroundId)
    this.bp?.let {
        builder = builder.setBp(it)
    }
    this.ap?.let {
        builder = builder.setAp(it)
    }
    this.hp?.let {
        builder = builder.setHp(it)
    }
    this.attackId?.let {
        builder = builder.setAttack(it)
    }
    this.criticalAttackId?.let {
        builder = builder.setCritical(it)
    }
    return builder.build()
}