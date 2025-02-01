package com.github.cfogrady.vitalwear.composable.util

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.tooling.preview.devices.WearDevices


class VitalBoxFactory(
    private val imageScaler: ImageScaler,
    private val width: Int = ImageScaler.VB_WIDTH.toInt(),
    private val height: Int = ImageScaler.VB_HEIGHT.toInt(),
) {

    companion object {
        fun getVitalBoxFactoryFromContext(context: Context): VitalBoxFactory {
            val imageScaler = ImageScaler.getContextImageScaler(context)
            return VitalBoxFactory(imageScaler, ImageScaler.VB_WIDTH.toInt(), ImageScaler.VB_HEIGHT.toInt())
        }
    }

    @Composable
    fun VitalBox(content: @Composable BoxScope.() -> Unit) {
        VitalBox(imageScaler, width, height, content)
    }
}

@Composable
fun VitalBox(
    imageScaler: ImageScaler,
    width: Int = ImageScaler.VB_WIDTH.toInt(),
    height: Int = ImageScaler.VB_HEIGHT.toInt(),
    content: @Composable BoxScope.()-> Unit
) {
    val scaledWidth = imageScaler.scaledDpValueFromPixels(width)
    val scaledHeight = imageScaler.scaledDpValueFromPixels(height)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier
            .clipToBounds()
            .width(scaledWidth)
            .height(scaledHeight), content=content)
    }
}


@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun VitalBoxPreview() {
    val imageScaler = ImageScaler.getContextImageScaler(LocalContext.current)
    val bitmapScaler = BitmapScaler(imageScaler)
    val background = BitmapFactory.decodeStream(LocalContext.current.assets.open("test_background.png"))
    VitalBox(imageScaler) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            bitmapScaler.ScaledBitmap(
                bitmap = background,
                contentDescription = "image",
                alignment = Alignment.Center
            )
        }
    }
}