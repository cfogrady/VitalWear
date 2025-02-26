package com.github.cfogrady.vitalwear.common.card

import android.content.Context
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
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntityDao
import com.github.cfogrady.vitalwear.common.card.db.AttributeFusionEntity
import com.github.cfogrady.vitalwear.common.card.db.AttributeFusionEntityDao
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntity
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntityDao
import com.github.cfogrady.vitalwear.common.card.db.SpecificFusionEntity
import com.github.cfogrady.vitalwear.common.card.db.SpecificFusionEntityDao
import com.github.cfogrady.vitalwear.common.card.db.TransformationEntity
import com.github.cfogrady.vitalwear.common.card.db.TransformationEntityDao
import com.google.common.io.BaseEncoding
import timber.log.Timber
import java.io.InputStream
import java.lang.IllegalStateException
import java.security.MessageDigest
import java.util.*
import kotlin.collections.ArrayList

/**
 * The purpose of this class is to import card images into the app database
 */
class CardLoader(
    private val characterSpritesIO: CharacterSpritesIO,
    private val cardSpriteLoader: CardSpriteLoader,
    private val cardSpritesIO: CardSpritesIO,
    private val cardMetaEntityDao: CardMetaEntityDao,
    private val speciesEntityDao: SpeciesEntityDao,
    private val transformationEntityDao: TransformationEntityDao,
    private val adventureEntityDao: AdventureEntityDao,
    private val attributeFusionEntityDao: AttributeFusionEntityDao,
    private val specificFusionEntityDao: SpecificFusionEntityDao,
    private val dimReader: DimReader,
) {

    fun importCardImage(applicationContext: Context, cardName: String, card: Card<*, *, *, *, *, *>, uniqueSprites: Boolean = false) {
        val spritesByCharacterId = cardSpriteLoader.spritesByCharacterId(card)
        writeCardMeta(cardName, card)
        writeSpeciesEntitiesAndSprites(applicationContext, cardName, card, uniqueSprites, spritesByCharacterId)
        writeTransformations(cardName, card)
        writeAdventures(cardName, card)
        writeAttributeFusions(cardName, card)
        writeSpecificFusions(cardName, card)
        cardSpritesIO.saveCardSprites(applicationContext, cardName, card)
    }

    fun importCardImage(applicationContext: Context, cardName: String, inputStream: InputStream, uniqueSprites: Boolean = false) {
        val card = dimReader.readCard(inputStream, true)
        if(card.calculatedCheckSum != card.checksum) {
            throw IllegalStateException("Card Image has bad checksum. Expected: ${card.checksum} Actual: ${card.calculatedCheckSum}")
        }
        importCardImage(applicationContext, cardName, card, uniqueSprites)
    }

    private fun writeCardMeta(cardName: String, card: Card<*, *, *, *, *, *>) {
        var cardType = CardType.DIM
        var franchise = 0
        if(card is BemCard) {
            cardType = CardType.BEM
            franchise = card.header.franchiseId
        }
        val cardMetaEntity = CardMetaEntity(cardName, card.header.dimId, card.checksum, cardType, franchise, null)
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
                val characterSprites = spritesByCharacterId[index]
                val background = getBackgroundSprite(spritesByCharacterId[index])
                val spriteDir = if(uniqueSprites) getUniqueSprites(cardName, index) else getBackgroundHash(applicationContext, cardName, index, card, background)
                characterSpritesIO.saveSpritesFromCard(applicationContext, true, character.stage, characterSprites, spriteDir)
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
                        if(cardAdventure.giftCharacterIndex == DimReader.NONE_VALUE) null else cardAdventure.giftCharacterIndex
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
        val hex = BaseEncoding.base16().encode(hash)
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
        val existingSprite = characterSpritesIO.loadCharacterSpriteFile(applicationContext, characterDir, CharacterSpritesIO.SPLASH)!!
        if(existingSprite.width != newSplash.width || existingSprite.height != newSplash.height) {
            Timber.e("Splash images have wrong dimensions!")
            return true
        }
        // check if the data is the same instead of a collision
        return !Arrays.equals(existingSprite.pixelData, newSplash.pixelData)
    }

    private fun getBackgroundSprite(sprites: List<Sprite>): Sprite {
        return sprites[sprites.size - 1]
    }

    private fun getUniqueSprites(cardName: String, characterId: Int): String {
        return "$cardName-$characterId"
    }
}