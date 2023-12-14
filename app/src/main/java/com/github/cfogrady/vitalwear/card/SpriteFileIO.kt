package com.github.cfogrady.vitalwear.card

import com.github.cfogrady.vb.dim.sprite.SpriteData.Sprite
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SpriteFileIO {
    companion object {
        const val LIBRARY_DIR = "library"
    }

    internal fun loadSpriteFile(file: File): Sprite {
        FileInputStream(file).use { fileInput ->
            val width = fileInput.read()
            val height = fileInput.read()
            val pixelData = fileInput.readBytes()
            return Sprite.builder().width(width).height(height).pixelData(pixelData).build()
        }
    }

    internal fun saveSpriteFile(sprite: Sprite, file: File) {
        FileOutputStream(file).use {fileOutput ->
            fileOutput.write(sprite.width)
            fileOutput.write(sprite.height)
            fileOutput.write(sprite.pixelData)
        }
    }
}