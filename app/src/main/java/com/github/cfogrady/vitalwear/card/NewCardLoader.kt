package com.github.cfogrady.vitalwear.card

import com.github.cfogrady.vb.dim.card.BemCard
import com.github.cfogrady.vb.dim.card.Card
import com.github.cfogrady.vb.dim.card.DimCard
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vb.dim.character.CharacterStats.CharacterStatsEntry
import com.github.cfogrady.vb.dim.sprite.SpriteData
import java.io.InputStream

/**
 * The purpose of this class is to import card images into the app database
 */
class NewCardLoader(
    private val cardMetaEntityDao: CardMetaEntityDao,
    private val speciesEntityDao: SpeciesEntityDao,
    private val transformationEntityDao: TransformationEntityDao,
    private val adventureEntityDao: AdventureEntityDao,
    private val attributeFusionEntityDao: AttributeFusionEntityDao,
    private val specificFusionEntityDao: SpecificFusionEntityDao,
    private val dimReader: DimReader,
    private val spriteBitmapConverter: SpriteBitmapConverter,
) {
    companion object {
        private const val BEM_FIRST_CHARACTER_SPRITE_INDEX = 54
        private const val BEM_SPRITES_PER_CHARACTER = 14
    }

    fun importCardImage(cardName: String, inputStream: InputStream, uniqueSprites: Boolean = false) {
        val card = dimReader.readCard(inputStream, true)
        writeCardMeta(cardName, card)
    }

    fun writeCardMeta(cardName: String, card: Card<*, *, *, *, *, *>) {
        var cardType = CardType.DIM
        var franchise = 0
        if(card is BemCard) {
            cardType = CardType.BEM
            franchise = card.header.bemFlags[2].toInt()
        }
        val cardMetaEntity = CardMetaEntity(cardName, card.header.dimId, card.checksum, cardType, franchise)
        cardMetaEntityDao.insert(cardMetaEntity)
    }

    fun writeSpeciesEntities(cardName: String, card: Card<*, *, *, *, *, *>, uniqueSprites: Boolean) {
        val speciesEntities = ArrayList<SpeciesEntity>()
        if(card is BemCard) {
            for((index, character) in card.characterStats.characterEntries.withIndex()) {
                val spriteDir = if(uniqueSprites) getUniqueSprites(cardName, index) else getBackgroundHash(cardName, index, card)
                speciesEntities.add(createSpecies(cardName, index, character, 10, character.thirdPoolBattleChance, spriteDir))
            }
        } else if(card is DimCard) {
            for((index, character) in card.characterStats.characterEntries.withIndex()) {
                val spriteDir = if(uniqueSprites) getUniqueSprites(cardName, index) else getBackgroundHash(cardName, index, card)
                speciesEntities.add(createSpecies(cardName, index, character, character.dpStars, 0, spriteDir))
            }
        }
        speciesEntityDao.insertAll(speciesEntities)
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
        )
    }

    private fun getBackgroundHash(cardName: String, characterId: Int, card: Card<*, *, *, *, *, *>) : String {
        if(card.characterStats.characterEntries[characterId].stage == 0) {

        }
    }

    private fun getUniqueSprites(cardName: String, characterId: Int): String {
        return "$cardName-$characterId"
    }

    private fun spritesFromCard(card: Card<*, *, *, *, *, *>, slotId: Int) : List<SpriteData.Sprite> {
        if(card is BemCard) {
            return bemCharacterSprites(card.spriteData.sprites, slotId)
        } else {
            return dimCharacterSprites(card.spriteData.sprites, slotId)
        }
    }

    private fun bemCharacterSprites(sprites: List<SpriteData.Sprite>, slotId: Int) : List<SpriteData.Sprite> {
        val start: Int = BEM_FIRST_CHARACTER_SPRITE_INDEX + slotId * BEM_SPRITES_PER_CHARACTER
        val end: Int = BEM_FIRST_CHARACTER_SPRITE_INDEX + (slotId + 1) * BEM_SPRITES_PER_CHARACTER
        return sprites.subList(start, end)
    }

    private fun dimCharacterSprites(sprites: List<SpriteData.Sprite>, slotId: Int) : List<SpriteData.Sprite> {
        var startingSprite = 10
        for (i in 0 until slotId) {
            startingSprite += numberOfSpritesForDimSlot(i)
        }
        return sprites.subList(
            startingSprite,
            startingSprite + numberOfSpritesForDimSlot(slotId)
        )
    }

    private fun numberOfSpritesForDimSlot(stage: Int): Int {
        return if (stage == 0) {
            6
        } else if (stage == 1) {
            7
        } else {
            14
        }
    }
}