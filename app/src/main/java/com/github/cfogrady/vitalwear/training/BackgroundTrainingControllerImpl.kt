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
import com.github.cfogrady.vitalwear.util.flow.filterState
import com.github.cfogrady.vitalwear.util.flow.transformState
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class BackgroundTrainingControllerImpl(
    override val backgroundHeight: Dp,
    override val vitalBoxFactory: VitalBoxFactory,
    override val bitmapScaler: BitmapScaler,
    private val backgroundManager: BackgroundManager,
    private val firmwareManager: FirmwareManager,
    private val trainingService: TrainingService,
    private val characterManager: CharacterManager): BackgroundTrainingController {
    override val background: StateFlow<Bitmap> // create our flow on the get because the initial state needs to be non-null
        get() = backgroundManager.selectedBackground
        .filterState { it != null }
        .transformState(initialValue = backgroundManager.selectedBackground.value!!) { emit(it!!) }
    override val firmware: Firmware
        get() = firmwareManager.getFirmware().value!!
    override val backgroundTrainingProgress: StateFlow<Float>
        get() = trainingService.backgroundTrainingProgressTracker!!.progressFlow()
    override val partnerTrainingSprites: StateFlow<List<Bitmap>>
        get() = characterManager.getCharacterFlow()
            .transformState(initialValue = GameState.TRAINING.bitmaps(characterManager.getCurrentCharacter()!!.characterSprites.sprites)) {
                if(it != null) {
                    emit(GameState.TRAINING.bitmaps(it.characterSprites.sprites))
                }
            }
}