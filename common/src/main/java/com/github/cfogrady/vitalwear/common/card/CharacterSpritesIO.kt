package com.github.cfogrady.vitalwear.common.card

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.github.cfogrady.vb.dim.sprite.SpriteData
import com.github.cfogrady.vb.dim.sprite.SpriteData.Sprite
import com.github.cfogrady.vb.dim.sprite.SpriteData.SpriteDimensions
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import java.io.File

class CharacterSpritesIO(private val spriteFileIO: SpriteFileIO, private val spriteBitmapConverter: SpriteBitmapConverter) {
    companion object {
        const val CHARACTERS = "characters"

        const val NAME = "name.img"
        const val IDLE1 = "idle1.img"
        const val IDLE2 = "idle2.img"
        const val WALK1 = "walk1.img"
        const val WALK2 = "walk2.img"
        const val RUN1 = "run1.img"
        const val RUN2 = "run2.img"
        const val TRAIN1 = "train1.img"
        const val TRAIN2 = "train2.img"
        const val WIN = "win.img"
        const val DOWN = "down.img" //sleep or lose
        const val ATTACK = "attack.img"
        const val DODGE = "dodge.img"
        const val SPLASH = "splash.img"

        private const val STANDARD_WIDTH = 64
        private const val STANDARD_HEIGHT = 56
    }

    fun loadCharacterBitmapFile(applicationContext: Context, characterDir: String, spriteFile: String, resize: Boolean = false): Bitmap? {
        loadCharacterSpriteFile(applicationContext, characterDir, spriteFile)?.let {
            val bitmap = spriteBitmapConverter.getBitmap(it)
            if(resize && smallerThanStandardSize(it.spriteDimensions)) {
                return makeStandardSized(bitmap)
            }
            return bitmap
        }
        return null
    }

    internal fun loadCharacterSpriteFile(applicationContext: Context, characterDir: String, spriteFile: String): Sprite? {
        val file = File(applicationContext.filesDir, "${SpriteFileIO.LIBRARY_DIR}/$CHARACTERS/$characterDir/$spriteFile")
        if(file.exists()) {
            return spriteFileIO.loadSpriteFile(file)
        } else {
            return null
        }
    }

    fun characterSpritesExist(applicationContext: Context, characterDir: String): Boolean {
        val file = getCharacterFileDir(applicationContext, characterDir)
        return file.exists()
    }

    private fun getCharacterFileDir(applicationContext: Context, characterDir: String): File {
        return File(applicationContext.filesDir, "${SpriteFileIO.LIBRARY_DIR}/$CHARACTERS/$characterDir")
    }

    private fun saveCharacterSpriteFile(applicationContext: Context, sprite: SpriteData.Sprite, characterDir: String, spriteFile: String) {
        val file = File(applicationContext.filesDir, "${SpriteFileIO.LIBRARY_DIR}/$CHARACTERS/$characterDir/$spriteFile")
        spriteFileIO.saveSpriteFile(sprite, file)
    }

    // This could totally be refactored to reduce code... but this is clear for now.
    fun saveSpritesFromCard(applicationContext: Context, contiguous: Boolean, phase: Int, sprites: List<SpriteData.Sprite>, characterDir: String) {
        if(characterSpritesExist(applicationContext, characterDir)) {
            return //these sprites already exist
        }
        val characterDirFile = getCharacterFileDir(applicationContext, characterDir)
        if (!characterDirFile.mkdirs()) {
            throw java.lang.UnsupportedOperationException("Unable to create character directories")
        }
        if(phase >= 2) {
            saveCharacterSpriteFile(applicationContext, sprites[0], characterDir, NAME)
            saveCharacterSpriteFile(applicationContext, sprites[1], characterDir, IDLE1)
            saveCharacterSpriteFile(applicationContext, sprites[2], characterDir, IDLE2)
            saveCharacterSpriteFile(applicationContext, sprites[3], characterDir, WALK1)
            saveCharacterSpriteFile(applicationContext, sprites[4], characterDir, WALK2)
            saveCharacterSpriteFile(applicationContext, sprites[5], characterDir, RUN1)
            saveCharacterSpriteFile(applicationContext, sprites[6], characterDir, RUN2)
            saveCharacterSpriteFile(applicationContext, sprites[7], characterDir, TRAIN1)
            saveCharacterSpriteFile(applicationContext, sprites[8], characterDir, TRAIN2)
            saveCharacterSpriteFile(applicationContext, sprites[9], characterDir, WIN)
            saveCharacterSpriteFile(applicationContext, sprites[10], characterDir, DOWN)
            saveCharacterSpriteFile(applicationContext, sprites[11], characterDir, ATTACK)
            saveCharacterSpriteFile(applicationContext, sprites[12], characterDir, DODGE)
            saveCharacterSpriteFile(applicationContext, sprites[13], characterDir, SPLASH)
        } else if(phase == 1) {
            if(contiguous) {//DIM
                saveCharacterSpriteFile(applicationContext, sprites[0], characterDir, NAME)
                saveCharacterSpriteFile(applicationContext, sprites[1], characterDir, IDLE1)
                saveCharacterSpriteFile(applicationContext, sprites[2], characterDir, IDLE2)
                saveCharacterSpriteFile(applicationContext, sprites[3], characterDir, WALK1)
                saveCharacterSpriteFile(applicationContext, sprites[4], characterDir, WIN)
                saveCharacterSpriteFile(applicationContext, sprites[5], characterDir, DOWN)
                saveCharacterSpriteFile(applicationContext, sprites[6], characterDir, SPLASH)
            } else {//BEM
                saveCharacterSpriteFile(applicationContext, sprites[0], characterDir, NAME)
                saveCharacterSpriteFile(applicationContext, sprites[1], characterDir, IDLE1)
                saveCharacterSpriteFile(applicationContext, sprites[2], characterDir, IDLE2)
                saveCharacterSpriteFile(applicationContext, sprites[3], characterDir, WALK1)
                saveCharacterSpriteFile(applicationContext, sprites[9], characterDir, WIN)
                saveCharacterSpriteFile(applicationContext, sprites[10], characterDir, DOWN)
                saveCharacterSpriteFile(applicationContext, sprites[13], characterDir, SPLASH)
            }
        } else {
            if(contiguous) {//DIM
                saveCharacterSpriteFile(applicationContext, sprites[0], characterDir, NAME)
                saveCharacterSpriteFile(applicationContext, sprites[1], characterDir, IDLE1)
                saveCharacterSpriteFile(applicationContext, sprites[2], characterDir, IDLE2)
                saveCharacterSpriteFile(applicationContext, sprites[3], characterDir, WALK1)
                saveCharacterSpriteFile(applicationContext, sprites[4], characterDir, WIN)
                saveCharacterSpriteFile(applicationContext, sprites[5], characterDir, DOWN)
            } else {//BEM
                saveCharacterSpriteFile(applicationContext, sprites[0], characterDir, NAME)
                saveCharacterSpriteFile(applicationContext, sprites[1], characterDir, IDLE1)
                saveCharacterSpriteFile(applicationContext, sprites[2], characterDir, IDLE2)
                saveCharacterSpriteFile(applicationContext, sprites[3], characterDir, WALK1)
                saveCharacterSpriteFile(applicationContext, sprites[9], characterDir, WIN)
                saveCharacterSpriteFile(applicationContext, sprites[10], characterDir, DOWN)
            }
        }
    }

