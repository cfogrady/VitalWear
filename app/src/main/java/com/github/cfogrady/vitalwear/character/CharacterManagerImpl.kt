package com.github.cfogrady.vitalwear.character

import android.content.Context
import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.adventure.data.CharacterAdventureDao
import com.github.cfogrady.vitalwear.card.CardMeta
import com.github.cfogrady.vitalwear.card.DimToBemStatConversion
import com.github.cfogrady.vitalwear.character.data.*
import com.github.cfogrady.vitalwear.character.transformation.ExpectedTransformation
import com.github.cfogrady.vitalwear.character.transformation.TransformationOption
import com.github.cfogrady.vitalwear.character.transformation.history.TransformationHistoryDao
import com.github.cfogrady.vitalwear.character.transformation.history.TransformationHistoryEntity
import com.github.cfogrady.vitalwear.common.card.CardType
import com.github.cfogrady.vitalwear.complications.ComplicationRefreshService
import com.github.cfogrady.vitalwear.common.card.CharacterSpritesIO
import com.github.cfogrady.vitalwear.common.card.db.AttributeFusionEntityDao
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntityDao
import com.github.cfogrady.vitalwear.common.card.db.SpecificFusionEntityDao
import com.github.cfogrady.vitalwear.common.card.db.TransformationEntityDao
import com.github.cfogrady.vitalwear.settings.CharacterSettings
import com.github.cfogrady.vitalwear.settings.CharacterSettingsDao
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.time.LocalDateTime
import kotlin.collections.ArrayList
import kotlin.math.max

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
    private val characterSettingsDao: CharacterSettingsDao,
    private val characterAdventureDao: CharacterAdventureDao,
    private val transformationHistoryDao: TransformationHistoryDao,
    private val attributeFusionEntityDao: AttributeFusionEntityDao,
    private val specificFusionEntityDao: SpecificFusionEntityDao,
    private val dimToBemStatConversion: DimToBemStatConversion,
) : CharacterManager {
    private val activeCharacterFlow = MutableStateFlow<VBCharacter?>(null)
    private lateinit var vbUpdater: VBUpdater
    override val initialized = MutableStateFlow(false)

    suspend fun init(applicationContext: Context, vbUpdater: VBUpdater) {
        Timber.i("Initializing character manager")
        this.vbUpdater = vbUpdater
        withContext(Dispatchers.IO) {
            val character = loadActiveCharacter(applicationContext)
            if(character != null) {
                withContext(Dispatchers.Main) {
                    activeCharacterFlow.value = character
                    vbUpdater.setupTransformationChecker(character)
                }
            }
            initialized.value = true
            Timber.i("Character manager initialized")
        }
    }

    override fun getCurrentCharacter(): VBCharacter? {
        return activeCharacterFlow.value
    }

    override fun getCharacterFlow() : StateFlow<VBCharacter?> {
        return activeCharacterFlow
    }

    private suspend fun loadActiveCharacter(applicationContext: Context) : VBCharacter? {
        Timber.i("Loading active character")
        // replace this with a table for activePartner and fetch by character id
        val activeCharacterStats = characterDao.getCharactersByState(CharacterState.ACTIVE)
        if(activeCharacterStats.isNotEmpty()) {
            val characterStats = activeCharacterStats[0]
            val settings = CharacterSettings.fromCharacterSettingsEntity(characterSettingsDao.getByCharacterId(characterStats.id))
            return try {
                val cardMeta = CardMeta.fromCardMetaEntity(cardMetaEntityDao.getByName(characterStats.cardFile)!!)
                if(cardMeta.cardType == CardType.BEM) {
                    buildBEMCharacter(applicationContext, cardMeta, characterStats.slotId, settings) {
                        characterStats
                    }
                } else if (settings.assumedFranchise != null) {
                    buildDIMToBEMCharacter(applicationContext, cardMeta, characterStats.slotId, settings) {
                        characterStats
                    }
                } else {
                    buildDIMCharacter(applicationContext, cardMeta, characterStats.slotId, settings) {
                        characterStats
                    }
                }
            } catch (e: Exception) {
                Timber.e("Unable to load character! Act as if empty", e)
                null
            }
        }
        Timber.i("No character to load")
        return null
    }

    override suspend fun largestTransformationTimeSeconds(cardName: String, slotId: Int) : Long {
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
            val idleBitmap = characterSpritesIO.loadCharacterBitmapFile(applicationContext, transformationEntity.toCharDir, CharacterSpritesIO.IDLE1, resize = true)!!
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
                    Timber.w("Multiple support characters found!")
                }
                val support = supports[0]
                val card = cardMetaEntityDao.getByName(support.cardFile)!!
                val characterSettings = characterSettingsDao.getByCharacterId(support.id)
                var species = speciesEntityDao.getCharacterByCardAndCharacterId(support.cardFile, support.slotId)
                if(characterSettings.assumedFranchise != null && card.cardType == CardType.DIM) {
                    species = dimToBemStatConversion.convertSpeciesEntity(species)
                }
                val idle1 = characterSpritesIO.loadCharacterBitmapFile(context, species.spriteDirName, CharacterSpritesIO.IDLE1)!!
                val idle2 = characterSpritesIO.loadCharacterBitmapFile(context, species.spriteDirName, CharacterSpritesIO.IDLE2)!!
                val attack = characterSpritesIO.loadCharacterBitmapFile(context, species.spriteDirName, CharacterSpritesIO.ATTACK) ?: idle2
                SupportCharacter(
                    support.cardFile,
                    card.cardId,
                    characterSettings.assumedFranchise ?: card.franchise,
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

    override suspend fun doActiveCharacterTransformation(applicationContext: Context, transformationOption: ExpectedTransformation) : VBCharacter {
        val actualCharacter = activeCharacterFlow.value!!
        val transformedCharacter = if(actualCharacter is BEMCharacter)
            buildBEMCharacter(applicationContext, actualCharacter.cardMeta, transformationOption.slotId, actualCharacter.settings) {
                actualCharacter.characterStats
            }
        else if (actualCharacter is DIMToBEMCharacter) {
            buildDIMToBEMCharacter(applicationContext, actualCharacter.cardMeta, transformationOption.slotId, actualCharacter.settings) {
                actualCharacter.characterStats
            }
        }
        else buildDIMCharacter(applicationContext, actualCharacter.cardMeta, transformationOption.slotId, actualCharacter.settings) {
                actualCharacter.characterStats
            }
        transformedCharacter.characterStats.slotId = transformationOption.slotId
        transformedCharacter.characterStats.currentPhaseBattles = 0
        transformedCharacter.characterStats.currentPhaseWins = 0
        transformedCharacter.characterStats.hasTransformations = transformedCharacter.transformationOptions.isNotEmpty()
        transformedCharacter.characterStats.updateTimeStamps(LocalDateTime.now())
        transformedCharacter.characterStats.timeUntilNextTransformation = transformedCharacter.transformationWaitTimeSeconds
        transformedCharacter.characterStats.trainedPP = 0
        if(transformedCharacter.speciesStats.phase > 2 || actualCharacter.speciesStats.phase > 2) {
            transformedCharacter.characterStats.vitals = 0
        }
        updateCharacter(transformedCharacter.characterStats)
        transformationHistoryDao.upsert(TransformationHistoryEntity(transformedCharacter.characterStats.id, transformedCharacter.speciesStats.phase, transformedCharacter.cardName(), transformedCharacter.characterStats.slotId ))
        vbUpdater.cancel()
        activeCharacterFlow.value = transformedCharacter
        vbUpdater.setupTransformationChecker(transformedCharacter)
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
        Timber.i("Updating the active character")
        val character = activeCharacterFlow.value
        if(character != null) {
            updateCharacterStats(character.characterStats, now)
        }
    }

    override suspend fun createNewCharacter(applicationContext: Context, cardMeta: CardMeta, slotId: Int, characterSettings: CharacterSettings) {
        val currentCharacter = activeCharacterFlow.value
        if(currentCharacter != null) {
            currentCharacter.characterStats.state = CharacterState.STORED
            updateCharacter(currentCharacter.characterStats)
            vbUpdater.cancel()
        }
        val character = newCharacter(applicationContext, cardMeta, characterSettings, slotId)
        insertCharacter(character.characterStats, character.settings, character.speciesStats.phase)
        activeCharacterFlow.value = character
        vbUpdater.setupTransformationChecker(character)
        complicationRefreshService.refreshVitalsComplication()
    }

    private suspend fun newCharacter(applicationContext: Context, cardMeta: CardMeta, characterSettings: CharacterSettings, slotId: Int) : VBCharacter {
        return if(cardMeta.cardType == CardType.BEM) {
            buildBEMCharacter(applicationContext, cardMeta, slotId, characterSettings) { transformationTime ->
                newCharacterEntityFromCard(cardMeta.cardName, slotId, transformationTime)
            }
        } else if(characterSettings.assumedFranchise != null) {
            buildDIMToBEMCharacter(applicationContext, cardMeta, slotId, characterSettings) { transformationTime ->
                newCharacterEntityFromCard(cardMeta.cardName, slotId, transformationTime)
            }
        } else {
            buildDIMCharacter(applicationContext, cardMeta, slotId, characterSettings) { transformationTime ->
                newCharacterEntityFromCard(cardMeta.cardName, slotId, transformationTime)
            }
        }

    }

    private suspend fun buildBEMCharacter(applicationContext: Context, cardMeta: CardMeta, slotId: Int, settings: CharacterSettings, characterEntitySupplier: (Long) -> CharacterEntity): BEMCharacter {
        val cardName = cardMeta.cardName
        val transformationTime = largestTransformationTimeSeconds(cardName, slotId)
        val characterEntity = characterEntitySupplier.invoke(transformationTime)
        val speciesEntity = speciesEntityDao.getCharacterByCardAndCharacterId(cardName, slotId)
        val bitmaps = characterSpritesIO.loadCharacterSprites(applicationContext, speciesEntity.spriteDirName)
        val transformationOptions = transformationOptions(applicationContext, cardName, slotId)
        val attributeFusionEntity = attributeFusionEntityDao.findByCardAndSpeciesId(cardName, slotId)
        val specificFusionOptions = specificFusionEntityDao.findByCardAndSpeciesId(cardName, slotId)
        return BEMCharacter(cardMeta, bitmaps, characterEntity, speciesEntity, transformationTime, transformationOptions, attributeFusionEntity, specificFusionOptions, settings)
    }

    private suspend fun buildDIMCharacter(applicationContext: Context, cardMeta: CardMeta, slotId: Int, settings: CharacterSettings, characterEntitySupplier: (Long) -> CharacterEntity): DIMCharacter {
        val cardName = cardMeta.cardName
        val transformationTime = largestTransformationTimeSeconds(cardName, slotId)
        val characterEntity = characterEntitySupplier.invoke(transformationTime)
        val speciesEntity = speciesEntityDao.getCharacterByCardAndCharacterId(cardName, slotId)
        val bitmaps = characterSpritesIO.loadCharacterSprites(applicationContext, speciesEntity.spriteDirName)
        val transformationOptions = transformationOptions(applicationContext, cardName, slotId)
        val attributeFusionEntity = attributeFusionEntityDao.findByCardAndSpeciesId(cardName, slotId)
        val specificFusionOptions = specificFusionEntityDao.findByCardAndSpeciesId(cardName, slotId)
        return DIMCharacter(cardMeta, bitmaps, characterEntity, speciesEntity, transformationTime, transformationOptions, attributeFusionEntity, specificFusionOptions, settings)
    }

    private suspend fun buildDIMToBEMCharacter(applicationContext: Context, cardMeta: CardMeta, slotId: Int, settings: CharacterSettings, characterEntitySupplier: (Long) -> CharacterEntity): DIMToBEMCharacter {
        val cardName = cardMeta.cardName
        val transformationTime = largestTransformationTimeSeconds(cardName, slotId)
        val characterEntity = characterEntitySupplier.invoke(transformationTime)
        val speciesEntity = dimToBemStatConversion.convertSpeciesEntity(speciesEntityDao.getCharacterByCardAndCharacterId(cardName, slotId))
        val bitmaps = characterSpritesIO.loadCharacterSprites(applicationContext, speciesEntity.spriteDirName)
        val transformationOptions = transformationOptions(applicationContext, cardName, slotId)
        val attributeFusionEntity = attributeFusionEntityDao.findByCardAndSpeciesId(cardName, slotId)
        val specificFusionOptions = specificFusionEntityDao.findByCardAndSpeciesId(cardName, slotId)
        return DIMToBEMCharacter(cardMeta, bitmaps, characterEntity, speciesEntity, transformationTime, transformationOptions, attributeFusionEntity, specificFusionOptions, settings)
    }

    private val totalTrainingTime = 100L*60L*60L //100 hours * 60min/hr * 60sec/min = total seconds

    private fun newCharacterEntityFromCard(file: String, slotId: Int, transformationTime: Long) : CharacterEntity {
        return CharacterEntity(0,
            CharacterState.ACTIVE,
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
            false,
            false
        )
    }

    private fun insertCharacter(character: CharacterEntity, settings: CharacterSettings, phase: Int) {
        character.id = characterDao.insert(character).toInt()
        val settingsToSave = settings.copy(characterId = character.id).toCharacterSettingsEntity()
        characterSettingsDao.insert(settingsToSave)
        transformationHistoryDao.upsert(TransformationHistoryEntity(character.id, phase, character.cardFile, character.slotId))
    }

    override suspend fun swapToCharacter(applicationContext: Context, swapCharacterIdentifier : CharacterManager.SwapCharacterIdentifier) {
        withContext(Dispatchers.IO) {
            val cardMeta = CardMeta.fromCardMetaEntity(cardMetaEntityDao.getByName(swapCharacterIdentifier.cardName)!!)
            val settings = CharacterSettings.fromCharacterSettingsEntity(characterSettingsDao.getByCharacterId(swapCharacterIdentifier.characterId))
            val selectedCharacter = if(cardMeta.cardType == CardType.BEM)
                buildBEMCharacter(applicationContext, cardMeta, swapCharacterIdentifier.slotId, settings) {
                    characterDao.getCharacterById(swapCharacterIdentifier.characterId)
                }
            else if(settings.assumedFranchise != null) {
                buildDIMToBEMCharacter(applicationContext, cardMeta, swapCharacterIdentifier.slotId, settings) {
                    characterDao.getCharacterById(swapCharacterIdentifier.characterId)
                }
            }
            else buildDIMCharacter(applicationContext, cardMeta, swapCharacterIdentifier.slotId, settings) {
                characterDao.getCharacterById(swapCharacterIdentifier.characterId)
            }
            selectedCharacter.characterStats.lastUpdate = LocalDateTime.now() // don't count down timers for character that was in storage
            val currentCharacter = activeCharacterFlow.value
            if(currentCharacter != null) {
                currentCharacter.characterStats.state =
                    if(swapCharacterIdentifier.state == CharacterState.SUPPORT) CharacterState.SUPPORT
                    else CharacterState.STORED
                updateCharacter(currentCharacter.characterStats)
                vbUpdater.cancel()
            }
            selectedCharacter.characterStats.state = CharacterState.ACTIVE
            updateCharacter(selectedCharacter.characterStats)
            withContext(Dispatchers.Main) {
                activeCharacterFlow.value = selectedCharacter
                complicationRefreshService.refreshVitalsComplication()
            }
            vbUpdater.setupTransformationChecker(selectedCharacter)
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

    override suspend fun getTransformationHistory(characterId: Int): List<TransformationHistoryEntity> {
        return transformationHistoryDao.getByCharacterId(characterId)
    }

    override suspend fun addCharacter(
        cardName: String,
        characterEntity: CharacterEntity,
        characterSettings: CharacterSettings,
        transformationHistory: List<TransformationHistoryEntity>
    ): Int {
        val characterId = withContext(Dispatchers.IO) {
            val characterId = characterDao.insert(characterEntity).toInt()
            val settingsEntity = characterSettings.copy(characterId = characterId).toCharacterSettingsEntity()
            characterSettingsDao.insert(settingsEntity)
            val updatedTransformationHistory = mutableListOf<TransformationHistoryEntity>()
            for(transformation in transformationHistory) {
                updatedTransformationHistory.add(transformation.copy(characterId = characterId))
            }
            transformationHistoryDao.insert(updatedTransformationHistory)
            characterId
        }
        return characterId
    }

    override fun deleteCurrentCharacter() {
        activeCharacterFlow.value?.let {activeCharacter->
            deleteCharacter(activeCharacter.characterStats.id)
            activeCharacterFlow.update {
                var updateTo = it
                if(it != null && it.characterStats.id == activeCharacter.characterStats.id) {
                    updateTo = null
                }
                updateTo
            }
        }
    }

    override fun deleteCharacter(characterPreview: CharacterPreview) {
        val currentCharacter = activeCharacterFlow.value
        if(currentCharacter == null ||
            (currentCharacter.characterStats.id != characterPreview.characterId)) {
            deleteCharacter(characterPreview.characterId)
        } else {
            Timber.e("Cannot delete active character")
        }
    }

    private fun deleteCharacter(characterId: Int) {
        characterSettingsDao.deleteById(characterId)
        characterAdventureDao.deleteByCharacterId(characterId)
        transformationHistoryDao.deleteByCharacterId(characterId)
        characterDao.deleteById(characterId)
    }

    override fun maybeUpdateCardMeta(cardMeta: CardMeta) {
        val activeCharacter = activeCharacterFlow.value
        if(activeCharacter?.cardMeta?.cardName == cardMeta.cardName) {
            if(activeCharacter is BEMCharacter) {
                activeCharacterFlow.value = activeCharacter.copy(cardMeta = cardMeta)
            } else if(activeCharacter is DIMCharacter) {
                activeCharacterFlow.value = activeCharacter.copy(cardMeta = cardMeta)
            } else if(activeCharacter is DIMToBEMCharacter) {
                activeCharacterFlow.value = activeCharacter.copy(cardMeta = cardMeta)
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