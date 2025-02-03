package com.github.cfogrady.vitalwear.character

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.character.data.CharacterFirmwareSprites
import com.github.cfogrady.vitalwear.common.card.CardSpriteLoader
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.ImageScaler
import com.github.cfogrady.vitalwear.firmware.Firmware
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

interface PartnerScreenController {
    val backgroundHeight: Dp
    val bitmapScaler: BitmapScaler
    val characterFirmwareSprites: CharacterFirmwareSprites
    val dailyStepCount: StateFlow<Int>
    val characterBitmaps: StateFlow<List<Bitmap>>
    fun getEmoteBitmaps(coroutineScope: CoroutineScope): StateFlow<List<Bitmap?>>
    fun getVitalsFlow(coroutineScope: CoroutineScope): StateFlow<Int>
    fun getTimeFlow(coroutineScope: CoroutineScope): StateFlow<LocalDateTime>

    class EmptyController(
        context: Context,
        imageScaler: ImageScaler = ImageScaler.getContextImageScaler(context),
        override val backgroundHeight: Dp = imageScaler.calculateBackgroundHeight(),
        override val bitmapScaler: BitmapScaler = BitmapScaler(imageScaler),
        val firmware: Firmware = Firmware.loadPreviewFirmwareFromDisk(context),
        override val characterFirmwareSprites: CharacterFirmwareSprites = firmware.characterFirmwareSprites,
        override val dailyStepCount: StateFlow<Int> = MutableStateFlow(8674),
        override val characterBitmaps: StateFlow<List<Bitmap>> = MutableStateFlow(CardSpriteLoader.loadTestCharacterSprites(context, 3).sprites.subList(CharacterSprites.IDLE_1, CharacterSprites.IDLE_2+1)),
        private val emoteBitmaps: StateFlow<List<Bitmap>> = MutableStateFlow(firmware.characterFirmwareSprites.emoteFirmwareSprites.happyEmote),
        private val vitals: StateFlow<Int> = MutableStateFlow(3784),
    ): PartnerScreenController {

        override fun getEmoteBitmaps(coroutineScope: CoroutineScope): StateFlow<List<Bitmap?>> {
            return emoteBitmaps
        }

        override fun getVitalsFlow(coroutineScope: CoroutineScope): StateFlow<Int> {
            return vitals
        }

        override fun getTimeFlow(coroutineScope: CoroutineScope): StateFlow<LocalDateTime> {
            return MutableStateFlow(LocalDateTime.now())
        }
    }
}