package com.github.cfogrady.vitalwear.card

import android.content.Context
import android.graphics.Bitmap
import com.github.cfogrady.vb.dim.sprite.SpriteData
import com.github.cfogrady.vb.dim.sprite.SpriteData.Sprite
import com.github.cfogrady.vitalwear.character.data.CharacterSprites
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
    }

    fun loadCharacterBitmapFile(applicationContext: Context, characterDir: String, spriteFile: String): Bitmap {
        return spriteBitmapConverter.getBitmap(loadCharacterSpriteFile(applicationContext, characterDir, spriteFile))
    }

    fun loadCharacterSpriteFile(applicationContext: Context, characterDir: String, spriteFile: String): Sprite {
        val file = File(applicationContext.filesDir, "${SpriteFileIO.LIBRARY_DIR}/${CHARACTERS}/$characterDir/$spriteFile")
        return spriteFileIO.loadSpriteFile(file)
    }

    fun characterSpritesExist(applicationContext: Context, characterDir: String): Boolean {
        val file = getCharacterFileDir(applicationContext, characterDir)
        return file.exists()
    }

    private fun getCharacterFileDir(applicationContext: Context, characterDir: String): File {
        return File(applicationContext.filesDir, "${SpriteFileIO.LIBRARY_DIR}/${CHARACTERS}/$characterDir")
    }

    private fun saveCharacterSpriteFile(applicationContext: Context, sprite: SpriteData.Sprite, characterDir: String, spriteFile: String) {
        val file = File(applicationContext.filesDir, "${SpriteFileIO.LIBRARY_DIR}/${CHARACTERS}/$characterDir/$spriteFile")
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

    private fun loadSpriteOrDefault(file: File, default: SpriteData.Sprite): SpriteData.Sprite {
        if(!file.exists()) {
            return default
        }
        return spriteFileIO.loadSpriteFile(file)
    }

    fun loadCharacterSprites(applicationContext: Context, characterDir: String) : CharacterSprites {
        //TODO: optimize by using the same idle bitmap instead of recreating it for missing sprites
        val sprites = ArrayList<Bitmap>()
        val rootCharDir = getCharacterFileDir(applicationContext, characterDir)
        val nameFile = File(rootCharDir, NAME)
        val nameSprite = spriteFileIO.loadSpriteFile(nameFile)
        sprites.add(spriteBitmapConverter.getBitmap(nameSprite))
        val idleFile = File(rootCharDir, IDLE1)
        val idle1Sprite = spriteFileIO.loadSpriteFile(idleFile)
        sprites.add(spriteBitmapConverter.getBitmap(idle1Sprite))
        val idle2File = File(rootCharDir, IDLE2)
        val idle2Sprite = spriteFileIO.loadSpriteFile(idle2File)
        sprites.add(spriteBitmapConverter.getBitmap(idle2Sprite))
        val walk1File = File(rootCharDir, WALK1)
        val walk1Sprite = spriteFileIO.loadSpriteFile(walk1File)
        sprites.add(spriteBitmapConverter.getBitmap(walk1Sprite))
        val walk2File = File(rootCharDir, WALK2)
        val walk2Sprite = loadSpriteOrDefault(walk2File, idle1Sprite)
        sprites.add(spriteBitmapConverter.getBitmap(walk2Sprite))
        val run1File = File(rootCharDir, RUN1)
        val run1Sprite = loadSpriteOrDefault(run1File, idle1Sprite)
        sprites.add(spriteBitmapConverter.getBitmap(run1Sprite))
        val run2File = File(rootCharDir, RUN2)
        val run2Sprite = loadSpriteOrDefault(run2File, idle1Sprite)
        sprites.add(spriteBitmapConverter.getBitmap(run2Sprite))
        val train1File = File(rootCharDir, TRAIN1)
        val train1Sprite = loadSpriteOrDefault(train1File, idle1Sprite)
        sprites.add(spriteBitmapConverter.getBitmap(train1Sprite))
        val train2File = File(rootCharDir, TRAIN2)
        val train2Sprite = loadSpriteOrDefault(train2File, idle1Sprite)
        sprites.add(spriteBitmapConverter.getBitmap(train2Sprite))
        val winFile = File(rootCharDir, WIN)
        val winSprite = spriteFileIO.loadSpriteFile(winFile)
        sprites.add(spriteBitmapConverter.getBitmap(winSprite))
        val downFile = File(rootCharDir, DOWN)
        val downSprite = spriteFileIO.loadSpriteFile(downFile)
        sprites.add(spriteBitmapConverter.getBitmap(downSprite))
        val attackFile = File(rootCharDir, ATTACK)
        val attackSprite = loadSpriteOrDefault(attackFile, idle1Sprite)
        sprites.add(spriteBitmapConverter.getBitmap(attackSprite))
        val dodgeFile = File(rootCharDir, DODGE)
        val dodgeSprite = loadSpriteOrDefault(dodgeFile, idle1Sprite)
        sprites.add(spriteBitmapConverter.getBitmap(dodgeSprite))
        val splashFile = File(rootCharDir, SPLASH)
        val splashSprite = loadSpriteOrDefault(splashFile, idle1Sprite)
        sprites.add(spriteBitmapConverter.getBitmap(splashSprite))
        return CharacterSprites(sprites)
    }
}