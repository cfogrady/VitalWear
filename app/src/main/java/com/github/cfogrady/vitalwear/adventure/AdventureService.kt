package com.github.cfogrady.vitalwear.adventure

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import com.github.cfogrady.vitalwear.adventure.data.CharacterAdventureDao
import com.github.cfogrady.vitalwear.adventure.data.CharacterAdventureEntity
import com.github.cfogrady.vitalwear.battle.data.BattleResult
import com.github.cfogrady.vitalwear.card.CardMeta
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntityDao
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager
import com.github.cfogrady.vitalwear.steps.AccelerometerToStepSensor
import com.github.cfogrady.vitalwear.steps.StepSensorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.IllegalStateException

class AdventureService(
    private val gameStateFlow: MutableStateFlow<GameState>,
    private val cardMetaEntityDao: CardMetaEntityDao,
    private val characterManager: CharacterManager,
    private val adventureEntityDao: AdventureEntityDao,
    private val cardSpritesIO: CardSpritesIO,
    private val notificationChannelManager: NotificationChannelManager,
    private val characterAdventureDao: CharacterAdventureDao,
    private val stepService: StepSensorService,
    private val sensorManager: SensorManager) {

    var activeAdventure: ActiveAdventure? = null
    var accelerometerToStepSensor: AccelerometerToStepSensor? = null

    fun startAdventure(context: Context, cardName: String, startingAdventure: Int): Job {
        val adventureService = this
        return CoroutineScope(Dispatchers.IO).launch {
            val adventures = getAdventureOptions(cardName)
            val backgrounds = cardSpritesIO.loadCardBackgrounds(context, cardName)
            val partner = characterManager.getCurrentCharacter()!!
            val dailySteps: StateFlow<Int> =
            if(!stepService.stepSensorEnabled()) {
                val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                val accelerometerToSteps = AccelerometerToStepSensor()
                sensorManager.registerListener(accelerometerToSteps, accelerometer, SensorManager.SENSOR_DELAY_GAME)
                accelerometerToStepSensor = accelerometerToSteps
                accelerometerToSteps.currentStep
            } else {
                stepService.dailySteps
            }
            val adventure = ActiveAdventure(context, adventureService, adventures, backgrounds, startingAdventure, partner, dailySteps)
            activeAdventure = adventure
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
        }
        gameStateFlow.value = GameState.IDLE
        activeAdventure?.end()
        activeAdventure = null
    }

    fun completeBattle(context: Context, battleResult: BattleResult) {
        val adventure = activeAdventure
        if(adventure?.zoneCompleted?.value != true) {
            Timber.e("Battle completed, but we haven't completed a zone...")
            return
        }
        if(battleResult == BattleResult.RETREAT) {
            stopAdventure(context)
            return
        }
        notificationChannelManager.cancelNotification(NotificationChannelManager.ADVENTURE_BOSS)
        if(battleResult == BattleResult.WIN) {
            // function so we don't lose references in coroutine scope during finishZone
            markCompletion(adventure.currentAdventureEntity(), adventure.partner.characterStats.id, adventure.partner.getFranchise(), adventure.partner.settings.assumedFranchise != null)
        }
        adventure.finishZone(battleResult == BattleResult.WIN)
    }

    private fun markCompletion(adventureEntity: AdventureEntity, partnerId: Int, partnerFranchise: Int, assumedFranchise: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val cardMeta = cardMetaEntityDao.getByName(adventureEntity.cardName)!!
            // only do a card adventure unlock if this was a character originally belonging to that franchise
            if(!assumedFranchise && partnerFranchise == cardMeta.franchise) {
                val maxAdventureCompletedForCard = cardMeta.maxAdventureCompletion ?: -1
                if(maxAdventureCompletedForCard < adventureEntity.adventureId) {
                    val updated = cardMeta.copy(maxAdventureCompletion = adventureEntity.adventureId)
                    cardMetaEntityDao.update(updated)
                    characterManager.maybeUpdateCardMeta(CardMeta.fromCardMetaEntity(updated))
                }
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

    suspend fun getMaxAdventureIdxByCardCompletedForCharacter(characterId: Int): Map<String, Int> {
        return withContext(Dispatchers.IO) {
            val adventures = characterAdventureDao.getByCharacterId(characterId)
            val map = mutableMapOf<String, Int>()
            for(adventure in adventures) {
                map.put(adventure.cardName, adventure.adventureId)
            }
            map
        }

    }

    // Add adventures a character had off device (typically adding the values of a returning character back)
    suspend fun addCharacterAdventures(characterId: Int, adventureIdxByCard: Map<String, Int>) {
        val characterAdventureEntities = mutableListOf<CharacterAdventureEntity>()
        for(cardNameAndMaxAdventure in adventureIdxByCard) {
            characterAdventureEntities.add(CharacterAdventureEntity(cardNameAndMaxAdventure.key, characterId, cardNameAndMaxAdventure.value))
        }
        characterAdventureDao.insert(characterAdventureEntities)
    }



}