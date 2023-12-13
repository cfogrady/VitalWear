package com.github.cfogrady.vitalwear.battle

import android.graphics.Bitmap

interface BattleSpriteLoader {
    fun getBackground(): Bitmap
    fun getReadyIcon(): Bitmap
    fun getGoIcon(): Bitmap
}