package com.github.cfogrady.vitalwear.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.SparseArray
import com.github.cfogrady.vb.dim.card.BemCard
import com.github.cfogrady.vb.dim.card.Card
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vb.dim.sprite.SpriteData
import java.io.File
import java.io.FileInputStream

class CardLoader(val applicationContext: Context, val spriteBitmapConverter: SpriteBitmapConverter) {
    private val TAG = "CardLoader"
    private val BEM_FIRST_CHARACTER_SPRITE_INDEX = 54
    private val BEM_SPRITES_PER_CHARACTER = 14
    private val CARD_FILE = "imperialdramon+Favs.bin"
    private val LIBRARY_DIR = "library"

    private val dimReader = DimReader()

    fun loadCard(file: File = File(applicationContext.filesDir,
        "$LIBRARY_DIR/$CARD_FILE"
    )): Card<*, *, *, *, *, *> {
        try {
            FileInputStream(file).use { fileInput ->
                return dimReader.readCard(fileInput, true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to load Card", e)
            throw e
        }
    }

    fun loadCard(cardName: String): Card<*, *, *, *, *, *> {
        val file = File(applicationContext.filesDir, "$LIBRARY_DIR/$cardName")
        return loadCard(file)
    }

    fun listLibrary(applicationContext: Context) : List<File> {
        var filesRoot = applicationContext.filesDir
        var libraryDir = File(filesRoot, LIBRARY_DIR)
        return libraryDir.listFiles().toList()
    }

    fun loadBitmapsForSlots(requestedSlotsByCardName : Map<String, out Collection<Int>>, spriteId: Int) : Map<String, SparseArray<Bitmap>> {
        val resultMap = HashMap<String, SparseArray<Bitmap>>()
        for(entry in requestedSlotsByCardName.entries) {
            val card = loadCard(entry.key)
            val cardSlots = SparseArray<Bitmap>()
            for(slotId in entry.value) {
                val sprites = bemCharacterSprites(card.spriteData.sprites, slotId)
                val bitmap = spriteBitmapConverter.getBitmap(sprites.get(spriteId))
                cardSlots.put(slotId, bitmap)
            }
            resultMap.put(entry.key, cardSlots)
        }
        return resultMap
    }

    fun bitmapsFromCard(cardName: String, slotId: Int) : List<Bitmap> {
        val card = loadCard(cardName)
        return bitmapsFromCard(card, slotId)
    }

    fun bitmapsFromCard(card: Card<*, *, *, *, *, *>, slotId: Int) : List<Bitmap> {
        val sprites = spritesFromCard(card, slotId)
        return spriteBitmapConverter.getBitmaps(sprites)
    }

    fun spritesFromCard(card: Card<*, *, *, *, *, *>, slotId: Int) : List<SpriteData.Sprite> {
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