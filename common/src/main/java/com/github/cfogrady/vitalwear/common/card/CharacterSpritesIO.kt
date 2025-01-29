package com.github.cfogrady.vitalwear.common.card

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import com.github.cfogrady.vb.dim.sprite.SpriteData
import com.github.cfogrady.vb.dim.sprite.SpriteData.Sprite
import com.github.cfogrady.vb.dim.sprite.SpriteData.SpriteDimensions
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import timber.log.Timber
import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter

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

        // sprite to use when they key is missing.
        private val BACKUP_SPRITES = mapOf(
            Pair(RUN1, IDLE1),
            Pair(RUN2, IDLE2),
            Pair(TRAIN1, IDLE1),
            Pair(TRAIN2, IDLE2),
            Pair(ATTACK, IDLE2),
            Pair(DODGE, IDLE1),
            Pair(SPLASH, IDLE1),
        )
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
        var file = File(applicationContext.filesDir, "${SpriteFileIO.LIBRARY_DIR}/$CHARACTERS/$characterDir/$spriteFile")
        if(file.exists()) {
            return spriteFileIO.loadSpriteFile(file)
        }
        val backup = BACKUP_SPRITES[spriteFile]
        file = File(applicationContext.filesDir, "${SpriteFileIO.LIBRARY_DIR}/$CHARACTERS/$characterDir/$backup")
        if(file.exists()) {
            return spriteFileIO.loadSpriteFile(file)
        }
        Timber.w("Attempted to load sprite $spriteFile which doesn't exist. Backup $backup, also doesn't exist!")
        return null
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
        val spriteToSpriteType = generateSpriteMap(sprites, phase, contiguous)
        for(entry in spriteToSpriteType) {
            saveCharacterSpriteFile(applicationContext, entry.value, characterDir, entry.key)
        }
    }

    private fun loadCharacterSpriteOrDefault(file: File, default: Bitmap): Bitmap {
        if(!file.exists()) {
            return default
        }
        return loadCharacterSprite(file)
    }

    internal fun smallerThanStandardSize(dimensions: SpriteDimensions): Boolean {
        return dimensions.width < STANDARD_WIDTH && dimensions.height < STANDARD_HEIGHT
    }

    internal fun makeStandardSized(bitmap: Bitmap): Bitmap {
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
        val spriteMap = loadCharacterSpritesIntoMap(applicationContext, characterDir)
        return generateCharacterSpritesFromMap(spriteMap)
    }

    internal fun loadCharacterSpritesIntoMap(applicationContext: Context, characterDir: String): Map<String, Bitmap> {
        val map = mutableMapOf<String, Bitmap>()
        val rootCharDir = getCharacterFileDir(applicationContext, characterDir)
        val spriteFiles = rootCharDir.listFiles({ file ->
            file.name.endsWith(".img")
        })!!
        for(spriteFile in spriteFiles) {
            map[spriteFile.name] = loadCharacterSprite(spriteFile)
        }
        return map
    }

    internal fun generateSpriteMap(sprites: List<Sprite>, phase: Int, isDim: Boolean): Map<String, Sprite> {
        val map = mutableMapOf<String, Sprite>()
        if(phase >= 2) {
            map[NAME] = sprites[0]
            map[IDLE1] = sprites[1]
            map[IDLE2] = sprites[2]
            map[WALK1] = sprites[3]
            map[WALK2] = sprites[4]
            map[RUN1] = sprites[5]
            map[RUN2] = sprites[6]
            map[TRAIN1] = sprites[7]
            map[TRAIN2] = sprites[8]
            map[WIN] = sprites[9]
            map[DOWN] = sprites[10]
            map[ATTACK] = sprites[11]
            map[DODGE] = sprites[12]
            map[SPLASH] = sprites[13]
        } else if(phase == 1) {
            if(isDim) {//DIM
                map[NAME] = sprites[0]
                map[IDLE1] = sprites[1]
                map[IDLE2] = sprites[2]
                map[WALK1] = sprites[3]
                map[WIN] = sprites[4]
                map[DOWN] = sprites[5]
                map[SPLASH] = sprites[6]
            } else {//BEM
                map[NAME] = sprites[0]
                map[IDLE1] = sprites[1]
                map[IDLE2] = sprites[2]
                map[WALK1] = sprites[3]
                map[WIN] = sprites[9]
                map[DOWN] = sprites[10]
                map[SPLASH] = sprites[13]
            }
        } else {
            if(isDim) {//DIM
                map[NAME] = sprites[0]
                map[IDLE1] = sprites[1]
                map[IDLE2] = sprites[2]
                map[WALK1] = sprites[3]
                map[WIN] = sprites[4]
                map[DOWN] = sprites[5]
            } else {//BEM
                map[NAME] = sprites[0]
                map[IDLE1] = sprites[1]
                map[IDLE2] = sprites[2]
                map[WALK1] = sprites[3]
                map[WIN] = sprites[9]
                map[DOWN] = sprites[10]
            }
        }
        return map
    }

    internal fun generateCharacterSpritesFromMap(spritesByType: Map<String, Bitmap>): CharacterSprites {
        val sprites = ArrayList<Bitmap>()
        sprites.add(spritesByType[NAME]!!)
        sprites.add(spritesByType[IDLE1]!!)
        sprites.add(spritesByType[IDLE2]!!)
        sprites.add(spritesByType[WALK1]!!)
        sprites.add(spritesByType[WALK2]?: spritesByType[BACKUP_SPRITES[WALK2]]!!)
        sprites.add(spritesByType[RUN1]?: spritesByType[BACKUP_SPRITES[RUN1]]!!)
        sprites.add(spritesByType[RUN2]?: spritesByType[BACKUP_SPRITES[RUN2]]!!)
        sprites.add(spritesByType[TRAIN1]?: spritesByType[BACKUP_SPRITES[TRAIN1]]!!)
        sprites.add(spritesByType[TRAIN2]?: spritesByType[BACKUP_SPRITES[TRAIN2]]!!)
        sprites.add(spritesByType[WIN]!!)
        sprites.add(spritesByType[DOWN]!!)
        sprites.add(spritesByType[ATTACK]?: spritesByType[BACKUP_SPRITES[ATTACK]]!!)
        sprites.add(spritesByType[DODGE]?: spritesByType[BACKUP_SPRITES[DODGE]]!!)
        sprites.add(spritesByType[SPLASH]?: spritesByType[BACKUP_SPRITES[SPLASH]]!!)
        return CharacterSprites(sprites)
    }
}