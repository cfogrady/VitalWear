package com.github.cfogrady.vitalwear.battle.composable

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.battle.data.BattleModel
import com.github.cfogrady.vitalwear.battle.data.BattleResult
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.PositionOffsetRatios
import com.github.cfogrady.vitalwear.data.FirmwareManager
import com.google.common.collect.Lists

class EndFightReactionFactory(private val bitmapScaler: BitmapScaler, private val firmwareManager: FirmwareManager, private val backgroundHeight: Dp) {
    @Composable
    fun EndFightReaction(battleModel: BattleModel, battleResult: BattleResult, finished: () -> Unit) {
        val characterBitmaps = remember { characterBitmaps(battleResult, battleModel.partnerCharacter) }
        val emoteBitmaps = remember { emoteBitmaps(battleResult) }
        Handler(Looper.getMainLooper()!!).postDelayed({
            finished.invoke()
        }, 2000)
        bitmapScaler.ScaledBitmap(bitmap = battleModel.background, contentDescription = "Background")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().offset(y = backgroundHeight.times(PositionOffsetRatios.CHARACTER_OFFSET_FROM_BOTTOM))) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                    if(emoteBitmaps.size > 1) {
                        bitmapScaler.AnimatedScaledBitmap(
                            bitmaps = emoteBitmaps,
                            startIdx = 0,
                            frames = emoteBitmaps.size,
                            contentDescription = "Emote"
                        )
                    } else {
                        var showEmote by remember { mutableStateOf(true) }
                        Handler(Looper.getMainLooper()!!).postDelayed({
                            showEmote = !showEmote
                        }, 500)
                        if(showEmote) {
                            bitmapScaler.ScaledBitmap(bitmap = emoteBitmaps[0], contentDescription = "Emote")
                        }
                    }
                }
                bitmapScaler.AnimatedScaledBitmap(
                    bitmaps = characterBitmaps,
                    startIdx = 0,
                    frames = characterBitmaps.size,
                    contentDescription = "Character"
                )
            }
        }
    }

    fun characterBitmaps(battleResult: BattleResult, character: BEMCharacter): List<Bitmap> {
        return when(battleResult) {
            BattleResult.WIN -> {
                Lists.newArrayList(character.sprites[1], character.sprites[9])
            }
            BattleResult.LOSE -> {
                Lists.newArrayList(character.sprites[1], character.sprites[10])
            }
            BattleResult.INJURED -> {
                Lists.newArrayList(character.sprites[1], character.sprites[10])
            }
            BattleResult.RETREAT -> {
                Lists.newArrayList(character.sprites[5], character.sprites[6])
            }
        }
    }

    fun emoteBitmaps(battleResult: BattleResult): List<Bitmap> {
        val firmware = firmwareManager.getFirmware().value!!
        return when(battleResult) {
            BattleResult.WIN -> {
                firmware.happyEmote
            }
            BattleResult.LOSE -> {
                firmware.loseEmote
            }
            BattleResult.INJURED -> {
                firmware.injuredEmote
            }
            BattleResult.RETREAT -> {
                Lists.newArrayList(firmware.sweatEmote)
            }
        }
    }
}