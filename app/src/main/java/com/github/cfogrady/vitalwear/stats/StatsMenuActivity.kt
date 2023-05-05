package com.github.cfogrady.vitalwear.stats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.BackgroundManager
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.composable.util.formatNumber
import java.time.LocalDateTime

class StatsMenuActivity : ComponentActivity() {

    companion object {
        const val TAG = "StatsMenuActivity"
    }

    lateinit var characterManager: CharacterManager
    lateinit var backgroundManager: BackgroundManager
    lateinit var bitmapScaler: BitmapScaler
    lateinit var vitalBoxFactory: VitalBoxFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        characterManager = (application as VitalWearApp).characterManager
        backgroundManager = (application as VitalWearApp).backgroundManager
        bitmapScaler = (application as VitalWearApp).bitmapScaler
        vitalBoxFactory = (application as VitalWearApp).vitalBoxFactory
        setContent {
            statsMenu()
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun statsMenu() {
        val scrollingNameFactory = remember { (application as VitalWearApp).scrollingNameFactory }
        LaunchedEffect(true) {
            characterManager.getLiveCharacter().value!!.characterStats.updateTimeStamps(LocalDateTime.now())
        }
        val background = remember { backgroundManager.selectedBackground.value!! }
        val partner = remember { characterManager.getLiveCharacter().value!! }
        var state = remember {PagerState(0)}
        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(
                bitmap = background,
                contentDescription = "Background",
                alignment = Alignment.BottomCenter
            )
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
                if(state.currentPage < 5) {
                    scrollingNameFactory.ScrollingName(name = partner.sprites[0])
                    bitmapScaler.ScaledBitmap(bitmap = partner.sprites[1], contentDescription = "Partner")
                }
                VerticalPager(state = state, pageCount = 4) { page ->
                    when (page) {
                        0 -> {
                            LimitAndRank(partner)
                        }
                        1 -> {
                            PhaseAndAttribute(partner)
                        }
                        2 -> {
                            Stats(partner)
                        }
                        3 -> {
                            TransformationFulfilment(partner)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LimitAndRank(partner: BEMCharacter) {
        val (timeRemaining, timeUnit) = timeRemaining(partner)
        val displayTimeRemaining = formatNumber(timeRemaining.toInt(), 2)
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.padding(top = 5.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Start) {
                Text("LIMIT", fontSize = 2.em, fontStyle = FontStyle.Italic)
                Text(text = displayTimeRemaining, fontWeight = FontWeight.Bold, fontSize = 3.em, fontStyle = FontStyle.Italic)
                Text(timeUnit)
            }
            Row(modifier = Modifier.padding(top = 5.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Start) {
                Text("RANK")
            }
        }
    }

    @Composable
    private fun PhaseAndAttribute(partner: BEMCharacter) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PHASE ${partner.speciesStats.stage+1}", fontSize = 2.em, fontStyle = FontStyle.Italic)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
                val attribute = when(partner.speciesStats.attribute) {
                    0 -> "NONE"
                    1 -> "VIRUS"
                    2 -> "DATA"
                    3 -> "VACCINE"
                    4 -> "FREE"
                    else -> "UNKNOWN"
                }
                Text(text = attribute)
            }
        }
    }

    @Composable
    private fun Stats(partner: BEMCharacter) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("BP", fontSize = 1.5.em)
                Text(formatNumber(partner.speciesStats.dp, 4), fontSize = 1.5.em)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("   ", fontSize = 1.5.em)
                Text(formatNumber(partner.characterStats.trainedBp, 4), color = Color.Yellow, fontSize = 1.5.em)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("HP", fontSize = 1.5.em)
                Text(formatNumber(partner.speciesStats.hp, 4), fontSize = 1.5.em)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("   ", fontSize = 1.5.em)
                Text(formatNumber(partner.characterStats.trainedHp, 4), color = Color.Yellow, fontSize = 1.5.em)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("AP", fontSize = 1.5.em)
                Text(formatNumber(partner.speciesStats.ap, 4), fontSize = 1.5.em)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("   ", fontSize = 1.5.em)
                Text(formatNumber(partner.characterStats.trainedAp, 4), color = Color.Yellow, fontSize = 1.5.em)
            }
        }
    }

    @Composable
    fun TransformationFulfilment(partner: BEMCharacter) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("Vitals", fontSize = 1.5.em)
                Text(formatNumber(partner.characterStats.vitals, 4), fontSize = 1.5.em)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("Battles", fontSize = 1.5.em)
                Text(formatNumber(partner.characterStats.currentPhaseBattles, 4), fontSize = 1.5.em)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("Win Ratio", fontSize = 1.5.em)
                Text("${partner.characterStats.currentPhaseWinRatio()}%", fontSize = 1.5.em)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("PP", fontSize = 1.5.em)
                Text(formatNumber(partner.characterStats.trainedPP, 3), fontSize = 1.5.em)
            }
        }
    }



    private fun timeRemaining(partner: BEMCharacter) : Pair<Long, String> {
        val timeRemainingMinutes = partner.characterStats.trainingTimeRemainingInSeconds/60
        if(timeRemainingMinutes < 0) {
            // this really shouldn't happen
            return Pair(0, "m")
        } else if(timeRemainingMinutes <= 60) {
            return Pair(timeRemainingMinutes, "m")
        }
        return Pair(timeRemainingMinutes/60, "h")
    }
}