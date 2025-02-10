package com.github.cfogrady.vitalwear.adventure

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.battle.BattleActivity
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.util.flow.mapState
import kotlinx.coroutines.flow.StateFlow

class AdventureScreenControllerImpl(
    override val backgroundHeight: Dp,
    override val vitalBoxFactory: VitalBoxFactory,
    override val bitmapScaler: BitmapScaler,
    private val firmwareManager: FirmwareManager,
    private val adventureService: AdventureService,
    private val adventureActivityLauncher: AdventureActivityLauncher,
    private val characterFlow: StateFlow<VBCharacter?>,
    private val context: Context,
) : AdventureScreenController {
    override val firmware: Firmware
        get() = firmwareManager.getFirmware().value!!
    override val zoneCompleted: StateFlow<Boolean>
        get() = adventureService.activeAdventure!!.zoneCompleted
    override val stepsToGoal: StateFlow<Int>
        get() = adventureService.activeAdventure!!.dailySteps.mapState {
            adventureService.activeAdventure!!.stepsTowardsGoal(it)
        }
    override val goal: StateFlow<Int>
        get() = adventureService.activeAdventure!!.goal
    override val adventureBackground: StateFlow<Bitmap>
        get() = adventureService.activeAdventure!!.currentBackground
    override val partnerWalkingSprites
        get() = characterFlow.mapState {
            it?.characterSprites?.sprites?.subList(CharacterSprites.WALK_1, CharacterSprites.WALK_2+1)?: emptyList()
        }

    override fun stopAdventure() {
        adventureService.stopAdventure(context)
    }

    override fun launchBattle() {
        adventureActivityLauncher.launchBattle {
            val adventureEntity = adventureService.activeAdventure!!.currentAdventureEntity()
            it.putExtra(BattleActivity.BATTLE_CHARACTER_INFO, adventureEntity.toBattleCharacterInfo().toByteArray())
        }
    }
}