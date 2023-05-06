package com.github.cfogrady.vitalwear.battle.composable

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.BackgroundManager
import com.github.cfogrady.vitalwear.battle.data.PostBattleModel
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.formatNumber
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import java.lang.Math.abs

class EndFightVitalsFactory(
    private val bitmapScaler: BitmapScaler,
    private val firmwareManager: FirmwareManager,
    private val backgroundManager: BackgroundManager,
    private val backgroundHeight: Dp
) {
    @Composable
    fun EndFightVitals(postBattleModel: PostBattleModel, onComplete: () -> Unit) {
        val background = remember { backgroundManager.selectedBackground.value!! }
        val vitalsFilledSprites = remember { firmwareManager.getFirmware().value!!.battleFirmwareSprites.vitalsRangeIcons }
        val idx = remember {getIdx(postBattleModel.vitalChange + postBattleModel.partnerCharacter.vitals(), 9999, vitalsFilledSprites.size)}
        remember {
            Handler(Looper.getMainLooper()!!).postDelayed({
                onComplete.invoke()
            }, 1000)
        }
        bitmapScaler.ScaledBitmap(bitmap = background, contentDescription = "Background")
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(0.dp), modifier = Modifier.fillMaxWidth()) {
            EndFightVitalsText(text = formatNumber(postBattleModel.partnerCharacter.vitals(), 4))
            bitmapScaler.ScaledBitmap(bitmap = vitalsFilledSprites[idx], contentDescription = "Vital Range", modifier = Modifier.padding(top = backgroundHeight.times(.05f)))
            EndFightVitalsText(text = if(postBattleModel.vitalChange < 0) "-" else "+", color = Color.Yellow)
            EndFightVitalsText(text = formatNumber(abs(postBattleModel.vitalChange), 4), color = Color.Yellow)
        }
    }

    fun getIdx(vitals: Int, max: Int, options: Int): Int {
        return (options * vitals) / max
    }

    @Composable
    fun EndFightVitalsText(text: String, color: Color = Color.White) {
        Text(text, fontSize = 3.em, fontWeight = FontWeight.Bold, color = color)
    }
}