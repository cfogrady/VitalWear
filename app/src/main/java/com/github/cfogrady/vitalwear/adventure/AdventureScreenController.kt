package com.github.cfogrady.vitalwear.adventure

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.activity.ImageScaler
import com.github.cfogrady.vitalwear.common.card.CardSpriteLoader
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.firmware.Firmware
import kotlinx.coroutines.flow.StateFlow

interface AdventureScreenController {

    companion object {
        fun buildAdventureScreenController(): AdventureScreenController {

        }
    }

    class EmptyController(
        context: Context,
        override val firmware: Firmware = Firmware.loadPreviewFirmwareFromDisk(context),
        characterSprites: CharacterSprites = CardSpriteLoader.loadTestCharacterSprites(context, 2),
        adventureBackground: Bitmap = BitmapFactory.decodeStream(context.assets.open("test_background.png")),
        override val adventureActivityLauncher: AdventureActivityLauncher = AdventureActivityLauncher(),
        imageScaler: ImageScaler = ImageScaler.getContextImageScaler(context),
        override val vitalBoxFactory: VitalBoxFactory = VitalBoxFactory(imageScaler),
        override val bitmapScaler: BitmapScaler = BitmapScaler(imageScaler),
    ): AdventureScreenController {
        override fun getActiveAdventure(): ActiveAdventure {
            return ActiveAdventure()
        }

        override fun stepsToGoal(): StateFlow<Int> {
            TODO("Not yet implemented")
        }

        override fun stopAdventure() {
            TODO("Not yet implemented")
        }

    }

    // static
    val vitalBoxFactory: VitalBoxFactory
    val bitmapScaler: BitmapScaler
    val adventureActivityLauncher: AdventureActivityLauncher
    val firmware: Firmware
    // dynamic
    val zoneCompleted: StateFlow<Boolean>
    val adventureBackground: StateFlow<Bitmap>
    fun stepsToGoal(): StateFlow<Int>

    // logic
    fun stopAdventure()
}