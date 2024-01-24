package com.github.cfogrady.vitalwear.battle

import android.graphics.Bitmap

interface BattleSpriteLoader {
    suspend fun getBackground(): Bitmap
    suspend fun getReadyIcon(): Bitmap
    suspend fun getGoIcon(): Bitmap
}