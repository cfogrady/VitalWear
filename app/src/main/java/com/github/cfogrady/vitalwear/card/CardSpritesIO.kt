package com.github.cfogrady.vitalwear.card

import android.content.Context
import android.graphics.Bitmap
import com.github.cfogrady.vb.dim.card.BemCard
import com.github.cfogrady.vb.dim.card.Card
import com.github.cfogrady.vb.dim.sprite.SpriteData
import java.io.File

class CardSpritesIO(private val spriteFileIO: SpriteFileIO, private val spriteBitmapConverter: SpriteBitmapConverter) {
    companion object {
        const val CARDS = "cards"

        // specific images
        const val ICON = "icon.img"
        const val READY = "ready.img"
        const val GO = "go.img"
        const val WIN = "win.img"
        const val LOSE = "lose.img"
        // directories with numbered files
        const val BACKGROUNDS = "backgrounds"
        const val ATTRIBUTE_ICONS = "attributes"
        const val PHASE_ICONS = "phases"
        const val START_ICONS = "start"
        const val RECEIVE_HIT_ICONS = "hit"
        const val ATTACKS = "attacks"
        const val CRITICAL_ATTACKS = "critical_attacks"
    }

    fun loadCardSprite(applicationContext: Context, cardName: String, spriteFile: String): Bitmap {
        return spriteBitmapConverter.getBitmap(loadCardSpriteFile(applicationContext, cardName, spriteFile))
    }

    fun loadCardSpriteList(applicationContext: Context, cardName: String, spriteType: String): List<Bitmap> {
        val sprites = ArrayList<Bitmap>()
        val dir = File(applicationContext.filesDir, "${SpriteFileIO.LIBRARY_DIR}/${CARDS}/$cardName/$spriteType")
        for(item in dir.listFiles()) {
            sprites.add(spriteBitmapConverter.getBitmap(spriteFileIO.loadSpriteFile(item)))
        }
        return sprites
    }

    fun loadIndexedSprite(applicationContext: Context, cardName: String, spriteType: String, index: Int): Bitmap {
        return spriteBitmapConverter.getBitmap(loadCardSpriteFile(applicationContext, cardName, "$spriteType/$index.img"))
    }

    private fun loadCardSpriteFile(applicationContext: Context, cardName: String, spriteFile: String): SpriteData.Sprite {
        val file = File(applicationContext.filesDir, "${SpriteFileIO.LIBRARY_DIR}/${CARDS}/$cardName/$spriteFile")
        return spriteFileIO.loadSpriteFile(file)
    }

    fun loadCardBackgrounds(applicationContext: Context, cardName: String): ArrayList<SpriteData.Sprite> {
        val backgrounds = ArrayList<SpriteData.Sprite>()
        val file = File(applicationContext.filesDir, "${SpriteFileIO.LIBRARY_DIR}/${CARDS}/$cardName/$BACKGROUNDS")
        for(i in 0 .. 5) {
            val backgroundFile = File(file, "$i.img")
            if(backgroundFile.exists()) {
                backgrounds.add(spriteFileIO.loadSpriteFile(file))
            } else {
                break
            }
        }
        return backgrounds
    }

    private fun saveCardSpriteFile(applicationContext: Context, sprite: SpriteData.Sprite, cardName: String, spriteFile: String) {
        val file = File(applicationContext.filesDir, "${SpriteFileIO.LIBRARY_DIR}/$CARDS/$cardName/$spriteFile")
        spriteFileIO.saveSpriteFile(sprite, file)
    }

    private fun saveDirectory(applicationContext: Context, sprites: Collection<SpriteData.Sprite>, cardName: String, directorName: String) {
        val directory = File(applicationContext.filesDir, "${SpriteFileIO.LIBRARY_DIR}/$CARDS/$cardName/$directorName")
        directory.mkdirs()
        for((idx, sprite) in sprites.withIndex()) {
            spriteFileIO.saveSpriteFile(sprite, File(directory, "$idx.img"))
        }
    }

    fun saveCardSprites(applicationContext: Context, cardName: String, card: Card<*, *, *, *, *, *>) {
        val cardDir = File(applicationContext.filesDir, "${SpriteFileIO.LIBRARY_DIR}/$CARDS/$cardName")
        cardDir.mkdirs()
        File(cardDir, BACKGROUNDS).mkdirs()
        saveCardSpriteFile(applicationContext, card.spriteData.sprites[0], cardName, ICON)
        saveCardSpriteFile(applicationContext, card.spriteData.sprites[1], cardName, "$BACKGROUNDS/0.img")
        saveDirectory(applicationContext, card.spriteData.sprites.subList(2, 10), cardName, START_ICONS)
        if(card is BemCard) {
            saveCardSpriteFile(applicationContext, card.spriteData.sprites[10], cardName, "$BACKGROUNDS/1.img")
            saveCardSpriteFile(applicationContext, card.spriteData.sprites[11], cardName, READY)
            saveCardSpriteFile(applicationContext, card.spriteData.sprites[12], cardName, GO)
            saveCardSpriteFile(applicationContext, card.spriteData.sprites[13], cardName, WIN)
            saveCardSpriteFile(applicationContext, card.spriteData.sprites[14], cardName, LOSE)
            saveDirectory(applicationContext, card.spriteData.sprites.subList(15, 18), cardName, RECEIVE_HIT_ICONS)
            saveDirectory(applicationContext, card.spriteData.sprites.subList(18, 22), cardName, ATTRIBUTE_ICONS)
            saveDirectory(applicationContext, card.spriteData.sprites.subList(22, 30), cardName, PHASE_ICONS)
            saveCardSpriteFile(applicationContext, card.spriteData.sprites[30], cardName, "$BACKGROUNDS/2.img")
            saveCardSpriteFile(applicationContext, card.spriteData.sprites[31], cardName, "$BACKGROUNDS/3.img")
            saveCardSpriteFile(applicationContext, card.spriteData.sprites[32], cardName, "$BACKGROUNDS/4.img")
            saveCardSpriteFile(applicationContext, card.spriteData.sprites[33], cardName, "$BACKGROUNDS/5.img")
            saveDirectory(applicationContext, card.spriteData.sprites.subList(34, 44), cardName, ATTACKS)
            saveDirectory(applicationContext, card.spriteData.sprites.subList(44, 54), cardName, CRITICAL_ATTACKS)
        }
    }
}