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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AdventureScreenControllerImpl(
    override val backgroundHeight: Dp,
    override val vitalBoxFactory: VitalBoxFactory,
    override val bitmapScaler: BitmapScaler,
    private val firmwareManager: FirmwareManager,
    private val adventureService: AdventureService,
    private val adventureActivityLauncher: AdventureActivityLauncher,
    characterFlow: Flow<VBCharacter?>,
    private val context: Context,
    private val coroutineScope: CoroutineScope,
) : AdventureScreenController {
    override val firmware: Firmware
        get() = firmwareManager.getFirmware().value!!
    override val zoneCompleted: StateFlow<Boolean>
        get() = adventureService.activeAdventure!!.zoneCompleted
    override val stepsToGoal: StateFlow<Int>
        get() = adventureService.activeAdventure!!.dailySteps.map {
            adventureService.activeAdventure!!.stepsTowardsGoal()
        }.stateIn(coroutineScope, SharingStarted.Lazily, 0)
    override val goal: StateFlow<Int>
        get() = adventureService.activeAdventure!!.goal
    override val adventureBackground: StateFlow<Bitmap>
        get() = adventureService.activeAdventure!!.currentBackground
    override val partnerWalkingSprites = characterFlow.map {
            it?.characterSprites?.sprites?.subList(CharacterSprites.WALK_1, CharacterSprites.WALK_2+1)?: emptyList()
        }.stateIn(coroutineScope, SharingStarted.Lazily, emptyList())

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