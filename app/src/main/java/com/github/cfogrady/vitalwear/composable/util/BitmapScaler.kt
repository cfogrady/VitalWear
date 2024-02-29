package com.github.cfogrady.vitalwear.composable.util

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.activity.ImageScaler

class BitmapScaler(val imageScaler: ImageScaler) {
    companion object {
        const val TAG = "BitmapScaler"
    }

    @Composable
    fun ScaledBitmap(bitmap: Bitmap, contentDescription: String, modifier: Modifier = Modifier, alignment: Alignment = Alignment.TopStart) {
        val scaledWidth = imageScaler.scaledDpValueFromPixels(bitmap.width)
        val scaledHeight = imageScaler.scaledDpValueFromPixels(bitmap.height)
        Image(bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier.width(scaledWidth).height(scaledHeight),
            alignment = alignment
        )
    }

    fun scaledDimension(x: Int): Dp {
        return imageScaler.scaledDpValueFromPixels(x)
    }

    @Composable
    fun AnimatedScaledBitmap(bitmaps: List<Bitmap?>, startIdx: Int = 0, frames: Int = bitmaps.size, contentDescription: String, alignment: Alignment = Alignment.TopStart, modifier: Modifier = Modifier, msPerFrame: Long = 500) {
        var spriteIdx by remember { mutableIntStateOf(0) }
        // only run this when spriteIdx is changed
        remember(spriteIdx) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                spriteIdx++
                if(spriteIdx >= frames) {
                    spriteIdx = 0
                }
            }, msPerFrame)
        }
        val currentFrame = bitmaps[startIdx + spriteIdx]
        if(currentFrame != null) {
            ScaledBitmap(bitmap = currentFrame, contentDescription = contentDescription, alignment = alignment, modifier = modifier)
        }
    }
}