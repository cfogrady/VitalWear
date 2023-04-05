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
import java.io.InputStream

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
            Log.i(TAG, "Reading card $entry")
            //TODO: 2+ seconds per card. That's a long potential load time...
            val card = loadCard(entry.key)
            Log.i(TAG, "Card read.")
            val cardSlots = SparseArray<Bitmap>()
            for(slotId in entry.value) {
                Log.i(TAG, "Fetching sprites for slot $slotId")
                val sprites = bemCharacterSprites(card.spriteData.sprites, slotId)
                Log.i(TAG, "Converting spriteId to bitmap.")
                val bitmap = spriteBitmapConverter.getBitmap(sprites.get(spriteId))
                Log.i(TAG, "Done.")
                cardSlots.put(slotId, bitmap)
            }
            resultMap.put(entry.key, cardSlots)
        }
        Log.i(TAG, "BitmapsForSlots Built")
        return resultMap
    }

    fun loadSpritesFromCard(cardName: String, slotIds: Collection<Int>) {
        val file = File(applicationContext.filesDir, "$LIBRARY_DIR/$cardName")
        try {
            FileInputStream(file).use { fileInput ->

            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to load Card", e)
            throw e
        }
    }

    fun readAndDiscardNBytes(n: Int, instr: InputStream) {
        val bytes = ByteArray(1024)
        while(n > 0) {
            bytes.get(1024)
        }
    }

    fun bitmapsFromCard(cardName: String, slotId: Int) : List<Bitmap> {
        val card = loadCard(cardName)
        return bitmapsFromCard(card, slotId)
    }

    fun bitmapsFromCard(card: Card<*, *, *, *, *, *>, slotId: Int) : List<Bitmap> {
        val sprites = spritesFromCard(card, slotId)
        return spriteBitmapConverter.getBitmaps(sprites)
    }

    fun bitmapFromCard(card: Card<*, *, *, *, *, *>, slotId: Int, characterSpriteIdx: Int) : Bitmap {
        val sprite = spritesFromCard(card, slotId).get(characterSpriteIdx)
        return spriteBitmapConverter.getBitmap(sprite)
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