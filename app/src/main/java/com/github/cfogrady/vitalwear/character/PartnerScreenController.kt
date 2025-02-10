package com.github.cfogrady.vitalwear.character

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.VitalWearApp
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
    val emoteBitmaps: StateFlow<List<Bitmap?>> // nullable because some emotes blink instead of animate. A null indicates a blink frame.
    val vitals: StateFlow<Int>
    fun getCharacterBitmaps(coroutineScope: CoroutineScope): StateFlow<List<Bitmap>>
    fun getTimeFlow(coroutineScope: CoroutineScope): StateFlow<LocalDateTime>

    companion object {
        fun buildPartnerScreenController(vitalWearApp: VitalWearApp): PartnerScreenController {
            return PartnerScreenControllerImpl(
                backgroundHeight = vitalWearApp.backgroundHeight,
                bitmapScaler = vitalWearApp.bitmapScaler,
                firmwareManager = vitalWearApp.firmwareManager,
                characterManager = vitalWearApp.characterManager,
                stepSensorService = vitalWearApp.stepService,
                heartRateService = vitalWearApp.heartRateService,
            )
        }
    }

    class EmptyController(
        context: Context,
        imageScaler: ImageScaler = ImageScaler.getContextImageScaler(context),
        override val backgroundHeight: Dp = imageScaler.calculateBackgroundHeight(),
        override val bitmapScaler: BitmapScaler = BitmapScaler(imageScaler),
        firmware: Firmware = Firmware.loadPreviewFirmwareFromDisk(context),
        override val characterFirmwareSprites: CharacterFirmwareSprites = firmware.characterFirmwareSprites,
        override val dailyStepCount: StateFlow<Int> = MutableStateFlow(8674),
        characterSprites: CharacterSprites = CardSpriteLoader.loadTestCharacterSprites(context, 3),
        private val characterBitmaps: StateFlow<List<Bitmap>> = MutableStateFlow(characterSprites.sprites.subList(CharacterSprites.IDLE_1, CharacterSprites.IDLE_2+1)),
        override val emoteBitmaps: StateFlow<List<Bitmap>> = MutableStateFlow(firmware.characterFirmwareSprites.emoteFirmwareSprites.happyEmote),
        override val vitals: StateFlow<Int> = MutableStateFlow(3784),
    ): PartnerScreenController {

        override fun getCharacterBitmaps(coroutineScope: CoroutineScope): StateFlow<List<Bitmap>> {
            return characterBitmaps
        }

        override fun getTimeFlow(coroutineScope: CoroutineScope): StateFlow<LocalDateTime> {
            return MutableStateFlow(LocalDateTime.now())
        }
    }
}