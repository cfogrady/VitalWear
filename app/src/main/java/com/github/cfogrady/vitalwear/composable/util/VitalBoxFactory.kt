package com.github.cfogrady.vitalwear.composable.util

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import com.github.cfogrady.vitalwear.activity.ImageScaler

class VitalBoxFactory(private val imageScaler: ImageScaler, private val width: Int, private val height: Int) {
    @Composable
    fun VitalBox(content: @Composable BoxScope.()-> Unit) {
        val scaledWidth = imageScaler.scaledDpValueFromPixels(width)
        val scaledHeight = imageScaler.scaledDpValueFromPixels(height)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.clipToBounds().width(scaledWidth).height(scaledHeight), content=content)
        }
    }
}