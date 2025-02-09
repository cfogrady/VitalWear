package com.github.cfogrady.vitalwear.main

import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.SaveService
import com.github.cfogrady.vitalwear.background.BackgroundManager
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.PartnerScreenController
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.character.transformation.ExpectedTransformation
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.util.flow.mapState
import com.github.cfogrady.vitalwear.util.flow.transformState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll

class MainScreenControllerImpl(
    override val bitmapScaler: BitmapScaler,
    override val vitalBoxFactory: VitalBoxFactory,
    override val partnerScreenController: PartnerScreenController,
    private val firmwareManager: FirmwareManager,
    private val characterManager: CharacterManager,
    private val activityLaunchers: ActivityLaunchers,
    private val saveService: SaveService,
    private val backgroundManager: BackgroundManager,
) : MainScreenController {
    override val menuFirmwareSprites: MenuFirmwareSprites
        get() = firmwareManager.getFirmware().value!!.menuFirmwareSprites
    override val readyToTransform: StateFlow<Boolean> = characterManager.getCharacterFlow().transformState<VBCharacter?, ExpectedTransformation?>(null) {
        if(it == null) {
            emit(null)
        } else {
            emitAll(
                it.readyToTransform
            )
        }
    }.mapState { it != null }
    override val characterPhase = characterManager.getCharacterFlow().mapState {
        it?.speciesStats?.phase ?: 0
    }

    override val characterIsAsleep: StateFlow<Boolean> = characterManager.getCharacterFlow().mapState {
        it?.characterStats?.sleeping ?: false
    }

    override fun getBackgroundFlow(): StateFlow<Bitmap> {
        return backgroundManager.selectedBackground.transformState(
            initialValue = backgroundManager.selectedBackground.value!!) {
            if(it != null) {
                emit(it)
            }
        }
    }

    override fun launchStatsMenuActivity() {
        activityLaunchers.statsMenuLauncher()
    }

    override fun launchTransformActivity() {
        activityLaunchers.transformLauncher()
    }

    override fun launchCharacterSelectionActivity() {
        activityLaunchers.characterSelectionLauncher()
    }

    override fun launchTrainingMenuActivity() {
        activityLaunchers.trainingMenuLauncher()
    }

    override fun launchAdventureActivity() {
        activityLaunchers.adventureActivityLauncher.launchMenu()
    }

    override fun launchTransferActivity() {
        activityLaunchers.transformLauncher()
    }

    override fun launchBattleActivity() {
        activityLaunchers.battleLauncher()
    }

    override fun launchSettingsActivity() {
        activityLaunchers.settingsActivityLauncher()
    }

    override fun toggleSleep() {
        characterManager.getCurrentCharacter()?.let {
            it.characterStats.sleeping = !it.characterStats.sleeping
            saveService.saveAsync()
        }
    }
}