package com.github.cfogrady.vitalwear.character

import android.util.Log
import com.github.cfogrady.vb.dim.card.Card
import com.github.cfogrady.vb.dim.transformation.BemTransformationRequirements
import com.github.cfogrady.vb.dim.transformation.DimEvolutionRequirements
import com.github.cfogrady.vitalwear.character.data.*
import com.github.cfogrady.vitalwear.data.CardLoader
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.math.max

const val TAG = "CharacterManager"

class CharacterManager() : Service {

    lateinit var activePartner : Character

    fun loadActive() {
        if(this::activePartner.isInitialized) {
            Log.w(TAG, "Active Partner already loaded. Ignoring request to load.")
            return
        }
        // replace this with a table for activePartner and fetch by character id
        val activeCharacterStats = characterDao.getCharactersByNotState(CharacterState.BACKUP)
        if(!activeCharacterStats.isEmpty()) {
            val characterStats = activeCharacterStats.get(0)
            val card = cardLoader.loadCard(characterStats.cardFile)
            val bitmaps = cardLoader.bitmapsFromCard(card, characterStats.slotId)
            val timeToTransform = largestTransformationTimeSeconds(card, characterStats.slotId)
            val transformationOptions = transformationOptions(card, characterStats.slotId)
            val speciesStats = card.characterStats.characterEntries.get(characterStats.slotId)
            activePartner = Character(bitmaps, characterStats, speciesStats, timeToTransform, transformationOptions)
        }
    }

    fun previewCharacters() : List<CharacterPreview> {
        Log.i(TAG, "Fetching preview characters")
        val characters = characterDao.getCharactersOrderByRecent()
        Log.i(TAG, "Retrieved. Building slots needed map.")
        val slotsNeededByCard = HashMap<String, MutableSet<Int>>()
        for(character in characters) {
            val slotSet = slotsNeededByCard.getOrPut(character.cardFile) { HashSet<Int>() }
            slotSet.add(character.slotId)
        }
        Log.i(TAG, "Built. Reading Card Data.")
        val bitmapsByCardNameAndSlotId = cardLoader.loadBitmapsForSlots(slotsNeededByCard, 1)
        Log.i(TAG, "Read. Creating CharacterPreview objects")
        val previewCharacters = ArrayList<CharacterPreview>()
        for(character in characters) {
            val idleBitmap = bitmapsByCardNameAndSlotId.get(character.cardFile)!!.get(character.slotId)
            previewCharacters.add(CharacterPreview(character.cardFile, character.slotId, character.id, idleBitmap))
        }
        Log.i(TAG, "Ready")
        return previewCharacters
    }

    private fun performCharacterUpdate(character: Character) {
        val characterStats = character.characterStats
        val elapsedTimeInSeconds = Duration.between(characterStats.lastUpdate, LocalDateTime.now()).seconds
        if(characterStats.state != CharacterState.BACKUP) {
            characterStats.trainingTimeRemainingInSeconds -= elapsedTimeInSeconds;
            if(characterStats.trainingTimeRemainingInSeconds < 0) {
                characterStats.trainingTimeRemainingInSeconds = 0
            }
            if(characterStats.hasTransformations) {
                characterStats.timeUntilNextTransformation -= elapsedTimeInSeconds;
                if(characterStats.timeUntilNextTransformation <= 0) {
                    prepCharacterTransformation(character)
                }
            }
        }
    }

    fun prepCharacterTransformation(character: Character) {
        val characterStats = character.characterStats
        val transformationOption = hasValidTransformation(character)
        if(transformationOption.isPresent) {
            character.readyToTransform = transformationOption
        } else {
            characterStats.timeUntilNextTransformation = character.transformationWaitTimeSeconds
        }
    }

