package com.github.cfogrady.vitalwear.common.card

import android.content.Context
import com.github.cfogrady.vb.dim.card.BemCard
import com.github.cfogrady.vb.dim.card.Card
import com.github.cfogrady.vb.dim.card.DimCard
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vb.dim.sprite.SpriteData.Sprite
import com.github.cfogrady.vitalwear.common.character.CharacterSprites

class CardSpriteLoader {
    companion object {
        private const val DIM_FIRST_CHARACTER_SPRITE_INDEX = 10
        private const val BEM_FIRST_CHARACTER_SPRITE_INDEX = 54
        private const val BEM_SPRITES_PER_CHARACTER = 14

        fun loadTestCharacterSprites(
            context: Context,
            slotId: Int = 0,
            cardName: String = "test_dim.bin",
        ): CharacterSprites {
            val dimReader = DimReader()
            val spriteBitmapConverter = SpriteBitmapConverter()
            val spriteFileIO = SpriteFileIO()
            val characterSpritesIO = CharacterSpritesIO(spriteFileIO, spriteBitmapConverter)
            context.assets.open(cardName).use {
                val card = dimReader.readCard(it, false)
                val cardSpriteLoader = CardSpriteLoader()
                val spritesBySlot = cardSpriteLoader.spritesByCharacterId(card)
                val spriteByType = characterSpritesIO.generateSpriteMap(
                    spritesBySlot[slotId],
                    card.characterStats.characterEntries[slotId].stage,
                    card is DimCard)
                return characterSpritesIO.generateCharacterSpritesFromMap(spriteByType.mapValues { entry ->
                    var bitmap = spriteBitmapConverter.getBitmap(entry.value)
                    if(entry.key != CharacterSpritesIO.NAME &&
                        characterSpritesIO.smallerThanStandardSize(entry.value.spriteDimensions)) {
                        bitmap = characterSpritesIO.makeStandardSized(bitmap)
                    }
                    bitmap
                })
            }
        }
    }

    fun spritesByCharacterId(card: Card<*, *, *, *, *, *>) : List<List<Sprite>> {
        return if(card is BemCard) {
            bemCharacterSprites(card)
        } else {
            dimCharacterSprites(card)
        }
    }

    fun bemCharacterSprites(card: BemCard) : List<List<Sprite>> {
        val spritesByCharacterId = ArrayList<List<Sprite>>()
        val sprites = card.spriteData.sprites
        for(slotId in 0 until card.characterStats.characterEntries.size) {
            val start: Int = BEM_FIRST_CHARACTER_SPRITE_INDEX + slotId * BEM_SPRITES_PER_CHARACTER
            val end: Int = BEM_FIRST_CHARACTER_SPRITE_INDEX + (slotId + 1) * BEM_SPRITES_PER_CHARACTER
            spritesByCharacterId.add(sprites.subList(start, end))
        }

        return spritesByCharacterId
    }

    fun dimCharacterSprites(card: Card<*, *, *, *, *, *>) : List<List<Sprite>> {
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