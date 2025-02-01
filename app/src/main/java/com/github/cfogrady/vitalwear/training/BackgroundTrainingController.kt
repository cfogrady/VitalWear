package com.github.cfogrady.vitalwear.training

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.composable.util.ImageScaler
import com.github.cfogrady.vitalwear.common.card.CardSpriteLoader
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.firmware.Firmware
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface BackgroundTrainingController {
    companion object {
        fun buildBackgroundTrainingController(vitalWearApp: VitalWearApp, coroutineScope: CoroutineScope): BackgroundTrainingController {
            return BackgroundTrainingControllerImpl(
                backgroundHeight = vitalWearApp.backgroundHeight,
                vitalBoxFactory = vitalWearApp.vitalBoxFactory,
                bitmapScaler = vitalWearApp.bitmapScaler,
                backgroundManager = vitalWearApp.backgroundManager,
                firmwareManager = vitalWearApp.firmwareManager,
                trainingService = vitalWearApp.trainingService,
                characterManager = vitalWearApp.characterManager,
                coroutineScope = coroutineScope,
            )
        }
    }

    class EmptyController(
        context: Context,
        trainingProgress: Float = 0.0f,
        characterSprites: CharacterSprites = CardSpriteLoader.loadTestCharacterSprites(context, 2),
        background: Bitmap = BitmapFactory.decodeStream(context.assets.open("test_background.png")),
        override val firmware: Firmware = Firmware.loadPreviewFirmwareFromDisk(context),
        imageScaler: ImageScaler = ImageScaler.getContextImageScaler(context),
        override val backgroundHeight: Dp = imageScaler.calculateBackgroundHeight(),
        override val vitalBoxFactory: VitalBoxFactory = VitalBoxFactory(imageScaler),
        override val bitmapScaler: BitmapScaler = BitmapScaler(imageScaler),
    ): BackgroundTrainingController {
        override val background: StateFlow<Bitmap> = MutableStateFlow(background)
        override val backgroundTrainingProgress = MutableStateFlow(trainingProgress)
        override val partnerTrainingSprites = MutableStateFlow(GameState.TRAINING.bitmaps(characterSprites.sprites))
    }

    // dynamic
    val background: StateFlow<Bitmap>
    val backgroundTrainingProgress: StateFlow<Float>
    val partnerTrainingSprites: StateFlow<List<Bitmap>>

    // static
    val backgroundHeight: Dp
    val vitalBoxFactory: VitalBoxFactory
    val bitmapScaler: BitmapScaler
    val firmware: Firmware
}