    fun doCharacterTransformation() {
        activePartner.readyToTransform.ifPresent { option ->
            val card = cardLoader.loadCard(activePartner.characterStats.cardFile)
            val transformationTime = largestTransformationTimeSeconds(card, option.slotId)
            val transformationOptions = transformationOptions(card, option.slotId)
            val bitmaps = cardLoader.bitmapsFromCard(card, option.slotId)
            val newSpeciesStats = card.characterStats.characterEntries.get(option.slotId)
            activePartner.characterStats.slotId = option.slotId
            activePartner.characterStats.currentPhaseBattles = 0
            activePartner.characterStats.currentPhaseWins = 0
            activePartner.characterStats.hasTransformations = transformationOptions.isNotEmpty()
            activePartner.characterStats.lastUpdate = LocalDateTime.now()
            activePartner.characterStats.timeUntilNextTransformation = transformationTime
            activePartner.characterStats.trainedPP = 0
            activePartner.characterStats.vitals = 0
            updateCharacter(activePartner.characterStats)
            activePartner = Character(bitmaps, activePartner.characterStats, newSpeciesStats, transformationTime, transformationOptions)
        }
    }

    private fun hasValidTransformation(character: Character): Optional<TransformationOption> {
        val stats = character.characterStats
        for(transformationOption in character.transformationOptions) {
            if(transformationOption.requiredVitals > stats.vitals) {
                continue
            }
            if(transformationOption.requiredPp > stats.trainedPP) {
                continue
            }
            if(transformationOption.requiredBattles > stats.currentPhaseBattles) {
                continue
            }
            if(transformationOption.requiredWinRatio > stats.currentPhaseWinRatio()) {
                continue
            }
            return Optional.of(transformationOption)
        }
        return Optional.empty()
    }

    private fun updateCharacter(character: Character) {
        updateCharacter(character.characterStats)
    }

    private fun updateCharacter(character: CharacterEntity) {
        characterDao.update(character)
    }

    private fun insertCharacter(character: CharacterEntity) {
        character.id = characterDao.insert(character).toInt()
    }

    fun swapToCharacter(selectedCharacter : CharacterPreview) {
        val characterStats = characterDao.getCharacterById(selectedCharacter.characterId).get(0)
        val card = cardLoader.loadCard(selectedCharacter.cardName)
        val speciesStats = card.characterStats.characterEntries.get(selectedCharacter.slotId)
        val bitmaps = cardLoader.bitmapsFromCard(card, selectedCharacter.slotId)
        val transformationTime = largestTransformationTimeSeconds(card, selectedCharacter.slotId)
        val transformationOptions = transformationOptions(card, selectedCharacter.slotId)
        val fullSelectedCharacter = Character(bitmaps, characterStats, speciesStats, transformationTime, transformationOptions)
        if(this::activePartner.isInitialized) {
            activePartner.characterStats.state = CharacterState.BACKUP
            updateCharacter(activePartner)
        }
        characterStats.state = CharacterState.SYNCED
        updateCharacter(characterStats)
        activePartner = fullSelectedCharacter
    }

    fun createNewCharacter(file: File) {
        val card = cardLoader.loadCard(file)
        val character = newCharacter(file.name, card, 0)
        if(this::activePartner.isInitialized) {
            activePartner.characterStats.state = CharacterState.BACKUP
            updateCharacter(activePartner)
        }
        insertCharacter(character.characterStats)
        activePartner = character
    }

    fun deleteCharacter(characterPreview: CharacterPreview) {
        characterDao.deleteById(characterPreview.characterId)
    }

    private fun newCharacter(file: String, card: Card<*, *, *, *, *, *>, slotId: Int) : Character {
        val entry = card.characterStats.characterEntries.get(slotId)
        val transformationTime = largestTransformationTimeSeconds(card, slotId)
        val bitmaps = cardLoader.bitmapsFromCard(card, slotId)
        val transformationOptions = transformationOptions(card, slotId)
        return Character(bitmaps, newCharacterEntityFromCard(file, slotId, transformationTime), entry, transformationTime, transformationOptions)
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
}