package com.github.cfogrady.vitalwear.character

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.cfogrady.vb.dim.card.Card
import com.github.cfogrady.vb.dim.transformation.BemTransformationRequirements
import com.github.cfogrady.vb.dim.transformation.DimEvolutionRequirements
import com.github.cfogrady.vitalwear.character.data.*
import com.github.cfogrady.vitalwear.complications.ComplicationRefreshService
import com.github.cfogrady.vitalwear.data.CardLoader
import kotlinx.coroutines.*
import java.io.File
import java.time.LocalDateTime
import kotlin.collections.ArrayList
import kotlin.math.max

const val TAG = "CharacterRepository"

/**
 * Manage the character loading and updating
 */
class CharacterManagerImpl(
    private val complicationRefreshService: ComplicationRefreshService,
    private val characterDao: CharacterDao,
    private val cardLoader: CardLoader,
) : CharacterManager {
    private val activeCharacter = MutableLiveData(BEMCharacter.DEFAULT_CHARACTER)
    private lateinit var bemUpdater: BEMUpdater
    override val initialized = MutableLiveData(false)

    suspend fun init(bemUpdater: BEMUpdater) {
        this.bemUpdater = bemUpdater
        withContext(Dispatchers.IO) {
            val character = loadActiveCharacter()
            if(character != BEMCharacter.DEFAULT_CHARACTER) {
                withContext(Dispatchers.Main) {
                    activeCharacter.value = character
                    bemUpdater.initializeBEMUpdates(character)
                    initialized.value = true
                }
            }
        }
    }

    override fun getCurrentCharacter(): BEMCharacter {
        return activeCharacter.value!!
    }

    override fun getLiveCharacter() : LiveData<BEMCharacter> {
        return activeCharacter
    }

    override fun activeCharacterIsPresent() : Boolean {
        return activeCharacter.value != null && activeCharacter.value != BEMCharacter.DEFAULT_CHARACTER
    }

    private fun loadActiveCharacter() : BEMCharacter {
        // replace this with a table for activePartner and fetch by character id
        val activeCharacterStats = characterDao.getCharactersByNotState(CharacterState.BACKUP)
        if(!activeCharacterStats.isEmpty()) {
            val characterStats = activeCharacterStats.get(0)
            return try {
                val card = cardLoader.loadCard(characterStats.cardFile)
                val bitmaps = cardLoader.bitmapsFromCard(card, characterStats.slotId)
                val timeToTransform = largestTransformationTimeSeconds(card, characterStats.slotId)
                val transformationOptions = transformationOptions(card, characterStats.slotId)
                val speciesStats = card.characterStats.characterEntries.get(characterStats.slotId)
                BEMCharacter(bitmaps, characterStats, speciesStats, timeToTransform, transformationOptions)
            } catch (e: Exception) {
                Log.e(TAG, "Unable to load card! Act as if empty", e)
                BEMCharacter.DEFAULT_CHARACTER
            }

        }
        return BEMCharacter.DEFAULT_CHARACTER
    }

    private fun largestTransformationTimeSeconds(card: Card<*, *, *, *, *, *>, slotId: Int) : Long {
        var transformationTimeInSeconds = 0L
        for(transformationEntry in card.transformationRequirements.transformationEntries) {
            if(transformationEntry.fromCharacterIndex != slotId) {
                continue
            }
            if(transformationEntry is DimEvolutionRequirements.DimEvolutionRequirementBlock) {
                transformationTimeInSeconds = max(transformationTimeInSeconds, transformationEntry.hoursUntilEvolution * 60L * 60L)
            } else if(transformationEntry is BemTransformationRequirements.BemTransformationRequirementEntry) {
                transformationTimeInSeconds = max(transformationTimeInSeconds, transformationEntry.minutesUntilTransformation * 60L)
            }
        }
        return transformationTimeInSeconds
    }

    private fun transformationOptions(card: Card<*, *, *, *, *, *>, slotId: Int) : List<TransformationOption> {
        val transformationOptions = ArrayList<TransformationOption>()
        for(transformationEntry in card.transformationRequirements.transformationEntries) {
            if(transformationEntry.fromCharacterIndex != slotId) {
                continue
            }
            val idle = cardLoader.bitmapFromCard(card, slotId, 1)
            transformationOptions.add(
                TransformationOption(idle,
                    transformationEntry.toCharacterIndex,
                    transformationEntry.requiredVitalValues,
                    transformationEntry.requiredTrophies,
                    transformationEntry.requiredBattles,
                    transformationEntry.requiredWinRatio)
            )
        }
        return transformationOptions
    }

    override fun doActiveCharacterTransformation() {
        val actualCharacter = activeCharacter.value!!
        actualCharacter.readyToTransform.ifPresent { option ->
            val card = cardLoader.loadCard(actualCharacter.characterStats.cardFile)
            val transformationTime = largestTransformationTimeSeconds(card, option.slotId)
            val transformationOptions = transformationOptions(card, option.slotId)
            val bitmaps = cardLoader.bitmapsFromCard(card, option.slotId)
            val newSpeciesStats = card.characterStats.characterEntries.get(option.slotId)
            actualCharacter.characterStats.slotId = option.slotId
            actualCharacter.characterStats.currentPhaseBattles = 0
            actualCharacter.characterStats.currentPhaseWins = 0
            actualCharacter.characterStats.hasTransformations = transformationOptions.isNotEmpty()
            actualCharacter.characterStats.lastUpdate = LocalDateTime.now()
            actualCharacter.characterStats.timeUntilNextTransformation = transformationTime
            actualCharacter.characterStats.trainedPP = 0
            actualCharacter.characterStats.vitals = 0
            updateCharacter(actualCharacter.characterStats)
            bemUpdater.cancel()
            val transformedCharacter = BEMCharacter(bitmaps, actualCharacter.characterStats, newSpeciesStats, transformationTime, transformationOptions)
            activeCharacter.postValue(transformedCharacter)
            bemUpdater.initializeBEMUpdates(transformedCharacter)
        }
    }

    private fun updateCharacter(character: CharacterEntity) {
        characterDao.update(character)
    }

    private fun updateCharacterStats(character: CharacterEntity, now: LocalDateTime) {
        character.updateTimeStamps(now)
        characterDao.update(character)
    }

    fun updateActiveCharacter(now: LocalDateTime) {
        Log.i(TAG, "Updating the active character")
        if(activeCharacterIsPresent()) {
            updateCharacterStats(activeCharacter.value!!.characterStats, now)
        }
    }

    override fun createNewCharacter(file: File) {
        val card = cardLoader.loadCard(file)
        if(activeCharacterIsPresent()) {
            val currentCharacter = activeCharacter.value!!
            currentCharacter.characterStats.state = CharacterState.BACKUP
            updateCharacter(currentCharacter.characterStats)
            bemUpdater.cancel()
        }
        val character = newCharacter(file.name, card, 0)
        insertCharacter(character.characterStats)
        //TODO: Remove LiveData and replace with StateFlow
        activeCharacter.postValue(character)
        bemUpdater.initializeBEMUpdates(character)
        complicationRefreshService.refreshVitalsComplication()
    }

    private fun newCharacter(file: String, card: Card<*, *, *, *, *, *>, slotId: Int) : BEMCharacter {
        val entry = card.characterStats.characterEntries.get(slotId)
        val transformationTime = largestTransformationTimeSeconds(card, slotId)
        val bitmaps = cardLoader.bitmapsFromCard(card, slotId)
        val transformationOptions = transformationOptions(card, slotId)
        return BEMCharacter(bitmaps, newCharacterEntityFromCard(file, slotId, transformationTime), entry, transformationTime, transformationOptions)
    }



    private val totalTrainingTime = 100L*60L*60L //100 hours * 60min/hr * 60sec/min = total seconds

    private fun newCharacterEntityFromCard(file: String, slotId: Int, transformationTime: Long) : CharacterEntity {
        return CharacterEntity(0,
            CharacterState.SYNCED,
            file,
            slotId,
            LocalDateTime.now(),
            0,
            totalTrainingTime,
            transformationTime > 0,
            transformationTime,
            0,
            0,
            0,
            0,
            false,
            0,
            0,
            0,
            0,
            0,
            0,
            50,
            false
        )
    }

    private fun insertCharacter(character: CharacterEntity) {
        character.id = characterDao.insert(character).toInt()
    }

    override suspend fun swapToCharacter(selectedCharacterPreview : CharacterPreview) {
        withContext(Dispatchers.IO) {
            val characterStats = characterDao.getCharacterById(selectedCharacterPreview.characterId)[0]
            val card = cardLoader.loadCard(selectedCharacterPreview.cardName)
            val speciesStats = card.characterStats.characterEntries.get(selectedCharacterPreview.slotId)
            val bitmaps = cardLoader.bitmapsFromCard(card, selectedCharacterPreview.slotId)
            val transformationTime = largestTransformationTimeSeconds(card, selectedCharacterPreview.slotId)
            val transformationOptions = transformationOptions(card, selectedCharacterPreview.slotId)
            val selectedCharacter = BEMCharacter(bitmaps, characterStats, speciesStats, transformationTime, transformationOptions)
            if(activeCharacterIsPresent()) {
                val currentCharacter = activeCharacter.value!!
                currentCharacter.characterStats.state = CharacterState.BACKUP
                updateCharacter(currentCharacter.characterStats)
                bemUpdater.cancel()
            }
            characterStats.state = CharacterState.SYNCED
            updateCharacter(characterStats)
            withContext(Dispatchers.Main) {
                activeCharacter.value = selectedCharacter
                complicationRefreshService.refreshVitalsComplication()
            }
            bemUpdater.initializeBEMUpdates(selectedCharacter)
        }
    }

    override fun deleteCharacter(characterPreview: CharacterPreview) {
        val character = getCurrentCharacter() //force a load of the active character
        if(character.characterStats.id == characterPreview.characterId) {
            Log.e(TAG, "Cannot delete active character")
        } else {
            characterDao.deleteById(characterPreview.characterId)
        }
    }
}