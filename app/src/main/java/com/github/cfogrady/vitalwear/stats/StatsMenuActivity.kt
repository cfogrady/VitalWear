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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.BackgroundManager
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.character.transformation.TransformationOption
import com.github.cfogrady.vitalwear.character.transformation.TransformationFirmwareSprites
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.common.composable.util.formatNumber
import com.github.cfogrady.vitalwear.firmware.Firmware
import java.time.LocalDateTime

class StatsMenuActivity : ComponentActivity() {

    companion object {
        const val TAG = "StatsMenuActivity"
        const val TRANSFORMATION_ICON_COLUMN_WEIGHT = 0.3f
        const val TRANSFORMATION_VALUE_COLUMN_WEIGHT = 0.7f
        val TRANSFORMATION_ROW_PADDING = 10.dp
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
        val adventureEntityDao = (application as VitalWearApp).database.adventureEntityDao()
        setContent {
            var highestCompletedAdventure by remember { mutableStateOf<Int?>(null) }
            LaunchedEffect(true) {
                val adventures = adventureEntityDao.getByCard(characterManager.getCurrentCharacter()!!.cardName())
                highestCompletedAdventure = AdventureEntity.highestAdventureCompleted(adventures)
            }
            StatsMenu(highestCompletedAdventure)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun StatsMenu(highestCompletedAdventure: Int?) {
        LaunchedEffect(true) {
            characterManager.getCharacterFlow().value!!.characterStats.updateTimeStamps(LocalDateTime.now())
        }
        val background = remember { backgroundManager.selectedBackground.value!! }
        val partner = remember { characterManager.getCharacterFlow().value!! }
        val initialStatsPage = remember { mutableStateOf(0) }

        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(
                bitmap = background,
                contentDescription = "Background",
                alignment = Alignment.BottomCenter
            )
            VerticalPager(pageCount = 1 + partner.transformationOptions.size) {rootPage ->
                when(rootPage) {
                    0 -> {
                        PartnerStats(initialPage = initialStatsPage, partner = partner)
                    }
                    else -> {
                        val potentialOption = partner.transformationOptions[rootPage - 1]
                        PotentialTransformation(
                            firmwareSprites = firmware.transformationFirmwareSprites,
                            bemCharacter = partner,
                            transformationOption = potentialOption,
                            expectedTransformation = false,
                            locked = (potentialOption.requiredAdventureCompleted ?: -1) > (highestCompletedAdventure ?: -1)
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun PartnerStats(initialPage: MutableState<Int>, partner: BEMCharacter) {
        val scrollingNameFactory = remember { (application as VitalWearApp).scrollingNameFactory }
        var state = remember {PagerState(initialPage.value)}
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
            scrollingNameFactory.ScrollingName(name = partner.characterSprites.sprites[CharacterSprites.NAME])
            bitmapScaler.ScaledBitmap(bitmap = partner.characterSprites.sprites[CharacterSprites.IDLE_1], contentDescription = "Partner")
            VerticalPager(state = state, pageCount = 4) { page ->
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
                        TransformationFulfilment(partner)
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
            Text("PHASE ${partner.speciesStats.phase+1}", fontSize = 2.em, fontStyle = FontStyle.Italic)
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
    private fun PotentialTransformation(firmwareSprites: TransformationFirmwareSprites, bemCharacter: BEMCharacter, transformationOption: TransformationOption, expectedTransformation: Boolean, locked: Boolean) {
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
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TRANSFORMATION_ROW_PADDING), verticalAlignment = Alignment.CenterVertically) {
                //TODO: Add minutes (m)
                bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.hourglass, contentDescription = "time icon", modifier = Modifier.weight(
                    TRANSFORMATION_ICON_COLUMN_WEIGHT))
                Text(text = "${formatNumber(bemCharacter.characterStats.timeUntilNextTransformation.toInt()/3600, 2)}h", textAlign = TextAlign.Right, modifier = Modifier.weight(
                    TRANSFORMATION_VALUE_COLUMN_WEIGHT))
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TRANSFORMATION_ROW_PADDING), verticalAlignment = Alignment.CenterVertically) {
                bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.vitalsIcon, contentDescription = "vitals icon", modifier = Modifier.weight(
                    TRANSFORMATION_ICON_COLUMN_WEIGHT))
                Text(text = formatNumber(transformationOption.requiredVitals, 4), textAlign = TextAlign.Right, modifier = Modifier.weight(
                    TRANSFORMATION_VALUE_COLUMN_WEIGHT))
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TRANSFORMATION_ROW_PADDING), verticalAlignment = Alignment.CenterVertically) {
                bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.battlesIcon, contentDescription = "battles icon", modifier = Modifier.weight(
                    TRANSFORMATION_ICON_COLUMN_WEIGHT))
                Text(text = formatNumber(transformationOption.requiredBattles, 3), textAlign = TextAlign.Right, modifier = Modifier.weight(
                    TRANSFORMATION_VALUE_COLUMN_WEIGHT))
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TRANSFORMATION_ROW_PADDING), verticalAlignment = Alignment.CenterVertically) {
                bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.winRatioIcon, contentDescription = "win ratio icon", modifier = Modifier.weight(
                    TRANSFORMATION_ICON_COLUMN_WEIGHT))
                Text(text = "${formatNumber(transformationOption.requiredWinRatio, 3)}%", textAlign = TextAlign.Right, modifier = Modifier.weight(
                    TRANSFORMATION_VALUE_COLUMN_WEIGHT))
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TRANSFORMATION_ROW_PADDING), verticalAlignment = Alignment.CenterVertically) {
                bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.ppIcon, contentDescription = "pp icon", modifier = Modifier.weight(
                    TRANSFORMATION_ICON_COLUMN_WEIGHT))
                Row(modifier = Modifier.weight(TRANSFORMATION_VALUE_COLUMN_WEIGHT), horizontalArrangement = Arrangement.End) {
                    bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.squatIcon, contentDescription = "squat icon")
                    Text(text = "${formatNumber(transformationOption.requiredPp, 3)}", textAlign = TextAlign.Right)
                }
            }
        }
    }

    @Composable
    private fun Stats(partner: BEMCharacter) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("BP", fontSize = 1.5.em)
                Text(partner.speciesStats.displayBp(), fontSize = 1.5.em)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("   ", fontSize = 1.5.em)
                Text(formatNumber(partner.characterStats.trainedBp, 4), color = Color.Yellow, fontSize = 1.5.em)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("HP", fontSize = 1.5.em)
                Text(partner.speciesStats.displayHp(), fontSize = 1.5.em)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("   ", fontSize = 1.5.em)
                Text(formatNumber(partner.characterStats.trainedHp, 4), color = Color.Yellow, fontSize = 1.5.em)
            }
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text("AP", fontSize = 1.5.em)
                Text(partner.speciesStats.displayAp(), fontSize = 1.5.em)
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