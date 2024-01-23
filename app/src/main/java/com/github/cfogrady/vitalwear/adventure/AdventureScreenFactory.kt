package com.github.cfogrady.vitalwear.adventure

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.battle.BattleActivity
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.common.composable.util.formatNumber
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.firmware.Firmware
import java.time.LocalDateTime

class AdventureScreenFactory(
    private val adventureService: AdventureService,
    private val vitalBoxFactory: VitalBoxFactory,
    private val bitmapScaler: BitmapScaler,
    private val backgroundHeight: Dp
) {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun AdventureScreen(context: Context, adventureActivityLauncher: AdventureActivityLauncher, firmware: Firmware, partner: VBCharacter) {
        val adventure = adventureService.activeAdventure!!
        val currentStep by adventure.currentStep.collectAsState()
        val goalComplete by adventure.zoneCompleted.collectAsState()
        val stepsToGoal = remember(currentStep, goalComplete) {
            adventure.stepsTowardsGoal()
        }
        LaunchedEffect(goalComplete) {
            if(goalComplete) {
                Handler.createAsync(Looper.getMainLooper()).postDelayed({
                    adventureActivityLauncher.launchBattle {
                        val adventureEntity = adventure.currentAdventureEntity()
                        it.putExtra(BattleActivity.CARD_NAME, adventureEntity.cardName)
                        it.putExtra(BattleActivity.CHARACTER_ID, adventureEntity.characterId)
                        it.putExtra(BattleActivity.OPPONENT_BP, adventureEntity.bp)
                        it.putExtra(BattleActivity.OPPONENT_AP, adventureEntity.ap)
                        it.putExtra(BattleActivity.OPPONENT_HP, adventureEntity.hp)
                        it.putExtra(BattleActivity.BACKGROUND, adventureEntity.bossBackgroundId)
                        it.putExtra(BattleActivity.OPPONENT_ATTACK, adventureEntity.attackId)
                        it.putExtra(BattleActivity.OPPONENT_CRITICAL, adventureEntity.criticalAttackId)
                    }
                }, 500)
            }
        }
        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(bitmap = adventure.currentBackground(), contentDescription = "background")
            val pagerState = rememberPagerState(pageCount = {2})
            VerticalPager(state = pagerState) {
                when(it) {
                    0 -> PartnerScreen(firmware = firmware, partner = partner, steps = stepsToGoal, goal = adventure.goal())
                    1 -> CancelScreen(context, firmware)
                }

            }
        }
    }

    @Composable
    fun PartnerScreen(firmware: Firmware, partner: VBCharacter, steps: Int, goal: Int) {
        var now by remember { mutableStateOf(LocalDateTime.now()) }
        LaunchedEffect(key1 = now) {
            val secondsUntilNextMinute = 60 - now.second
            Handler.createAsync(Looper.getMainLooper()).postDelayed({
                now = LocalDateTime.now()
            }, 1000L*secondsUntilNextMinute)
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .fillMaxWidth()
                .offset(y = backgroundHeight.times(-.05f))) {
                Text(text="${formatNumber(now.hour, 2)}:${formatNumber(now.minute, 2)}", fontWeight = FontWeight.Bold, fontSize = 4.em)
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    bitmapScaler.ScaledBitmap(bitmap = firmware.adventureFirmwareSprites.flagImage, contentDescription = "Goal")
                    Text(text = formatNumber(goal, 4), color = Color.Yellow, modifier = Modifier.padding(5.dp, 0.dp))
                }
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp)) {
                    bitmapScaler.ScaledBitmap(bitmap = firmware.characterFirmwareSprites.stepsIcon, contentDescription = "Steps")
                    Text(text = formatNumber(steps, 4), color = Color.White, modifier = Modifier.padding(5.dp, 0.dp))
                }
                bitmapScaler.AnimatedScaledBitmap(bitmaps = partner.characterSprites.sprites, startIdx = CharacterSprites.WALK_1, frames = 2, contentDescription = "Character", alignment = Alignment.BottomCenter)
            }
        }
    }

    @Composable
    fun CancelScreen(context: Context, firmware: Firmware) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    adventureService.stopAdventure(context)
                },
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            bitmapScaler.ScaledBitmap(
                bitmap = firmware.menuFirmwareSprites.stopText,
                contentDescription = "stop")
            bitmapScaler.ScaledBitmap(
                bitmap = firmware.menuFirmwareSprites.stopIcon,
                contentDescription = "adventure")
        }
    }
}