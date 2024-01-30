package com.github.cfogrady.vitalwear.stats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.BackgroundManager
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.character.transformation.TransformationOption
import com.github.cfogrady.vitalwear.character.transformation.TransformationFirmwareSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.common.composable.util.formatNumber
import com.github.cfogrady.vitalwear.firmware.Firmware
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class StatsMenuActivity : ComponentActivity() {

    companion object {
        const val TAG = "StatsMenuActivity"
        const val TRANSFORMATION_ICON_COLUMN_WEIGHT = 0.3f
        const val TRANSFORMATION_VALUE_COLUMN_WEIGHT = 0.7f
        val TRANSFORMATION_ROW_PADDING = 10.dp
        val STAT_SIZE = 1.8.em
    }

    lateinit var characterManager: CharacterManager
    lateinit var backgroundManager: BackgroundManager
    lateinit var bitmapScaler: BitmapScaler
    lateinit var vitalBoxFactory: VitalBoxFactory
    lateinit var firmware: Firmware

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        characterManager = (application as VitalWearApp).characterManager
        backgroundManager = (application as VitalWearApp).backgroundManager
        bitmapScaler = (application as VitalWearApp).bitmapScaler
        vitalBoxFactory = (application as VitalWearApp).vitalBoxFactory
        firmware = (application as VitalWearApp).firmwareManager.getFirmware().value!!
        setContent {
            StatsMenu()
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun StatsMenu() {
        var currentOption by remember { mutableStateOf<TransformationOption?>(null) }
        val partner = remember { characterManager.getCurrentCharacter()!! }
        LaunchedEffect(true) {
            characterManager.getCharacterFlow().value!!.characterStats.updateTimeStamps(LocalDateTime.now())
            withContext(Dispatchers.IO) {
                currentOption = partner.hasValidTransformation()
            }
        }
        val background = remember { backgroundManager.selectedBackground.value!! }

        val initialStatsPage = remember { mutableStateOf(0) }

        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(
                bitmap = background,
                contentDescription = "Background",
                alignment = Alignment.BottomCenter
            )
            val pagerState = rememberPagerState(pageCount = {1 + partner. transformationOptions.size})
            VerticalPager(state = pagerState) {rootPage ->
                when(rootPage) {
                    0 -> {
                        PartnerStats(initialPage = initialStatsPage, partner = partner)
                    }
                    else -> {
                        val potentialOption = partner.transformationOptions[rootPage - 1]
                        val highestCompletedAdventure = partner.cardMeta.maxAdventureCompletion ?: -1
                        PotentialTransformation(
                            firmwareSprites = firmware.transformationFirmwareSprites,
                            bemCharacter = partner,
                            transformationOption = potentialOption,
                            expectedTransformation = currentOption == potentialOption,
                            locked = (potentialOption.requiredAdventureCompleted ?: -1) > highestCompletedAdventure
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun PartnerStats(initialPage: MutableState<Int>, partner: VBCharacter) {
        val scrollingNameFactory = remember { (application as VitalWearApp).scrollingNameFactory }
        val pagerState = rememberPagerState(initialPage = initialPage.value, pageCount = {4})
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
            scrollingNameFactory.ScrollingName(name = partner.characterSprites.sprites[CharacterSprites.NAME])
            bitmapScaler.ScaledBitmap(bitmap = partner.characterSprites.sprites[CharacterSprites.IDLE_1], contentDescription = "Partner")
            VerticalPager(state = pagerState) { page ->
                initialPage.value = page
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
                        TransformationFulfilment(partner, firmware.transformationFirmwareSprites)
                    }
                }
            }
        }
    }

    @Composable
    private fun LimitAndRank(partner: VBCharacter) {
        val (timeRemaining, timeUnit) = timeRemaining(partner)
        val displayTimeRemaining = formatNumber(timeRemaining.toInt(), 2)
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.padding(top = 5.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Start) {
                Text("LIMIT", fontSize = 3.em, fontStyle = FontStyle.Italic)
                Text(text = "$displayTimeRemaining$timeUnit", fontWeight = FontWeight.Bold, fontSize = 3.5.em, fontStyle = FontStyle.Italic)
            }
            Row(modifier = Modifier.padding(top = 3.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Start) {
                Text("RANK", fontSize = 3.em)
            }
        }
    }

    @Composable
    private fun PhaseAndAttribute(partner: VBCharacter) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PHASE ${partner.speciesStats.phase+1}", fontSize = 3.em, fontStyle = FontStyle.Italic)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
                val attribute = when(partner.speciesStats.attribute) {
                    0 -> "NONE"
                    1 -> "VIRUS"
                    2 -> "DATA"
                    3 -> "VACCINE"
                    4 -> "FREE"
                    else -> "UNKNOWN"
                }
                Text(text = attribute, fontSize = 3.em)
            }
        }
    }

    @Composable
    private fun PotentialTransformation(firmwareSprites: TransformationFirmwareSprites, bemCharacter: VBCharacter, transformationOption: TransformationOption, expectedTransformation: Boolean, locked: Boolean) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                if(expectedTransformation) {
                    bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.star, contentDescription = "star")
                    Text(text = "NEXT", fontSize = 2.em, fontStyle = FontStyle.Italic)
                    bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.star, contentDescription = "star")
                } else {
                    Text(text = "NEXT", fontSize = 2.em, fontStyle = FontStyle.Italic)
                }
            }
            if(locked) {
                bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.locked, contentDescription = "Potential Transformation Locked")
            } else {
                bitmapScaler.ScaledBitmap(bitmap = transformationOption.sprite, contentDescription = "Potential Transformation")
            }
            val timeUnit = if(bemCharacter.characterStats.timeUntilNextTransformation >= 60*60) 'h'
            else 'm'
            val timeRemaining = if(timeUnit == 'h') bemCharacter.characterStats.timeUntilNextTransformation/(60*60)
            else bemCharacter.characterStats.timeUntilNextTransformation/60
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TRANSFORMATION_ROW_PADDING), verticalAlignment = Alignment.CenterVertically) {
                bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.hourglass, contentDescription = "time icon", modifier = Modifier.weight(
                    TRANSFORMATION_ICON_COLUMN_WEIGHT))
                Text(text = "${formatNumber(timeRemaining.toInt(), 2)}$timeUnit", textAlign = TextAlign.Right, modifier = Modifier.weight(
                    TRANSFORMATION_VALUE_COLUMN_WEIGHT))
            }
            TransformationStats(
                firmwareSprites,
                transformationOption.requiredVitals,
                transformationOption.requiredBattles,
                transformationOption.requiredWinRatio,
                transformationOption.requiredPp,
                vitalsColor = if(bemCharacter.characterStats.vitals >= transformationOption.requiredVitals) Color.Yellow else Color.White,
                battlesColor = if(bemCharacter.characterStats.currentPhaseBattles >= transformationOption.requiredBattles) Color.Yellow else Color.White,
                winRatioColor = if(bemCharacter.characterStats.currentPhaseWinRatio() >= transformationOption.requiredWinRatio) Color.Yellow else Color.White,
                ppColor = if(bemCharacter.characterStats.trainedPP >= transformationOption.requiredPp) Color.Yellow else Color.White,
                )
        }
    }

    @Composable
    private fun TransformationStats(firmwareSprites: TransformationFirmwareSprites, vitals: Int, battles: Int, winRatio: Int, pp: Int, vitalsColor: Color = Color.White,  battlesColor: Color = Color.White, winRatioColor: Color = Color.White, ppColor: Color = Color.White) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TRANSFORMATION_ROW_PADDING), verticalAlignment = Alignment.CenterVertically) {
            bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.vitalsIcon, contentDescription = "vitals icon", modifier = Modifier.weight(
                TRANSFORMATION_ICON_COLUMN_WEIGHT))
            Text(text = formatNumber(vitals, 4), color = vitalsColor, textAlign = TextAlign.Right, modifier = Modifier.weight(
                TRANSFORMATION_VALUE_COLUMN_WEIGHT))
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TRANSFORMATION_ROW_PADDING), verticalAlignment = Alignment.CenterVertically) {
            bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.battlesIcon, contentDescription = "battles icon", modifier = Modifier.weight(
                TRANSFORMATION_ICON_COLUMN_WEIGHT))
            Text(text = formatNumber(battles, 3), color = battlesColor, textAlign = TextAlign.Right, modifier = Modifier.weight(
                TRANSFORMATION_VALUE_COLUMN_WEIGHT))
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TRANSFORMATION_ROW_PADDING), verticalAlignment = Alignment.CenterVertically) {
            bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.winRatioIcon, contentDescription = "win ratio icon", modifier = Modifier.weight(
                TRANSFORMATION_ICON_COLUMN_WEIGHT))
            Text(text = "${formatNumber(winRatio, 3)}%", color = winRatioColor, textAlign = TextAlign.Right, modifier = Modifier.weight(
                TRANSFORMATION_VALUE_COLUMN_WEIGHT))
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TRANSFORMATION_ROW_PADDING), verticalAlignment = Alignment.CenterVertically) {
            bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.ppIcon, contentDescription = "pp icon", modifier = Modifier.weight(
                TRANSFORMATION_ICON_COLUMN_WEIGHT))
            Row(modifier = Modifier.weight(TRANSFORMATION_VALUE_COLUMN_WEIGHT), horizontalArrangement = Arrangement.End) {
                bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.squatIcon, contentDescription = "squat icon")
                Text(text = formatNumber(pp, 3), color = ppColor, textAlign = TextAlign.Right)
            }
        }
    }

    @Composable
    private fun Stats(partner: VBCharacter) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(15.dp, 0.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("BP", fontSize = STAT_SIZE, modifier = Modifier.weight(.5f), textAlign = TextAlign.Left)
                Text(partner.speciesStats.displayBp(), fontSize = STAT_SIZE, modifier = Modifier.weight(.5f), textAlign = TextAlign.Right)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("", modifier = Modifier.weight(.5f))
                Text(formatNumber(partner.characterStats.trainedBp, 3), color = Color.Yellow, fontSize = STAT_SIZE, modifier = Modifier.weight(.5f), textAlign = TextAlign.Right)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("HP", fontSize = STAT_SIZE, modifier = Modifier.weight(.5f), textAlign = TextAlign.Left)
                Text(partner.speciesStats.displayHp(), fontSize = STAT_SIZE, modifier = Modifier.weight(.5f), textAlign = TextAlign.Right)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("", modifier = Modifier.weight(.5f))
                Text(formatNumber(partner.characterStats.trainedHp, 3), color = Color.Yellow, fontSize = STAT_SIZE, modifier = Modifier.weight(.5f), textAlign = TextAlign.Right)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("AP", fontSize = STAT_SIZE, modifier = Modifier.weight(.5f))
                Text(partner.speciesStats.displayAp(), fontSize = STAT_SIZE, modifier = Modifier.weight(.5f), textAlign = TextAlign.Right)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("", modifier = Modifier.weight(.5f))
                Text(formatNumber(partner.characterStats.trainedAp, 3), color = Color.Yellow, fontSize = STAT_SIZE, modifier = Modifier.weight(.5f), textAlign = TextAlign.Right)
            }
        }
    }

    @Composable
    fun TransformationFulfilment(partner: VBCharacter, firmwareSprites: TransformationFirmwareSprites) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TransformationStats(firmwareSprites = firmwareSprites, vitals = partner.characterStats.vitals, battles = partner.characterStats.currentPhaseBattles, winRatio = partner.characterStats.currentPhaseWinRatio(), pp = partner.characterStats.trainedPP)
        }
    }



    private fun timeRemaining(partner: VBCharacter) : Pair<Long, String> {
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