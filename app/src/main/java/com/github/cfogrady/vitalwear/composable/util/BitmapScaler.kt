package com.github.cfogrady.vitalwear.composable.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.wear.tooling.preview.devices.WearDevices
import com.github.cfogrady.vitalwear.activity.ImageScaler

class BitmapScaler(val imageScaler: ImageScaler) {
    companion object {
        const val TAG = "BitmapScaler"

        // meant for tests
        fun buildBitmapScalerFactory(context: Context): BitmapScaler {
            return BitmapScaler(ImageScaler.getContextImageScaler(context))
        }
    }

    @Composable
    fun ScaledBitmap(
        bitmap: Bitmap,
        contentDescription: String,
        modifier: Modifier = Modifier,
        alignment: Alignment = Alignment.TopStart,
    ) {
        ScaledBitmap(
            bitmap = bitmap,
            contentDescription = contentDescription,
            imageScaler = imageScaler,
            modifier = modifier,
            alignment = alignment
        )
    }

    @Composable
    fun AnimatedScaledBitmap(
        bitmaps: List<Bitmap?>,
        startIdx: Int = 0,
        frames: Int = bitmaps.size,
        contentDescription: String,
        alignment: Alignment = Alignment.TopStart,
        modifier: Modifier = Modifier,
        msPerFrame: Long = 500) {
        AnimatedScaledBitmap(
            bitmaps = bitmaps,
            startIdx = startIdx,
            imageScaler = imageScaler,
            frames = frames,
            contentDescription = contentDescription,
            alignment = alignment,
            modifier = modifier,
            msPerFrame = msPerFrame,
        )
    }

    fun scaledDimension(x: Int): Dp {
        return imageScaler.scaledDpValueFromPixels(x)
    }
}

@Composable
fun ScaledBitmap(
    bitmap: Bitmap,
    contentDescription: String,
    imageScaler: ImageScaler,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopStart,
) {
    val scaledWidth = imageScaler.scaledDpValueFromPixels(bitmap.width)
    val scaledHeight = imageScaler.scaledDpValueFromPixels(bitmap.height)
    Image(bitmap = bitmap.asImageBitmap(),
        contentDescription = contentDescription,
        modifier = modifier.width(scaledWidth).height(scaledHeight),
        alignment = alignment
    )
}

@Preview(
    device = WearDevices.SQUARE,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun PreviewScaledBitmap() {
    val imageScaler = ImageScaler.getContextImageScaler(LocalContext.current)
    VitalBox(imageScaler) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            ScaledBitmap(
                bitmap = BitmapFactory.decodeStream(LocalContext.current.assets.open("test_character_sprite.png")),
                contentDescription = "image",
                imageScaler = imageScaler,
                alignment = Alignment.BottomCenter
            )
        }
    }
}

@Composable
fun AnimatedScaledBitmap(
    bitmaps: List<Bitmap?>,
    startIdx: Int = 0,
    contentDescription: String,
    imageScaler: ImageScaler,
    frames: Int = bitmaps.size,
    alignment: Alignment = Alignment.TopStart,
    modifier: Modifier = Modifier,
    msPerFrame: Long = 500) {
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
        ScaledBitmap(
            bitmap = currentFrame,
            contentDescription = contentDescription,
            imageScaler = imageScaler,
            alignment = alignment,
            modifier = modifier
        )
    }
}

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun PreviewAnimatedScaledBitmap() {
    val imageScaler = ImageScaler.getContextImageScaler(LocalContext.current)
    val bitmaps = listOf(
        BitmapFactory.decodeStream(LocalContext.current.assets.open("test_character_sprite.png")),
        BitmapFactory.decodeStream(LocalContext.current.assets.open("test_character_sprite_2.png"))

    )
    VitalBox(imageScaler) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            AnimatedScaledBitmap(
                bitmaps = bitmaps,
                contentDescription = "animated",
                imageScaler = imageScaler,
                alignment = Alignment.BottomCenter
            )
        }
    }
}