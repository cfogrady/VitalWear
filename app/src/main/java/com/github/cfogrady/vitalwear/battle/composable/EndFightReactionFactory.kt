package com.github.cfogrady.vitalwear.battle.composable

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.battle.data.BattleResult
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.PositionOffsetRatios
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.google.common.collect.Lists

class EndFightReactionFactory(private val bitmapScaler: BitmapScaler, private val firmwareManager: FirmwareManager, private val characterManager: CharacterManager, private val backgroundHeight: Dp) {
    @Composable
    fun EndFightReaction(battleResult: BattleResult, background: Bitmap, finished: () -> Unit) {
        val characterBitmaps = remember { characterBitmaps(battleResult) }
        val emoteBitmaps = remember { emoteBitmaps(battleResult) }
        Handler(Looper.getMainLooper()!!).postDelayed({
            finished.invoke()
        }, 2000)
        bitmapScaler.ScaledBitmap(bitmap = background, contentDescription = "Background")
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

    private fun characterBitmaps(battleResult: BattleResult): List<Bitmap> {
        val character = characterManager.getLiveCharacter().value!!
        val sprites = character.characterSprites.sprites
        return when(battleResult) {
            BattleResult.WIN -> {
                Lists.newArrayList(sprites[CharacterSprites.IDLE_1], sprites[CharacterSprites.WIN])
            }
            BattleResult.LOSE -> {
                Lists.newArrayList(sprites[CharacterSprites.IDLE_1], sprites[CharacterSprites.DOWN])
            }
            BattleResult.INJURED -> {
                Lists.newArrayList(sprites[CharacterSprites.IDLE_1], sprites[CharacterSprites.DOWN])
            }
            BattleResult.RETREAT -> {
                Lists.newArrayList(sprites[CharacterSprites.RUN_1], sprites[CharacterSprites.RUN_2])
            }
        }
    }

    fun emoteBitmaps(battleResult: BattleResult): List<Bitmap> {
        val firmware = firmwareManager.getFirmware().value!!.emoteFirmwareSprites
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