package com.github.cfogrady.vitalwear.adventure

import android.content.Context
import android.content.Intent
import com.github.cfogrady.vitalwear.BackgroundManager
import com.github.cfogrady.vitalwear.adventure.data.CharacterAdventureDao
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntityDao
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager
import com.github.cfogrady.vitalwear.training.TrainingForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException

class AdventureService(
    private val adventureEntityDao: AdventureEntityDao,
    private val cardSpritesIO: CardSpritesIO,
    private val notificationChannelManager: NotificationChannelManager,
    private val characterAdventureDao: CharacterAdventureDao) {

    var activeAdventure: ActiveAdventure? = null

    fun startAdventure(context: Context, cardName: String, startingZone: Int = 0): Job {
        val adventureService = this
        return CoroutineScope(Dispatchers.IO).launch {
            val adventures = adventureEntityDao.getByCard(cardName)
            val backgrounds = cardSpritesIO.loadCardBackgrounds(context, cardName)
            val adventure = ActiveAdventure(adventureService, adventures, backgrounds, currentZone = startingZone)
            val foregroundIntent = Intent(context, AdventureForegroundService::class.java)
            context.startForegroundService(foregroundIntent)
            activeAdventure = adventure
        }
    }

    fun stopAdventure(context: Context) {
        if(activeAdventure == null) {
            throw IllegalStateException("Can't stopAdventure if activeAdventure isn't present")
        }
        // val adventure = activeAdventure!!
        activeAdventure = null
        context.stopService(Intent(context, AdventureForegroundService::class.java))
    }

    fun finishCurrentZone(context: Context) {
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