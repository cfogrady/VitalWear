package com.github.cfogrady.vitalwear.character

import android.content.Context
import android.util.Log
import com.github.cfogrady.vitalwear.adventure.data.CharacterAdventureDao
import com.github.cfogrady.vitalwear.character.data.*
import com.github.cfogrady.vitalwear.character.transformation.history.TransformationHistoryDao
import com.github.cfogrady.vitalwear.character.transformation.history.TransformationHistoryEntity
import com.github.cfogrady.vitalwear.complications.ComplicationRefreshService
import com.github.cfogrady.vitalwear.common.card.CharacterSpritesIO
import com.github.cfogrady.vitalwear.common.card.SpriteBitmapConverter
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntityDao
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntityDao
import com.github.cfogrady.vitalwear.common.card.db.TransformationEntityDao
import com.github.cfogrady.vitalwear.settings.CharacterSettingsDao
import com.github.cfogrady.vitalwear.settings.CharacterSettingsEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val characterSpritesIO: CharacterSpritesIO,
    private val speciesEntityDao: SpeciesEntityDao,
    private val cardMetaEntityDao: CardMetaEntityDao,
    private val transformationEntityDao: TransformationEntityDao,
    private val spriteBitmapConverter: SpriteBitmapConverter,
    private val characterSettingsDao: CharacterSettingsDao,
    private val characterAdventureDao: CharacterAdventureDao,
    private val transformationHistoryDao: TransformationHistoryDao,
) : CharacterManager {
    private val activeCharacterFlow = MutableStateFlow(BEMCharacter.DEFAULT_CHARACTER)
    private lateinit var bemUpdater: BEMUpdater
    override val initialized = MutableStateFlow(false)

    suspend fun init(applicationContext: Context, bemUpdater: BEMUpdater) {
        Log.i(TAG, "Initializing character manager")
        this.bemUpdater = bemUpdater
        withContext(Dispatchers.IO) {
            val character = loadActiveCharacter(applicationContext)
            if(character != null) {
                withContext(Dispatchers.Main) {
                    activeCharacterFlow.value = character
                    bemUpdater.setupTransformationChecker(character)
                }
            }
            initialized.value = true
            Log.i(TAG, "Character manager initialized")
        }
    }

    override fun getCurrentCharacter(): BEMCharacter? {
        return activeCharacterFlow.value
    }

    override fun getCharacterFlow() : StateFlow<BEMCharacter?> {
        return activeCharacterFlow
    }

    private fun loadActiveCharacter(applicationContext: Context) : BEMCharacter? {
        Log.i(TAG, "Loading active character")
        // replace this with a table for activePartner and fetch by character id
        val activeCharacterStats = characterDao.getCharactersByNotState(CharacterState.BACKUP)
        if(activeCharacterStats.isNotEmpty()) {
            val characterStats = activeCharacterStats[0]
            val settings = characterSettingsDao.getByCharacterId(characterStats.id)
            return try {
                val cardMetaEntity = cardMetaEntityDao.getByName(characterStats.cardFile)
                buildBEMCharacter(applicationContext, cardMetaEntity, characterStats.slotId, settings) {
                    characterStats
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unable to load character! Act as if empty", e)
                BEMCharacter.DEFAULT_CHARACTER
            }
        }
        Log.i(TAG, "No character to load")
        return BEMCharacter.DEFAULT_CHARACTER
    }

    private fun largestTransformationTimeSeconds(cardName: String, slotId: Int) : Long {
        var transformationTimeInSeconds = 0L
        val transformationEntities = transformationEntityDao.getByCardAndFromCharacterId(cardName, slotId)
        for(transformationEntry in transformationEntities) {
            transformationTimeInSeconds = max(transformationTimeInSeconds, transformationEntry.timeToTransformationMinutes * 60L)
        }
        return transformationTimeInSeconds
    }

    private fun transformationOptions(applicationContext: Context, cardName: String, slotId: Int) : List<TransformationOption> {
        val transformationOptions = ArrayList<TransformationOption>()
        for(transformationEntity in transformationEntityDao.getByCardAndFromCharacterIdWithToCharDir(cardName, slotId)) {
            val idleSprite = characterSpritesIO.loadCharacterSpriteFile(applicationContext, transformationEntity.toCharDir, CharacterSpritesIO.IDLE1)
            val idleBitmap = spriteBitmapConverter.getBitmap(idleSprite)
            transformationOptions.add(
                TransformationOption(idleBitmap,
                    transformationEntity.toCharacterId,
                    transformationEntity.requiredVitals,
                    transformationEntity.requiredPp,
                    transformationEntity.requiredBattles,
                    transformationEntity.requiredWinRatio)
            )
        }
        return transformationOptions
    }

    override fun doActiveCharacterTransformation(applicationContext: Context, transformationOption: TransformationOption) : BEMCharacter {
        val actualCharacter = activeCharacterFlow.value!!
        val transformedCharacter = buildBEMCharacter(applicationContext, actualCharacter.cardMetaEntity, transformationOption.slotId, actualCharacter.settings) {
            actualCharacter.characterStats
        }
        transformedCharacter.characterStats.slotId = transformationOption.slotId
        transformedCharacter.characterStats.currentPhaseBattles = 0
        transformedCharacter.characterStats.currentPhaseWins = 0
        transformedCharacter.characterStats.hasTransformations = transformedCharacter.transformationOptions.isNotEmpty()
        transformedCharacter.characterStats.lastUpdate = LocalDateTime.now()
        transformedCharacter.characterStats.timeUntilNextTransformation = transformedCharacter.transformationWaitTimeSeconds
        transformedCharacter.characterStats.trainedPP = 0
        if(transformedCharacter.speciesStats.phase > 2 || actualCharacter.speciesStats.phase > 2) {
            transformedCharacter.characterStats.vitals = 0
        }
        updateCharacter(transformedCharacter.characterStats)
        transformationHistoryDao.upsert(TransformationHistoryEntity(transformedCharacter.characterStats.id, transformedCharacter.speciesStats.phase, transformedCharacter.cardName(), transformedCharacter.characterStats.slotId ))
        bemUpdater.cancel()
        activeCharacterFlow.value = transformedCharacter
        bemUpdater.setupTransformationChecker(transformedCharacter)
        return transformedCharacter
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
        val character = activeCharacterFlow.value
        if(character != null) {
            updateCharacterStats(character.characterStats, now)
        }
    }

    override fun createNewCharacter(applicationContext: Context, cardMetaEntity: CardMetaEntity) {
        val currentCharacter = activeCharacterFlow.value
        if(currentCharacter != null) {
            currentCharacter.characterStats.state = CharacterState.BACKUP
            updateCharacter(currentCharacter.characterStats)
            bemUpdater.cancel()
        }
        val character = newCharacter(applicationContext, cardMetaEntity, 0)
        insertCharacter(character.characterStats, character.settings, character.speciesStats.phase)
        activeCharacterFlow.value = character
        bemUpdater.setupTransformationChecker(character)
        complicationRefreshService.refreshVitalsComplication()
    }

    private fun newCharacter(applicationContext: Context, cardMetaEntity: CardMetaEntity, slotId: Int) : BEMCharacter {
        return buildBEMCharacter(applicationContext, cardMetaEntity, slotId, CharacterSettingsEntity.DEFAULT_SETTINGS) { transformationTime ->
            newCharacterEntityFromCard(cardMetaEntity.cardName, slotId, transformationTime)
        }
    }

    private fun buildBEMCharacter(applicationContext: Context, cardMetaEntity: CardMetaEntity, slotId: Int, settings: CharacterSettingsEntity, characterEntitySupplier: (Long) -> CharacterEntity): BEMCharacter {
        val cardName = cardMetaEntity.cardName
        val transformationTime = largestTransformationTimeSeconds(cardMetaEntity.cardName, slotId)
        val characterEntity = characterEntitySupplier.invoke(transformationTime)
        val speciesEntity = speciesEntityDao.getCharacterByCardAndCharacterId(cardName, slotId)
        val bitmaps = characterSpritesIO.loadCharacterSprites(applicationContext, speciesEntity.spriteDirName)
        val transformationOptions = transformationOptions(applicationContext, cardName, slotId)
        return BEMCharacter(cardMetaEntity, bitmaps, characterEntity, speciesEntity, transformationTime, transformationOptions, settings)
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

    private fun insertCharacter(character: CharacterEntity, settings: CharacterSettingsEntity, phase: Int) {
        character.id = characterDao.insert(character).toInt()
        settings.characterId = character.id
        characterSettingsDao.insert(settings)
        transformationHistoryDao.upsert(TransformationHistoryEntity(character.id, phase, character.cardFile, character.slotId))
    }

    override suspend fun swapToCharacter(applicationContext: Context, selectedCharacterPreview : CharacterPreview) {
        withContext(Dispatchers.IO) {
            val cardMetaEntity = cardMetaEntityDao.getByName(selectedCharacterPreview.cardName)
            val settings = characterSettingsDao.getByCharacterId(selectedCharacterPreview.characterId)
            val selectedCharacter = buildBEMCharacter(applicationContext, cardMetaEntity, selectedCharacterPreview.slotId, settings) {
                characterDao.getCharacterById(selectedCharacterPreview.characterId)
            }
            val currentCharacter = activeCharacterFlow.value
            if(currentCharacter != null) {
                currentCharacter.characterStats.state = CharacterState.BACKUP
                updateCharacter(currentCharacter.characterStats)
                bemUpdater.cancel()
            }
            selectedCharacter.characterStats.state = CharacterState.SYNCED
            updateCharacter(selectedCharacter.characterStats)
            withContext(Dispatchers.Main) {
                activeCharacterFlow.value = selectedCharacter
                complicationRefreshService.refreshVitalsComplication()
            }
            bemUpdater.setupTransformationChecker(selectedCharacter)
        }
    }

    override suspend fun updateSettings() {
        withContext(Dispatchers.IO) {
            val character = getCurrentCharacter()
            if(character != null) {
                characterSettingsDao.update(character.settings)
            }
        }
    }

    override fun deleteCharacter(characterPreview: CharacterPreview) {
        val currentCharacter = activeCharacterFlow.value
        if(currentCharacter == null) {
            characterSettingsDao.deleteById(characterPreview.characterId)
            characterAdventureDao.deleteByCharacterId(characterPreview.characterId)
            transformationHistoryDao.deleteByCharacterId(characterPreview.characterId)
            characterDao.deleteById(characterPreview.characterId)
        } else if(currentCharacter.characterStats.id == characterPreview.characterId) {
            Log.e(TAG, "Cannot delete active character")
        } else {
            characterSettingsDao.deleteById(characterPreview.characterId)
            characterAdventureDao.deleteByCharacterId(characterPreview.characterId)
            transformationHistoryDao.deleteByCharacterId(characterPreview.characterId)
            characterDao.deleteById(characterPreview.characterId)
        }
    }
}