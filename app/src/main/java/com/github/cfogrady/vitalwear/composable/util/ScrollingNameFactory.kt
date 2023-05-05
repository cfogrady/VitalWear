package com.github.cfogrady.vitalwear.composable.util

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ScrollingNameFactory(private val backgroundHeight: Dp, private val bitmapScaler: BitmapScaler) {
    @Composable
    fun ScrollingName(name: Bitmap) {
        val scaledNameWidth = remember {bitmapScaler.scaledDimension(name.width)}
        val nameScroll = remember { Animatable(0f) }
        var running by remember { mutableStateOf(true) }
        LaunchedEffect(running) {
            nameScroll.animateTo(1f, tween(2000 * name.width / 80 , easing = LinearEasing))
        }
        // only do this when animation running state changes
        LaunchedEffect(key1 = nameScroll.isRunning) {
            //only do this if we aren't running and we aren't at the start state
            if(!nameScroll.isRunning && nameScroll.value > 0f) {
                running = false
                Handler(Looper.getMainLooper()!!).postDelayed({
                    GlobalScope.launch {
                        nameScroll.snapTo(0f)
                        running = true
                    }
                }, 500)
            }
        }
        val offset = backgroundHeight.div(2)
        val distance = backgroundHeight.div(2).plus(scaledNameWidth)
        Box(modifier = Modifier.horizontalScroll(ScrollState(0)), contentAlignment = Alignment.TopStart) {
            bitmapScaler.ScaledBitmap(bitmap = name, contentDescription = "Name", modifier = Modifier.offset(x = offset - distance.times(nameScroll.value)))
        }
    }
}