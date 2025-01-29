package com.github.cfogrady.vitalwear.adventure

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.activity.ImageScaler
import com.github.cfogrady.vitalwear.common.card.CardSpriteLoader
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.firmware.Firmware
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface AdventureScreenController {

    companion object {
        fun buildAdventureScreenController(vitalWearApp: VitalWearApp, context: Context, adventureActivityLauncher: AdventureActivityLauncher, coroutineScope: CoroutineScope): AdventureScreenController {
            return AdventureScreenControllerImpl(
                backgroundHeight = vitalWearApp.backgroundHeight,
                vitalBoxFactory = vitalWearApp.vitalBoxFactory,
                bitmapScaler = vitalWearApp.bitmapScaler,
                firmwareManager = vitalWearApp.firmwareManager,
                adventureService = vitalWearApp.adventureService,
                adventureActivityLauncher = adventureActivityLauncher,
                characterFlow = vitalWearApp.characterManager.getCharacterFlow(),
                context = context,
                coroutineScope = coroutineScope
            )
        }
    }

    class EmptyController(
        context: Context,
        override val firmware: Firmware = Firmware.loadPreviewFirmwareFromDisk(context),
        characterSprites: CharacterSprites = CardSpriteLoader.loadTestCharacterSprites(context, 2),
        background: Bitmap = BitmapFactory.decodeStream(context.assets.open("test_background.png")),
        override val stepsToGoal: StateFlow<Int> = MutableStateFlow(422),
        override val goal: StateFlow<Int> = MutableStateFlow(500),
        imageScaler: ImageScaler = ImageScaler.getContextImageScaler(context),
        override val vitalBoxFactory: VitalBoxFactory = VitalBoxFactory(imageScaler),
        override val bitmapScaler: BitmapScaler = BitmapScaler(imageScaler),
        override val backgroundHeight: Dp = imageScaler.calculateBackgroundHeight()
    ): AdventureScreenController {
        override val zoneCompleted: StateFlow<Boolean> = MutableStateFlow(false)
        override val adventureBackground = MutableStateFlow(background)
        override val partnerWalkingSprites = MutableStateFlow(characterSprites.sprites.subList(CharacterSprites.WALK_1, CharacterSprites.WALK_2+1))
        override fun stopAdventure() {
            TODO("Not yet implemented")
        }

        override fun launchBattle() {
            TODO("Not yet implemented")
        }


    }

    // static
    val vitalBoxFactory: VitalBoxFactory
    val bitmapScaler: BitmapScaler
    val firmware: Firmware
    val backgroundHeight: Dp
    // dynamic
    val zoneCompleted: StateFlow<Boolean>
    val stepsToGoal: StateFlow<Int>
    val goal: StateFlow<Int>
    val adventureBackground: StateFlow<Bitmap>
    val partnerWalkingSprites: StateFlow<List<Bitmap>>

    // logic
    fun stopAdventure()
    fun launchBattle()
}