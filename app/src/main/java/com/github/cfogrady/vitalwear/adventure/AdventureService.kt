package com.github.cfogrady.vitalwear.adventure

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import com.github.cfogrady.vitalwear.adventure.data.CharacterAdventureDao
import com.github.cfogrady.vitalwear.adventure.data.CharacterAdventureEntity
import com.github.cfogrady.vitalwear.battle.data.BattleResult
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntityDao
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager
import com.github.cfogrady.vitalwear.steps.AccelerometerToStepSensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException

class AdventureService(
    private val gameStateFlow: MutableStateFlow<GameState>,
    private val cardMetaEntityDao: CardMetaEntityDao,
    private val characterManager: CharacterManager,
    private val adventureEntityDao: AdventureEntityDao,
    private val cardSpritesIO: CardSpritesIO,
    private val notificationChannelManager: NotificationChannelManager,
    private val characterAdventureDao: CharacterAdventureDao,
    private val sensorManager: SensorManager) {

    companion object {
        const val TAG = "AdventureService"
    }

    var activeAdventure: ActiveAdventure? = null
    var accelerometerToStepSensor: AccelerometerToStepSensor? = null

    fun startAdventure(context: Context, cardName: String, partnerId: Int, startingAdventure: Int): Job {
        val adventureService = this
        return CoroutineScope(Dispatchers.IO).launch {
            val adventures = getAdventureOptions(cardName)
            val backgrounds = cardSpritesIO.loadCardBackgrounds(context, cardName)
            val adventure = ActiveAdventure(context, adventureService, adventures, backgrounds, startingAdventure, partnerId)
            activeAdventure = adventure
            val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            if(stepCounter == null) {
                val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                accelerometerToStepSensor = AccelerometerToStepSensor(adventure)
                sensorManager.registerListener(accelerometerToStepSensor, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            } else {
                sensorManager.registerListener(activeAdventure, stepCounter, SensorManager.SENSOR_DELAY_GAME)
            }
            gameStateFlow.value = GameState.ADVENTURE
        }
    }

    fun stopAdventure(context: Context) {
        if(activeAdventure == null) {
            throw IllegalStateException("Can't stopAdventure if activeAdventure isn't present")
        }
        context.stopService(Intent(context, AdventureForegroundService::class.java))
        if(accelerometerToStepSensor != null) {
            sensorManager.unregisterListener(accelerometerToStepSensor)
            accelerometerToStepSensor = null
        } else {
            sensorManager.unregisterListener(activeAdventure)
        }
        gameStateFlow.value = GameState.IDLE
        activeAdventure = null
    }

    fun completeBattle(context: Context, battleResult: BattleResult) {
        val adventure = activeAdventure
        if(adventure?.zoneCompleted?.value != true) {
            Log.e(TAG, "Battle completed, but we haven't completed a zone...")
            return
        }
        if(battleResult == BattleResult.RETREAT) {
            stopAdventure(context)
            return
        }
        notificationChannelManager.cancelNotification(NotificationChannelManager.ADVENTURE_BOSS)
        if(battleResult == BattleResult.WIN) {
            // function so we don't lose references in coroutine scope during finishZone
            markCompletion(adventure.currentAdventureEntity(), adventure.partnerId)
        }
        adventure.finishZone(battleResult == BattleResult.WIN)
    }

    fun markCompletion(adventureEntity: AdventureEntity, partnerId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val cardMeta = cardMetaEntityDao.getByName(adventureEntity.cardName)
            val maxAdventureCompletedForCard = cardMeta.maxAdventureCompletion ?: -1
            if(maxAdventureCompletedForCard < adventureEntity.adventureId) {
                val updated = cardMeta.copy(maxAdventureCompletion = adventureEntity.adventureId)
                cardMetaEntityDao.update(updated)
                characterManager.maybeUpdateCardMeta(updated)
            }
            // set character card completion
            val highestCompleted = characterAdventureDao.getByCharacterIdAndCardName(adventureEntity.characterId, adventureEntity.cardName)?.adventureId ?: -1
            if(highestCompleted < adventureEntity.adventureId) {
                characterAdventureDao.upsert(CharacterAdventureEntity(adventureEntity.cardName, partnerId, adventureEntity.adventureId))
            }
        }
    }

    fun notifyZoneCompletion(context: Context) {
        notificationChannelManager.sendGenericNotification(context, "Adventure Boss!", "", NotificationChannelManager.ADVENTURE_BOSS)
    }

    suspend fun getAdventureOptions(cardName: String): List<AdventureEntity> {
        return withContext(Dispatchers.IO) {
            adventureEntityDao.getByCard(cardName)
        }
    }

    suspend fun getCurrentMaxAdventure(characterId: Int, cardName: String): Int {
        return withContext(Dispatchers.IO) {
            val highestCompletion = characterAdventureDao.getByCharacterIdAndCardName(characterId, cardName)
            if(highestCompletion == null) {
                0
            } else {
                highestCompletion.adventureId + 1
            }
        }
    }

}