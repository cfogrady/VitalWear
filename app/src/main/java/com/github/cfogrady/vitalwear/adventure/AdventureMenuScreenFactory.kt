package com.github.cfogrady.vitalwear.adventure

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.adventure.firmware.AdventureFirmwareSprites
import com.github.cfogrady.vitalwear.character.activity.LOADING_TEXT
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.common.card.CharacterSpritesIO
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntityDao
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.common.composable.util.formatNumber
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.firmware.Firmware
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdventureMenuScreenFactory(
    private val cardSpritesIO: CardSpritesIO,
    private val cardMetaEntityDao: CardMetaEntityDao,
    private val adventureService: AdventureService,
    private val vitalBoxFactory: VitalBoxFactory,
    private val characterSpritesIO: CharacterSpritesIO,
    private val speciesEntityDao: SpeciesEntityDao,
    private val bitmapScaler: BitmapScaler,
    private val backgroundHeight: Dp
) {

    enum class AdventureMenuState {
        Loading,
        CardSelection,
        ZoneSelection,
        ZoneConfirm,
        Ready,
        Go,
    }

    @Composable
    fun AdventureMenuScreen(context: Context, firmware: Firmware, character: BEMCharacter, onFinish: () -> Unit) {
        var adventureMenuState by remember { mutableStateOf(AdventureMenuState.CardSelection) }
        var cardSelected by remember { mutableStateOf<CardMetaEntity?>(null) }
        var backgrounds by remember { mutableStateOf(emptyList<Bitmap>()) }
        var cardIcon by remember { mutableStateOf<Bitmap?>(null) }
        var selectedAdventure by remember { mutableStateOf<AdventureEntity?>(null) }
        when(adventureMenuState) {
            AdventureMenuState.Loading -> Loading {}
            AdventureMenuState.CardSelection -> CardSelection(character) {
                adventureMenuState = AdventureMenuState.Loading
                cardSelected = it
                CoroutineScope(Dispatchers.IO).launch {
                    backgrounds = cardSpritesIO.loadCardBackgrounds(context, it.cardName)
                    cardIcon = cardSpritesIO.loadCardSprite(context, it.cardName, CardSpritesIO.ICON)
                    adventureMenuState = AdventureMenuState.ZoneSelection
                }
            }
            AdventureMenuState.ZoneSelection -> ZoneSelection(
                partner = character,
                cardMetaEntity = cardSelected!!,
                firmware = firmware,
                backgrounds = backgrounds,
                cardIcon = cardIcon!!) {
                selectedAdventure = it
                adventureMenuState = AdventureMenuState.ZoneConfirm
            }
            AdventureMenuState.ZoneConfirm -> ZoneConfirm(
                context = context,
                adventureFirmwareSprites = firmware.adventureFirmwareSprites,
                cardMetaEntity = cardSelected!!,
                adventureEntity = selectedAdventure!!,
                backgrounds = backgrounds
            ) {
                adventureMenuState = AdventureMenuState.Ready
            }
            AdventureMenuState.Ready -> Ready(firmware = firmware, background = backgrounds[selectedAdventure!!.adventureId]) {
                val foregroundIntent = Intent(context, AdventureForegroundService::class.java)
                foregroundIntent.putExtra(AdventureForegroundService.CARD_NAME, selectedAdventure!!.cardName)
                foregroundIntent.putExtra(AdventureForegroundService.STARTING_ADVENTURE, selectedAdventure!!.adventureId)
                foregroundIntent.putExtra(AdventureForegroundService.PARTNER, character.characterStats.id)
                context.startForegroundService(foregroundIntent)
                adventureMenuState = AdventureMenuState.Go
            }
            AdventureMenuState.Go -> Go(firmware = firmware, background = backgrounds[selectedAdventure!!.adventureId], characterSprites = character.characterSprites) {
                onFinish.invoke()
            }
        }
    }

    @Composable
    fun CardSelection(partner: BEMCharacter, onCardSelected: (CardMetaEntity) -> Unit) {
        var loaded by remember { mutableStateOf(false) }
        var cards by remember { mutableStateOf(ArrayList<CardMetaEntity>() as List<CardMetaEntity>) }
        LaunchedEffect(true) {
            loaded = false
            withContext(Dispatchers.IO) {
                cards = loadCards(partner.cardMetaEntity.franchise)
                loaded = true
            }
        }
        if(!loaded) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = LOADING_TEXT)
            }
        } else {
            ScalingLazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = cards) { card ->
                    Button(onClick = {
                        onCardSelected.invoke(card)
                    }) {
                        Text(text = card.cardName, modifier = Modifier.padding(10.dp))
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ZoneSelection(
        partner: BEMCharacter,
        firmware: Firmware,
        cardMetaEntity: CardMetaEntity,
        backgrounds: List<Bitmap>,
        cardIcon: Bitmap,
        onZoneSelected: (AdventureEntity) -> Unit ) {
        var loaded by remember { mutableStateOf(false) }
        var adventures by remember { mutableStateOf(listOf<AdventureEntity>()) }
        var pageState by remember { mutableStateOf(PagerState(0)) }
        if(!loaded) {
            Loading {
                val currentMax = adventureService.getCurrentMaxAdventure(partner.characterStats.id, cardMetaEntity.cardName)
                adventures = adventureService.getAdventureOptions(cardMetaEntity.cardName).subList(0, currentMax+1)
                pageState = PagerState(adventures.size-1)
                loaded = true
            }
        } else {
            val bitmapScaler = bitmapScaler
            vitalBoxFactory.VitalBox {
                VerticalPager(state = pageState, pageCount = adventures.size) {
                    val adventure = adventures[it]
                    bitmapScaler.ScaledBitmap(
                        bitmap = backgrounds[adventure.walkingBackgroundId],
                        contentDescription = "background"
                    )
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            onZoneSelected.invoke(adventure)
                        }, verticalArrangement = Arrangement.SpaceAround, horizontalAlignment = Alignment.CenterHorizontally) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            bitmapScaler.ScaledBitmap(
                                bitmap = firmware.adventureFirmwareSprites.advImage,
                                contentDescription = "adv text"
                            )
                            bitmapScaler.ScaledBitmap(
                                bitmap = firmware.adventureFirmwareSprites.missionImage,
                                contentDescription = "mission text"
                            )
                        }
                        bitmapScaler.ScaledBitmap(bitmap = cardIcon, contentDescription = "card icon")
                        bitmapScaler.ScaledBitmap(bitmap = firmware.adventureFirmwareSprites.stageImage, contentDescription = "stage text")
                        Text(text = formatNumber(it+1, 2), fontWeight = FontWeight.Bold, fontSize = 3.em)
                    }
                }
            }
        }
    }

    @Composable
    fun ZoneConfirm(context: Context, adventureFirmwareSprites: AdventureFirmwareSprites, cardMetaEntity: CardMetaEntity, adventureEntity: AdventureEntity, backgrounds: List<Bitmap>, onConfirm: () -> Unit) {
        var loading by remember { mutableStateOf(true) }
        var boss by remember { mutableStateOf<Bitmap?>(null) }
        if(loading) {
            Loading(scope = Dispatchers.IO) {
                boss = if(adventureEntity.hiddenBoss) {
                    adventureFirmwareSprites.hiddenImage
                } else {
                    val bossSpecies = speciesEntityDao.getCharacterByCardAndCharacterId(cardMetaEntity.cardName, adventureEntity.characterId)
                    characterSpritesIO.loadCharacterBitmapFile(context, bossSpecies.spriteDirName, CharacterSpritesIO.IDLE1)
                }
                loading = false
            }
        } else {
            vitalBoxFactory.VitalBox {
                bitmapScaler.ScaledBitmap(bitmap = backgrounds[adventureEntity.bossBackgroundId], contentDescription = "background")
                Column(modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        onConfirm.invoke()
                    }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
                    bitmapScaler.ScaledBitmap(bitmap = adventureFirmwareSprites.nextMissionImage, contentDescription = "Next text")
                    bitmapScaler.ScaledBitmap(bitmap = boss!!, contentDescription = "Next text")
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            bitmapScaler.ScaledBitmap(bitmap = adventureFirmwareSprites.stageImage, contentDescription = "stage text")
                            Text(text = formatNumber(adventureEntity.adventureId+1, 2), fontSize = 3.em)
                        }
                        bitmapScaler.ScaledBitmap(bitmap = adventureFirmwareSprites.underlineImage, contentDescription = "separator")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                            bitmapScaler.ScaledBitmap(bitmap = adventureFirmwareSprites.flagImage, contentDescription = "goal")
                            Text(text = "${adventureEntity.steps}", fontSize = 3.em)
                        }
                        bitmapScaler.ScaledBitmap(bitmap = adventureFirmwareSprites.underlineImage, contentDescription = "separator")
                    }
                }
            }
        }
    }

    @Composable
    fun Ready(firmware: Firmware, background: Bitmap, onFinish: () -> Unit) {
        var seconds by remember { mutableStateOf(3) }
        LaunchedEffect(key1 = seconds) {
            Handler.createAsync(Looper.getMainLooper()).postDelayed({
                if(seconds == 1) {
                    onFinish.invoke()
                } else {
                    seconds--
                }
            }, 1000)
        }
        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(
                bitmap = background,
                contentDescription = "background"
            )
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
                bitmapScaler.ScaledBitmap(bitmap = firmware.readyIcon, contentDescription = "ready", modifier = Modifier.padding(0.dp, 30.dp))
                Text(text = "$seconds", fontSize = 5.em)
            }
        }
    }

    @Composable
    fun Go(firmware: Firmware, background: Bitmap, characterSprites: CharacterSprites, onFinish: () -> Unit) {
        var state by remember { mutableStateOf(CharacterSprites.IDLE_1) }
        LaunchedEffect(key1 = state) {
            Handler.createAsync(Looper.getMainLooper()).postDelayed({
                if(state == CharacterSprites.IDLE_1) {
                    state = CharacterSprites.WIN
                } else {
                    onFinish.invoke()
                }
            }, 500)
        }
        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(
                bitmap = background,
                contentDescription = "background"
            )
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
                bitmapScaler.ScaledBitmap(bitmap = firmware.goIcon, contentDescription = "go", modifier = Modifier.padding(0.dp, 20.dp))
                bitmapScaler.ScaledBitmap(bitmap = characterSprites.sprites[state], contentDescription = "partner", modifier = Modifier.offset(y = backgroundHeight.times(-.05f)))
            }
        }
    }

    private fun loadCards(franchiseId: Int) : List<CardMetaEntity> {
        val cardMetaEntityDao = cardMetaEntityDao
        return cardMetaEntityDao.getByFranchise(franchiseId)
    }
}