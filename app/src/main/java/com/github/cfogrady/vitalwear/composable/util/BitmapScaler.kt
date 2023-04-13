package com.github.cfogrady.vitalwear.composable.util

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import com.github.cfogrady.vitalwear.activity.ImageScaler

class BitmapScaler(val imageScaler: ImageScaler) {
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

    @Composable
    fun AnimatedScaledBitmap(bitmaps: List<Bitmap>, startIdx: Int, frames: Int, contentDescription: String, alignment: Alignment = Alignment.TopStart, modifier: Modifier = Modifier) {
        var spriteIdx by remember { mutableStateOf(0) }
        animate({spriteIdx}, {x -> spriteIdx = x}, frames)
        ScaledBitmap(bitmap = bitmaps[startIdx + spriteIdx], contentDescription = contentDescription, alignment = alignment, modifier = modifier)
    }

    fun animate(getter: () -> Int, setter: (Int) -> Unit, max: Int) {
        Handler(Looper.getMainLooper()!!).postDelayed({
            var value = getter.invoke()
            value++
            if(value >= max) {
                value = 0
            }
            setter.invoke(value)
        }, 500)
    }
}