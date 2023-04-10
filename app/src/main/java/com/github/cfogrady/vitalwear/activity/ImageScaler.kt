package com.github.cfogrady.vitalwear.activity

import android.util.DisplayMetrics
import android.util.Log
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class ImageScaler(val displayMetrics: DisplayMetrics, val screenIsRound: Boolean) {
    companion object {
        const val VB_HEIGHT = 160f
        private const val TAG = "ImageScaler"
        private val SQRT_OF_TWO = Math.sqrt(2.0)
    }

    private var scale = 0f
    private var padding = 0
    private var paddingDp = 0.dp

    fun getScaling(): Float {
        if(scale == 0f) {
            val height = displayMetrics.heightPixels
            if(!screenIsRound) {
                scale = height / VB_HEIGHT
            } else {
                val workableHeight = height - getPaddingPixels()*2
                scale = workableHeight / VB_HEIGHT
            }
        }
        return scale
    }

    fun getPadding(): Dp {
        if(paddingDp == 0.dp) {
            Log.i(TAG, "Converting Pixels to Dp")
            paddingDp = convertPixelsToDp(getPaddingPixels())
        }
        return paddingDp
    }

    private fun getPaddingPixels(): Int {
        if(padding != 0 || !screenIsRound) {
            return padding
        }
        Log.i(TAG, "Calculating padding")
        val radius = calculateRadius()
        val halfWidth = calculateSquareHalfWidth(radius)
        Log.i(TAG, "Radius: $radius, HalfWidth: $halfWidth")
        padding = radius - halfWidth
        return padding
    }

    private fun calculateSquareHalfWidth(radius: Int) : Int {
        // Pythagorean Theorem where both sides are equal and the radius is the hypotenuse
        return Math.floor(radius/ SQRT_OF_TWO).toInt()
    }

    private fun calculateRadius(): Int {
        return displayMetrics.heightPixels/2
    }

    fun scaledDpValueFromPixels(pixels: Int): Dp {
        return convertPixelsToDp((pixels * getScaling()).toInt())
    }

    fun convertPixelsToDp(px: Int): Dp {
        return (px / (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).dp
    }
}