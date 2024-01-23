package com.github.cfogrady.vitalwear.character

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.github.cfogrady.vitalwear.adventure.data.CharacterAdventureDao
import com.github.cfogrady.vitalwear.character.data.*
import com.github.cfogrady.vitalwear.character.transformation.ExpectedTransformation
import com.github.cfogrady.vitalwear.character.transformation.TransformationOption
import com.github.cfogrady.vitalwear.character.transformation.history.TransformationHistoryDao
import com.github.cfogrady.vitalwear.character.transformation.history.TransformationHistoryEntity
import com.github.cfogrady.vitalwear.common.card.CardType
import com.github.cfogrady.vitalwear.complications.ComplicationRefreshService
import com.github.cfogrady.vitalwear.common.card.CharacterSpritesIO
import com.github.cfogrady.vitalwear.common.card.SpriteBitmapConverter
import com.github.cfogrady.vitalwear.common.card.db.AttributeFusionEntityDao
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntityDao
import com.github.cfogrady.vitalwear.common.card.db.SpecificFusionEntityDao
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
    private val attributeFusionEntityDao: AttributeFusionEntityDao,
    private val specificFusionEntityDao: SpecificFusionEntityDao,
) : CharacterManager {
    private val activeCharacterFlow = MutableStateFlow<VBCharacter?>(null)
    private lateinit var bemUpdater: VBUpdater
    override val initialized = MutableStateFlow(false)

    suspend fun init(applicationContext: Context, vbUpdater: VBUpdater) {
        Log.i(TAG, "Initializing character manager")
        this.bemUpdater = bemUpdater
        withContext(Dispatchers.IO) {
            val character = loadActiveCharacter(applicationContext)
            if(character != null) {
                withContext(Dispatchers.Main) {
                    activeCharacterFlow.value = character
                    vbUpdater.setupTransformationChecker(character)
                }
            }
            initialized.value = true
            Log.i(TAG, "Character manager initialized")
        }
    }

    override fun getCurrentCharacter(): VBCharacter? {
        return activeCharacterFlow.value
    }

    override fun getCharacterFlow() : StateFlow<VBCharacter?> {
        return activeCharacterFlow
    }

    private fun loadActiveCharacter(applicationContext: Context) : VBCharacter? {
        Log.i(TAG, "Loading active character")
        // replace this with a table for activePartner and fetch by character id
        val activeCharacterStats = characterDao.getCharactersByState(CharacterState.SYNCED)
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
            val idleSprite = characterSpritesIO.loadCharacterSpriteFile(applicationContext, transformationEntity.toCharDir, CharacterSpritesIO.IDLE1)!!
            val idleBitmap = spriteBitmapConverter.getBitmap(idleSprite)
            transformationOptions.add(
                TransformationOption(idleBitmap,
                    transformationEntity.toCharacterId,
                    transformationEntity.requiredVitals,
                    transformationEntity.requiredPp,
                    transformationEntity.requiredBattles,
                    transformationEntity.requiredWinRatio,
                    transformationEntity.minAdventureCompletionRequired,
                    transformationEntity.isSecret)
            )
        }
        return transformationOptions
    }

    override suspend fun fetchSupportCharacter(context: Context): SupportCharacter? {
        return withContext(Dispatchers.IO) {
            val supports = characterDao.getCharactersByState(CharacterState.SUPPORT)
            if(supports.isEmpty()) {
                null
            } else {
                if(supports.size > 1) {
                    Log.w(TAG, "Multiple support characters found!")
                }
                val support = supports[0]
                val card = cardMetaEntityDao.getByName(support.cardFile)
                val species = speciesEntityDao.getCharacterByCardAndCharacterId(support.cardFile, support.slotId)
                val idle1 = characterSpritesIO.loadCharacterBitmapFile(context, species.spriteDirName, CharacterSpritesIO.IDLE1)!!
                val idle2 = characterSpritesIO.loadCharacterBitmapFile(context, species.spriteDirName, CharacterSpritesIO.IDLE2)!!
                val attack = characterSpritesIO.loadCharacterBitmapFile(context, species.spriteDirName, CharacterSpritesIO.ATTACK) ?: idle2
                SupportCharacter(
                    support.cardFile,
                    card.cardId,
                    card.franchise,
                    support.slotId,
                    species.attribute,
                    species.phase,
                    species.bp + support.trainedBp,
                    species.ap + support.trainedAp,
                    species.hp + support.trainedHp,
                    species.criticalAttackId,
                    species.spriteDirName,
                    idle1,
                    idle2,
                    attack,
                    )
            }
        }
    }

    override fun doActiveCharacterTransformation(applicationContext: Context, transformationOption: ExpectedTransformation) : VBCharacter {
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
            currentCharacter.characterStats.state = CharacterState.STORED
            updateCharacter(currentCharacter.characterStats)
            bemUpdater.cancel()
        }
        val character = newCharacter(applicationContext, cardMetaEntity, 0)
        insertCharacter(character.characterStats, character.settings, character.speciesStats.phase)
        activeCharacterFlow.value = character
        bemUpdater.setupTransformationChecker(character)
        complicationRefreshService.refreshVitalsComplication()
    }

    private fun newCharacter(applicationContext: Context, cardMetaEntity: CardMetaEntity, slotId: Int) : VBCharacter {
        if(cardMetaEntity.cardType == CardType.BEM) {
            return buildBEMCharacter(applicationContext, cardMetaEntity, slotId, CharacterSettingsEntity.DEFAULT_SETTINGS) { transformationTime ->
                newCharacterEntityFromCard(cardMetaEntity.cardName, slotId, transformationTime)
            }
        } else {
            return buildDIMCharacter(applicationContext, cardMetaEntity, slotId, CharacterSettingsEntity.DEFAULT_SETTINGS) { transformationTime ->
                newCharacterEntityFromCard(cardMetaEntity.cardName, slotId, transformationTime)
            }
        }

    }

    private fun buildBEMCharacter(applicationContext: Context, cardMetaEntity: CardMetaEntity, slotId: Int, settings: CharacterSettingsEntity, characterEntitySupplier: (Long) -> CharacterEntity): BEMCharacter {
        val cardName = cardMetaEntity.cardName
        val transformationTime = largestTransformationTimeSeconds(cardMetaEntity.cardName, slotId)
        val characterEntity = characterEntitySupplier.invoke(transformationTime)
        val speciesEntity = speciesEntityDao.getCharacterByCardAndCharacterId(cardName, slotId)
        val bitmaps = characterSpritesIO.loadCharacterSprites(applicationContext, speciesEntity.spriteDirName)
        val transformationOptions = transformationOptions(applicationContext, cardName, slotId)
        val attributeFusionEntity = attributeFusionEntityDao.findByCardAndSpeciesId(cardName, slotId)
        val specificFusionOptions = specificFusionEntityDao.findByCardAndSpeciesId(cardName, slotId)
        return BEMCharacter(cardMetaEntity, bitmaps, characterEntity, speciesEntity, transformationTime, transformationOptions, attributeFusionEntity, specificFusionOptions, settings)
    }

    private fun buildDIMCharacter(applicationContext: Context, cardMetaEntity: CardMetaEntity, slotId: Int, settings: CharacterSettingsEntity, characterEntitySupplier: (Long) -> CharacterEntity): DIMCharacter {
        val cardName = cardMetaEntity.cardName
        val transformationTime = largestTransformationTimeSeconds(cardMetaEntity.cardName, slotId)
        val characterEntity = characterEntitySupplier.invoke(transformationTime)
        val speciesEntity = speciesEntityDao.getCharacterByCardAndCharacterId(cardName, slotId)
        val bitmaps = characterSpritesIO.loadCharacterSprites(applicationContext, speciesEntity.spriteDirName)
        val transformationOptions = transformationOptions(applicationContext, cardName, slotId)
        val attributeFusionEntity = attributeFusionEntityDao.findByCardAndSpeciesId(cardName, slotId)
        val specificFusionOptions = specificFusionEntityDao.findByCardAndSpeciesId(cardName, slotId)
        return DIMCharacter(cardMetaEntity, bitmaps, characterEntity, speciesEntity, transformationTime, transformationOptions, attributeFusionEntity, specificFusionOptions, settings)
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
                currentCharacter.characterStats.state =
                    if(selectedCharacterPreview.state == CharacterState.SUPPORT) CharacterState.SUPPORT
                    else CharacterState.STORED
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

    override suspend fun setToSupport(
        characterPreview: CharacterPreview
    ) {
        withContext(Dispatchers.IO) {
            val characters = characterDao.getCharactersByState(CharacterState.SUPPORT)
            if(characters.isNotEmpty()) {
                for (character in characters) {
                    character.state = CharacterState.STORED
                }
                characterDao.updateMany(characters)
            }
            val newSupportCharacter = characterDao.getCharacterById(characterPreview.characterId)
            newSupportCharacter.state = CharacterState.SUPPORT
            characterDao.update(newSupportCharacter)
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

    override fun maybeUpdateCardMeta(cardMetaEntity: CardMetaEntity) {
        val activeCharacter = activeCharacterFlow.value
        if(activeCharacter?.cardMetaEntity?.cardName == cardMetaEntity.cardName) {
            if(activeCharacter is BEMCharacter) {
                activeCharacterFlow.value = activeCharacter.copy(cardMetaEntity = cardMetaEntity)
            } else if(activeCharacter is DIMCharacter) {
                activeCharacterFlow.value = activeCharacter.copy(cardMetaEntity = cardMetaEntity)
            }
        }
    }

    override suspend fun getCharacterBitmap(context: Context, cardName: String, slotId: Int, sprite: String, backupSprite: String): Bitmap {
        return withContext(Dispatchers.IO) {
            val species = speciesEntityDao.getCharacterByCardAndCharacterId(cardName, slotId)
            characterSpritesIO.loadCharacterBitmapFile(context, species.spriteDirName, sprite) ?:
            characterSpritesIO.loadCharacterBitmapFile(context, species.spriteDirName, backupSprite)!!
        }
    }
}