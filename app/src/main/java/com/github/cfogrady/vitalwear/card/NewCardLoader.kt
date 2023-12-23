package com.github.cfogrady.vitalwear.card

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.SparseArray
import com.github.cfogrady.vb.dim.adventure.BemAdventureLevels.BemAdventureLevel
import com.github.cfogrady.vb.dim.card.BemCard
import com.github.cfogrady.vb.dim.card.Card
import com.github.cfogrady.vb.dim.card.DimCard
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vb.dim.character.CharacterStats.CharacterStatsEntry
import com.github.cfogrady.vb.dim.character.DimStats
import com.github.cfogrady.vb.dim.sprite.SpriteData.Sprite
import com.github.cfogrady.vb.dim.transformation.BemTransformationRequirements.BemTransformationRequirementEntry
import com.github.cfogrady.vb.dim.transformation.DimEvolutionRequirements.DimEvolutionRequirementBlock
import com.github.cfogrady.vitalwear.card.db.*
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager
import com.google.android.gms.common.util.Hex
import com.google.common.collect.ImmutableSet
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * The purpose of this class is to import card images into the app database
 */
class NewCardLoader(
    private val characterSpritesIO: CharacterSpritesIO,
    private val cardSpritesIO: CardSpritesIO,
    private val cardMetaEntityDao: CardMetaEntityDao,
    private val speciesEntityDao: SpeciesEntityDao,
    private val transformationEntityDao: TransformationEntityDao,
    private val adventureEntityDao: AdventureEntityDao,
    private val attributeFusionEntityDao: AttributeFusionEntityDao,
    private val specificFusionEntityDao: SpecificFusionEntityDao,
    private val notificationChannelManager: NotificationChannelManager,
    private val dimReader: DimReader,
) {
    companion object {
        private const val TAG = "NewCardLoader"
        const val IMPORT_DIR = "import"
        private const val DIM_FIRST_CHARACTER_SPRITE_INDEX = 10
        private const val BEM_FIRST_CHARACTER_SPRITE_INDEX = 54
        private const val BEM_SPRITES_PER_CHARACTER = 14
    }

    private val cardsBeingLoaded = HashSet<String>()
    private val cardsBeingLoadedObservers = HashSet<CardLoaderObserverImpl>()

    private fun addCardBeingLoaded(cardName: String) {
        cardsBeingLoaded.add(cardName)
        val immutableSet = ImmutableSet.copyOf(cardsBeingLoaded)
        for(cardLoaderObserver in cardsBeingLoadedObservers) {
            cardLoaderObserver.cardsBeingLoaded = immutableSet
            cardLoaderObserver.receiveObservation()
        }
    }

    private fun removeCardBeingLoaded(cardName: String) {
        cardsBeingLoaded.remove(cardName)
        val immutableSet = ImmutableSet.copyOf(cardsBeingLoaded)
        for(cardLoaderObserver in cardsBeingLoadedObservers) {
            cardLoaderObserver.cardsBeingLoaded = immutableSet
            cardLoaderObserver.receiveObservation()
        }
    }

    fun observerCardLoading(onChange: () -> Unit): CardLoaderObserver {
        val observer = CardLoaderObserverImpl(onChange, ImmutableSet.copyOf(cardsBeingLoaded)) { thisCardLoaderObserver ->
            cardsBeingLoadedObservers.remove(thisCardLoaderObserver)
        }
        return observer
    }

    fun importCardImage(applicationContext: Context, cardName: String, inputStream: InputStream, uniqueSprites: Boolean = false) {
        addCardBeingLoaded(cardName)
        val card = dimReader.readCard(inputStream, true)
        val spritesByCharacterId = spritesByCharacterId(card)
        writeCardMeta(cardName, card)
        writeSpeciesEntitiesAndSprites(applicationContext, cardName, card, uniqueSprites, spritesByCharacterId)
        writeTransformations(cardName, card)
        writeAdventures(cardName, card)
        writeAttributeFusions(cardName, card)
        writeSpecificFusions(cardName, card)
        cardSpritesIO.saveCardSprites(applicationContext, cardName, card)
        removeCardBeingLoaded(cardName)
        notificationChannelManager.sendGenericNotification(applicationContext, "$cardName Imported", "$cardName Imported Successfully")
    }

    fun listImportDir(applicationContext: Context) : List<File> {
        val filesRoot = applicationContext.filesDir
        val libraryDir = File(filesRoot, IMPORT_DIR)
        return libraryDir.listFiles().toList()
    }

    fun loadBitmapsForSlots(applicationContext: Context, requestedSlotsByCardName : Map<String, out Collection<Int>>, spriteFile: String) : Map<String, SparseArray<Bitmap>> {
        val resultMap = HashMap<String, SparseArray<Bitmap>>()
        for(entry in requestedSlotsByCardName.entries) {
            Log.i(TAG, "Reading stats $entry")
            val speciesStats = speciesEntityDao.getCharacterByCardAndInCharacterIds(entry.key, entry.value)
            Log.i(TAG, "Card read.")
            val cardSlots = SparseArray<Bitmap>()
            for(speciesStat in speciesStats) {
                Log.i(TAG, "Fetching sprite for slot ${speciesStat.characterId}")
                val bitmap = characterSpritesIO.loadCharacterBitmapFile(applicationContext, speciesStat.spriteDirName, spriteFile)
                Log.i(TAG, "Done.")
                cardSlots.put(speciesStat.characterId, bitmap)
            }
            resultMap.put(entry.key, cardSlots)
        }
        Log.i(TAG, "BitmapsForSlots Built")
        return resultMap
    }

    private fun writeCardMeta(cardName: String, card: Card<*, *, *, *, *, *>) {
        var cardType = CardType.DIM
        var franchise = 0
        if(card is BemCard) {
            cardType = CardType.BEM
            franchise = card.header.bemFlags[2].toInt()
        }
        val cardMetaEntity = CardMetaEntity(cardName, card.header.dimId, card.checksum, cardType, franchise)
        cardMetaEntityDao.insert(cardMetaEntity)
    }

    private fun writeSpeciesEntitiesAndSprites(applicationContext: Context, cardName: String, card: Card<*, *, *, *, *, *>, uniqueSprites: Boolean, spritesByCharacterId: List<List<Sprite>>) {
        val speciesEntities = ArrayList<SpeciesEntity>()
        if(card is BemCard) {
            for((index, character) in card.characterStats.characterEntries.withIndex()) {
                val characterSprites = spritesByCharacterId[index]
                val background = getBackgroundSprite(characterSprites)
                val spriteDir = if(uniqueSprites) getUniqueSprites(cardName, index) else getBackgroundHash(applicationContext, cardName, index, card, background)
                characterSpritesIO.saveSpritesFromCard(applicationContext, false, character.stage, characterSprites, spriteDir)
                speciesEntities.add(createSpecies(cardName, index, character, 10, character.thirdPoolBattleChance, spriteDir))
            }
        } else if(card is DimCard) {
            for((index, character) in card.characterStats.characterEntries.withIndex()) {
                val background = getBackgroundSprite(spritesByCharacterId[index])
                val spriteDir = if(uniqueSprites) getUniqueSprites(cardName, index) else getBackgroundHash(applicationContext, cardName, index, card, background)
                speciesEntities.add(createSpecies(cardName, index, character, character.dpStars, 0, spriteDir))
            }
        }
        speciesEntityDao.insertAll(speciesEntities)
    }

    private fun writeTransformations(cardName: String, card: Card<*, *, *, *, *, *>) {
        val transformationEntities = ArrayList<TransformationEntity>()
        for((idx, cardTransformation) in card.transformationRequirements.transformationEntries.withIndex()) {
            var isSecret = false
            var minAdventureCompleteLevel: Int? = null
            var timeToTransform = 0
            if(cardTransformation is BemTransformationRequirementEntry) {
                isSecret = cardTransformation.isNotSecret != 1
                timeToTransform = cardTransformation.minutesUntilTransformation
                if(cardTransformation.requiredCompletedAdventureLevel != DimReader.NONE_VALUE) {
                    minAdventureCompleteLevel = cardTransformation.requiredCompletedAdventureLevel
                }
            } else if(cardTransformation is DimEvolutionRequirementBlock) {
                timeToTransform = cardTransformation.hoursUntilEvolution * 60
                if(cardTransformation.hasNextIndependentStage() &&
                    (card.characterStats.characterEntries[cardTransformation.toCharacterIndex] as DimStats.DimStatBlock).isUnlockRequired) {
                    minAdventureCompleteLevel = card.adventureLevels.levels.size-1
                }
            }
            val transformation = TransformationEntity(
                cardName,
                cardTransformation.fromCharacterIndex,
                cardTransformation.toCharacterIndex,
                timeToTransform,
                cardTransformation.requiredVitalValues,
                cardTransformation.requiredTrophies,
                cardTransformation.requiredBattles,
                cardTransformation.requiredWinRatio,
                minAdventureCompleteLevel,
                isSecret,
                idx
            )
            transformationEntities.add(transformation)
        }
        transformationEntityDao.insertAll(transformationEntities)
    }

    private fun writeAdventures(cardName: String, card: Card<*, *, *, *, *, *>) {
        val adventureEntities = ArrayList<AdventureEntity>()
        for((idx, cardAdventure) in card.adventureLevels.levels.withIndex()) {
            if(cardAdventure is BemAdventureLevel) {
                adventureEntities.add(
                    AdventureEntity(
                    cardName,
                    idx,
                    cardAdventure.steps,
                    cardAdventure.bossCharacterIndex,
                    cardAdventure.bossDp,
                    cardAdventure.bossHp,
                    cardAdventure.bossAp,
                    cardAdventure.smallAttackId,
                    cardAdventure.bigAttackId,
                    cardAdventure.background1,
                    cardAdventure.background2,
                    cardAdventure.showBossIdentity == 2,
                    if(cardAdventure.giftCharacterIndex == DimReader.NONE_VALUE) null else cardAdventure.giftCharacterIndex,
                        false
                )
                )
            } else {
                adventureEntities.add(
                    AdventureEntity(
                    cardName,
                    idx,
                    cardAdventure.steps,
                    cardAdventure.bossCharacterIndex,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0,
                    0,
                    false,
                    null,
                        false
                )
                )
            }
        }
        adventureEntityDao.insertAll(adventureEntities)
    }

    private fun writeAttributeFusions(cardName: String, card: Card<*, *, *, *, *, *>) {
        val attributeFusions = ArrayList<AttributeFusionEntity>()
        for(cardAttributeFusion in card.attributeFusions.entries) {
            attributeFusions.add(
                AttributeFusionEntity(
                cardName,
                cardAttributeFusion.characterIndex,
                if(cardAttributeFusion.attribute1Fusion == DimReader.NONE_VALUE) null else cardAttributeFusion.attribute1Fusion,
                if(cardAttributeFusion.attribute2Fusion == DimReader.NONE_VALUE) null else cardAttributeFusion.attribute2Fusion,
                if(cardAttributeFusion.attribute3Fusion == DimReader.NONE_VALUE) null else cardAttributeFusion.attribute3Fusion,
                if(cardAttributeFusion.attribute4Fusion == DimReader.NONE_VALUE) null else cardAttributeFusion.attribute4Fusion,
            )
            )
        }
        attributeFusionEntityDao.insertAll(attributeFusions)
    }

    private fun writeSpecificFusions(cardName: String, card: Card<*, *, *, *, *, *>) {
        val specificFusionEntities = ArrayList<SpecificFusionEntity>()
        for(cardFusion in card.specificFusions.entries) {
            specificFusionEntities.add(
                SpecificFusionEntity(
                cardName,
                cardFusion.fromCharacterIndex,
                cardFusion.toCharacterIndex,
                cardFusion.backupDimId,
                cardFusion.backupCharacterIndex
            )
            )
        }
        specificFusionEntityDao.insertAll(specificFusionEntities)
    }

    private fun createSpecies(cardName: String, index: Int, character: CharacterStatsEntry, stars: Int, battlePool3: Int, spriteDir: String): SpeciesEntity {
        return SpeciesEntity(
            cardName,
            index,
            character.stage,
            character.attribute,
            character.type,
            character.smallAttackId,
            character.bigAttackId,
            stars,
            character.dp,
            character.hp,
            character.ap,
            character.firstPoolBattleChance,
            character.secondPoolBattleChance,
            battlePool3,
            spriteDir,
            false,
        )
    }


    private fun getBackgroundHash(applicationContext: Context, cardName: String, characterId: Int, card: Card<*, *, *, *, *, *>, background: Sprite) : String {
        // characters that are stage 0 get unique hashes because they have no background
        if(card.characterStats.characterEntries[characterId].stage == 0) {
            return getUniqueSprites(cardName, characterId)
        }
        val digest = MessageDigest.getInstance("MD5")
        val hash = digest.digest(background.pixelData)
        val hex = Hex.bytesToStringLowercase(hash)
        var dir = hex
        var i = 1
        // handle hash collisions
        while(characterSpritesIO.characterSpritesExist(applicationContext, dir) && collision(applicationContext, dir, background)) {
            i++
            dir = "${hex}-$i"
        }
        return dir
    }

    private fun collision(applicationContext: Context, characterDir: String, newSplash: Sprite): Boolean {
        val existingSprite = characterSpritesIO.loadCharacterSpriteFile(applicationContext, characterDir, CharacterSpritesIO.SPLASH)
        if(existingSprite.width != newSplash.width || existingSprite.height != newSplash.height) {
            Log.e(TAG, "Splash images have wrong dimensions!")
            return true
        }
        if(!Arrays.equals(existingSprite.pixelData, newSplash.pixelData)) {
            return true
        }
        //this is the same data, not a collision
        return false
    }

    private fun getBackgroundSprite(sprites: List<Sprite>): Sprite {
        return sprites[sprites.size - 1]
    }

    private fun getUniqueSprites(cardName: String, characterId: Int): String {
        return "$cardName-$characterId"
    }

    private fun spritesByCharacterId(card: Card<*, *, *, *, *, *>) : List<List<Sprite>> {
        return if(card is BemCard) {
            bemCharacterSprites(card)
        } else {
            dimCharacterSprites(card)
        }
    }

    private fun bemCharacterSprites(card: BemCard) : List<List<Sprite>> {
        val spritesByCharacterId = ArrayList<List<Sprite>>()
        val sprites = card.spriteData.sprites
        for(slotId in 0 until card.characterStats.characterEntries.size) {
            val start: Int = BEM_FIRST_CHARACTER_SPRITE_INDEX + slotId * BEM_SPRITES_PER_CHARACTER
            val end: Int = BEM_FIRST_CHARACTER_SPRITE_INDEX + (slotId + 1) * BEM_SPRITES_PER_CHARACTER
            spritesByCharacterId.add(sprites.subList(start, end))
        }

        return spritesByCharacterId
    }

    private fun dimCharacterSprites(card: Card<*, *, *, *, *, *>) : List<List<Sprite>> {
        val spritesByCharacterId = ArrayList<List<Sprite>>()
        val sprites = card.spriteData.sprites
        var startingSprite = DIM_FIRST_CHARACTER_SPRITE_INDEX
        for(slotId in 0 until card.characterStats.characterEntries.size) {
            val spritesForCharacterId = sprites.subList(startingSprite, startingSprite + numberOfSpritesForDimSlot(slotId))
            startingSprite += spritesForCharacterId.size
            spritesByCharacterId.add(spritesForCharacterId)
        }
        return spritesByCharacterId
    }

    private fun numberOfSpritesForDimSlot(characterId: Int): Int {
        return when(characterId) {
            0 -> 6
            1 -> 7
            else -> 14
        }
    }
}