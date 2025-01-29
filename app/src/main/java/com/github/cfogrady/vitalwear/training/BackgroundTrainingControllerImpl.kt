package com.github.cfogrady.vitalwear.training

import android.graphics.Bitmap
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.background.BackgroundManager
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform

class BackgroundTrainingControllerImpl(
    override val backgroundHeight: Dp,
    override val vitalBoxFactory: VitalBoxFactory,
    override val bitmapScaler: BitmapScaler,
    backgroundManager: BackgroundManager,
    private val firmwareManager: FirmwareManager,
    private val trainingService: TrainingService,
    characterManager: CharacterManager,
    coroutineScope: CoroutineScope): BackgroundTrainingController {
    override val background: StateFlow<Bitmap> = backgroundManager.selectedBackground
        .filter { it != null }
        .transform { emit(it!!) }
        // Test... this may need to be changed to an actual fake background
        .stateIn(coroutineScope, SharingStarted.Lazily, backgroundManager.selectedBackground.value!!)
    override val firmware: Firmware
        get() = firmwareManager.getFirmware().value!!
    override val backgroundTrainingProgress: StateFlow<Float>
        get() = trainingService.backgroundTrainingProgressTracker!!.progressFlow()
    override val partnerTrainingSprites: StateFlow<List<Bitmap>> =
        characterManager.getCharacterFlow()
            .filter { it != null }
            .map { GameState.TRAINING.bitmaps(it!!.characterSprites.sprites) }
            .stateIn(coroutineScope, SharingStarted.Lazily, listOf())
}