    private fun loadCharacterSpriteOrDefault(file: File, default: Bitmap): Bitmap {
        if(!file.exists()) {
            return default
        }
        return loadCharacterSprite(file)
    }

    private fun smallerThanStandardSize(dimensions: SpriteDimensions): Boolean {
        return dimensions.width < STANDARD_WIDTH && dimensions.height < STANDARD_HEIGHT
    }

    private fun makeStandardSized(bitmap: Bitmap): Bitmap {
        val standardSizedBitmap = Bitmap.createBitmap(STANDARD_WIDTH, STANDARD_HEIGHT, bitmap.config!!)
        val canvas = Canvas(standardSizedBitmap)
        canvas.drawBitmap(bitmap, (64f-bitmap.width)/2f, 56f-bitmap.height, null)
        return standardSizedBitmap
    }

    private fun loadCharacterSprite(file: File): Bitmap {
        val sprite = spriteFileIO.loadSpriteFile(file)
        val bitmap = spriteBitmapConverter.getBitmap(sprite)
        if(smallerThanStandardSize(sprite.spriteDimensions)) {
            return makeStandardSized(bitmap)
        }
        return bitmap
    }

    fun loadCharacterSprites(applicationContext: Context, characterDir: String) : CharacterSprites {
        val sprites = ArrayList<Bitmap>()
        val rootCharDir = getCharacterFileDir(applicationContext, characterDir)
        val nameFile = File(rootCharDir, NAME)
        val nameSprite = spriteFileIO.loadSpriteFile(nameFile)
        sprites.add(spriteBitmapConverter.getBitmap(nameSprite))
        val idleFile = File(rootCharDir, IDLE1)
        val idle1Bitmap = loadCharacterSprite(idleFile)
        sprites.add(idle1Bitmap)
        val idle2File = File(rootCharDir, IDLE2)
        val idle2Bitmap = loadCharacterSprite(idle2File)
        sprites.add(idle2Bitmap)
        val walk1File = File(rootCharDir, WALK1)
        sprites.add(loadCharacterSprite(walk1File))
        val walk2File = File(rootCharDir, WALK2)
        sprites.add(loadCharacterSpriteOrDefault(walk2File, idle1Bitmap))
        val run1File = File(rootCharDir, RUN1)
        sprites.add(loadCharacterSpriteOrDefault(run1File, idle1Bitmap))
        val run2File = File(rootCharDir, RUN2)
        sprites.add(loadCharacterSpriteOrDefault(run2File, idle2Bitmap))
        val train1File = File(rootCharDir, TRAIN1)
        sprites.add(loadCharacterSpriteOrDefault(train1File, idle1Bitmap))
        val train2File = File(rootCharDir, TRAIN2)
        sprites.add(loadCharacterSpriteOrDefault(train2File, idle2Bitmap))
        val winFile = File(rootCharDir, WIN)
        sprites.add(loadCharacterSprite(winFile))
        val downFile = File(rootCharDir, DOWN)
        sprites.add(loadCharacterSprite(downFile))
        val attackFile = File(rootCharDir, ATTACK)
        sprites.add(loadCharacterSpriteOrDefault(attackFile, idle2Bitmap))
        val dodgeFile = File(rootCharDir, DODGE)
        sprites.add(loadCharacterSpriteOrDefault(dodgeFile, idle1Bitmap))
        val splashFile = File(rootCharDir, SPLASH)
        sprites.add(loadCharacterSpriteOrDefault(splashFile, idle1Bitmap))
        return CharacterSprites(sprites)
    }
}