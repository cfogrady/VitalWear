package com.github.cfogrady.vitalwear.data

import android.graphics.Bitmap
import com.github.cfogrady.vb.dim.sprite.SpriteData

class SpriteBitmapConverter {
    val TAG = "SpriteBitmapConverter"

    fun getBitmap(sprite: SpriteData.Sprite) : Bitmap {
        var bitmap = Bitmap.createBitmap(createARGBIntArray(sprite), sprite.width, sprite.height, Bitmap.Config.HARDWARE)
        return bitmap
    }

    fun createARGBIntArray(sprite: SpriteData.Sprite) : IntArray {
        var bytes = sprite.get24BitRGB()
        var result = IntArray(sprite.width*sprite.height)
        for(i in result.indices) {
            var originalIndex = i*3
            var red = bytes[originalIndex].toUInt() and 0xFFu
            var green = bytes[originalIndex+1].toUInt() and 0xFFu
            var blue = bytes[originalIndex+2].toUInt() and 0xFFu
            var alpha = if(red == 0u && blue == 0u && green == 0xFFu) 0 else 0xFF
            result[i] = (alpha shl 24) or (red shl 16).toInt() or (green shl 8).toInt() or blue.toInt()
        }
        return result
    }

    fun getBitmaps(sprites : List<SpriteData.Sprite>) : List<Bitmap> {
        return sprites.map(this::getBitmap)
    }
}