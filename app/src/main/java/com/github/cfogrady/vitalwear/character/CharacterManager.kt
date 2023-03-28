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
import kotlin.math.max

const val TAG = "CharacterManager"

class CharacterManager(val characterDao: CharacterDao, val cardLoader: CardLoader) {

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
            val speciesStats = card.characterStats.characterEntries.get(characterStats.slotId)
            activePartner = Character(bitmaps, characterStats, speciesStats)
        }
    }

    fun previewCharacters() : List<CharacterPreview> {
        val characters = characterDao.getCharactersOrderByRecent()
        val slotsNeededByCard = HashMap<String, MutableSet<Int>>()
        for(character in characters) {
            val slotSet = slotsNeededByCard.getOrPut(character.cardFile) { HashSet<Int>() }
            slotSet.add(character.slotId)
        }
        val bitmapsByCardNameAndSlotId = cardLoader.loadBitmapsForSlots(slotsNeededByCard, 1)
        val previewCharacters = ArrayList<CharacterPreview>()
        for(character in characters) {
            val idleBitmap = bitmapsByCardNameAndSlotId.get(character.cardFile)!!.get(character.slotId)
            previewCharacters.add(CharacterPreview(character.cardFile, character.slotId, character.id, idleBitmap))
        }
        return previewCharacters
    }

    private fun performCharacterUpdate(character: CharacterEntity) {
        val elapsedTimeInSeconds = Duration.between(character.lastUpdate, LocalDateTime.now()).seconds
        if(character.state != CharacterState.BACKUP) {
            character.trainingTimeRemainingInSeconds -= elapsedTimeInSeconds;
            if(character.trainingTimeRemainingInSeconds < 0) {
                character.trainingTimeRemainingInSeconds = 0
            }
            if(character.hasTransformations) {
                character.timeUntilNextTransformation -= elapsedTimeInSeconds;
            }
        }
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
        val fullSelectedCharacter = Character(bitmaps, characterStats, speciesStats)
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
        return Character(bitmaps, newCharacterEntityFromCard(file, slotId, transformationTime), entry)
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