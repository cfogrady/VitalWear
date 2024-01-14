package com.github.cfogrady.vitalwear.adventure

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import com.github.cfogrady.vitalwear.adventure.data.CharacterAdventureDao
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntityDao
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException

class AdventureService(
    private val gameStateFlow: MutableStateFlow<GameState>,
    private val adventureEntityDao: AdventureEntityDao,
    private val cardSpritesIO: CardSpritesIO,
    private val notificationChannelManager: NotificationChannelManager,
    private val characterAdventureDao: CharacterAdventureDao,
    private val sensorManager: SensorManager) {

    var activeAdventure: ActiveAdventure? = null

    fun startAdventure(context: Context, cardName: String, startingAdventure: Int): Job {
        val adventureService = this
        return CoroutineScope(Dispatchers.IO).launch {
            val adventures = getAdventureOptions(cardName)
            val backgrounds = cardSpritesIO.loadCardBackgrounds(context, cardName)
            val adventure = ActiveAdventure(context, adventureService, adventures, backgrounds, startingAdventure)
            activeAdventure = adventure
            val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            sensorManager.registerListener(activeAdventure, stepCounter, SensorManager.SENSOR_DELAY_GAME)
            gameStateFlow.value = GameState.ADVENTURE
        }
    }

    fun stopAdventure(context: Context) {
        if(activeAdventure == null) {
            throw IllegalStateException("Can't stopAdventure if activeAdventure isn't present")
        }
        context.stopService(Intent(context, AdventureForegroundService::class.java))
        sensorManager.unregisterListener(activeAdventure)
        gameStateFlow.value = GameState.IDLE
        activeAdventure = null
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
            highestCompletion?.let {
                it.adventureId + 1
            }
            0
        }
    }

}