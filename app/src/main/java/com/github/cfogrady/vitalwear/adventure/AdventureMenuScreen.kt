package com.github.cfogrady.vitalwear.adventure

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.firmware.components.AdventureBitmaps
import com.github.cfogrady.vitalwear.character.activity.LOADING_TEXT
import com.github.cfogrady.vitalwear.common.card.CardSpriteLoader
import com.github.cfogrady.vitalwear.common.card.CardType
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.common.composable.util.formatNumber
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.ImageScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.firmware.Firmware
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AdventureMenuState {
    Loading,
    CardSelection,
    ZoneSelection,
    ZoneConfirm,
    Ready,
    Go,
}

interface AdventureMenuScreenController: CardSelectionController, ZoneSelectionController, ZoneConfirmController, ReadyController, GoController {
    fun loadCardBackgrounds(cardName: String): List<Bitmap>
    fun loadCardIcon(cardName: String): Bitmap
    fun startAdventure(cardName: String, selectedAdventureId: Int)
}

@Composable
fun AdventureMenuScreen(controller: AdventureMenuScreenController, onFinish: () -> Unit) {
    var adventureMenuState by remember { mutableStateOf(AdventureMenuState.CardSelection) }
    var cardSelected by remember { mutableStateOf<CardMetaEntity?>(null) }
    var backgrounds by remember { mutableStateOf(emptyList<Bitmap>()) }
    var cardIcon by remember { mutableStateOf<Bitmap?>(null) }
    var selectedAdventure by remember { mutableStateOf<AdventureEntity?>(null) }
    when(adventureMenuState) {
        AdventureMenuState.Loading -> Loading {}
        AdventureMenuState.CardSelection -> CardSelection(controller) {
            adventureMenuState = AdventureMenuState.Loading
            cardSelected = it
            CoroutineScope(Dispatchers.IO).launch {
                backgrounds = controller.loadCardBackgrounds(it.cardName)
                cardIcon = controller.loadCardIcon(it.cardName)
                adventureMenuState = AdventureMenuState.ZoneSelection
            }
        }
        AdventureMenuState.ZoneSelection -> ZoneSelection(
            controller,
            cardMetaEntity = cardSelected!!,
            backgrounds = backgrounds,
            cardIcon = cardIcon!!) {
            selectedAdventure = it
            adventureMenuState = AdventureMenuState.ZoneConfirm
        }
        AdventureMenuState.ZoneConfirm -> ZoneConfirm(
            controller,
            cardMetaEntity = cardSelected!!,
            adventureEntity = selectedAdventure!!,
            backgrounds = backgrounds
        ) {
            adventureMenuState = AdventureMenuState.Ready
        }
        AdventureMenuState.Ready -> Ready(controller, background = backgrounds[selectedAdventure!!.bossBackgroundId]) {
            controller.startAdventure(cardSelected!!.cardName, selectedAdventure!!.adventureId)
            adventureMenuState = AdventureMenuState.Go
        }
        AdventureMenuState.Go -> Go(controller, background = backgrounds[selectedAdventure!!.walkingBackgroundId]) {
            onFinish.invoke()
        }
    }
}

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
private fun AdventureMenuScreenPreview() {
    val imageScaler = ImageScaler.getContextImageScaler(LocalContext.current)
    val bitmapScaler = BitmapScaler(imageScaler)
    val vitalBoxFactory = VitalBoxFactory(imageScaler)
    val firmware = Firmware.loadPreviewFirmwareFromDisk(LocalContext.current)
    val controllerContext = LocalContext.current
    val controller = object: AdventureMenuScreenController {
        override val vitalBoxFactory = vitalBoxFactory
        override val bitmapScaler = bitmapScaler
        override fun loadCardBackgrounds(cardName: String): List<Bitmap> {
            val background = CardSpriteLoader.loadTestCardSprite(controllerContext, 1)
            return listOf(background)
        }
        override fun loadCardIcon(cardName: String): Bitmap {
            return CardSpriteLoader.loadTestCardSprite(controllerContext, 0)
        }
        override fun startAdventure(cardName: String, selectedAdventureId: Int) {}
        override fun loadCardsForActiveCharacterFranchise(): List<CardMetaEntity> {
            return listOf(
                CardMetaEntity("Test Card", 0, 0, CardType.DIM, 0, null),
                CardMetaEntity("Test Card2", 1, 0, CardType.DIM, 0, null),
                CardMetaEntity("Validation Card", 2, 0, CardType.DIM, 0, null),

                )
        }
        val adventureBitmaps: AdventureBitmaps = firmware.adventureBitmaps

        override suspend fun getMaxAdventureForActiveCharacter(cardName: String): Int {
            return 2
        }

        override suspend fun getAdventuresForCard(cardName: String): List<AdventureEntity> {
            return listOf(
                createAdventureEntity(0, 500, 2),
                createAdventureEntity(1, 500, 3),
                createAdventureEntity(2, 500, 4),
                createAdventureEntity(3, 500, 5),
                createAdventureEntity(4, 500, 6),
            )
        }

        override suspend fun loadBossCharacterBitmap(cardName: String, characterId: Int): Bitmap {
            return CardSpriteLoader.loadTestCharacterSprites(controllerContext, characterId).sprites[CharacterSprites.IDLE_1]
        }

        override val firmware: Firmware = firmware
        override val backgroundHeight: Dp = imageScaler.calculateBackgroundHeight()
        override val characterSprites: CharacterSprites = CardSpriteLoader.loadTestCharacterSprites(controllerContext, 2)

    }
    AdventureMenuScreen(controller, {})
}

interface CardSelectionController {
    val bitmapScaler: BitmapScaler
    val vitalBoxFactory: VitalBoxFactory
    fun loadCardsForActiveCharacterFranchise(): List<CardMetaEntity>
}

@Composable
fun CardSelection(controller: CardSelectionController, onCardSelected: (CardMetaEntity) -> Unit) {
    var loaded by remember { mutableStateOf(false) }
    var cards by remember { mutableStateOf(ArrayList<CardMetaEntity>() as List<CardMetaEntity>) }
    LaunchedEffect(true) {
        loaded = false
        withContext(Dispatchers.IO) {
            cards = controller.loadCardsForActiveCharacterFranchise()
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

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
private fun CardSelectionPreview() {
    val imageScaler = ImageScaler.getContextImageScaler(LocalContext.current)
    val bitmapScaler = BitmapScaler(imageScaler)
    val vitalBoxFactory = VitalBoxFactory(imageScaler)
    val controller = object: CardSelectionController {
        override val vitalBoxFactory = vitalBoxFactory
        override val bitmapScaler = bitmapScaler
        override fun loadCardsForActiveCharacterFranchise(): List<CardMetaEntity> {
            return listOf(
                CardMetaEntity("Test Card", 0, 0, CardType.DIM, 0, null),
                CardMetaEntity("Test Card2", 1, 0, CardType.DIM, 0, null),
                CardMetaEntity("Validation Card", 2, 0, CardType.DIM, 0, null),

            )
        }

    }
    CardSelection(controller, {})
}

interface ZoneSelectionController {
    val bitmapScaler: BitmapScaler
    val vitalBoxFactory: VitalBoxFactory
    val adventureBitmaps: AdventureBitmaps

    suspend fun getMaxAdventureForActiveCharacter(cardName: String): Int
    suspend fun getAdventuresForCard(cardName: String): List<AdventureEntity>
}

@Composable
fun ZoneSelection(
    controller: ZoneSelectionController,
    cardMetaEntity: CardMetaEntity,
    backgrounds: List<Bitmap>,
    cardIcon: Bitmap,
    onZoneSelected: (AdventureEntity) -> Unit ) {
    var loaded by remember { mutableStateOf(false) }
    var adventures by remember { mutableStateOf(listOf<AdventureEntity>()) }
    if(!loaded) {
        Loading {
            val currentMax = controller.getMaxAdventureForActiveCharacter(cardMetaEntity.cardName)
            val cardAdventures = controller.getAdventuresForCard(cardMetaEntity.cardName)
            adventures = cardAdventures.subList(0, (currentMax+1).coerceAtMost(cardAdventures.size))
            loaded = true
        }
    } else {
        val pagerState = rememberPagerState(pageCount = {adventures.size}, initialPage = adventures.size-1)
        val bitmapScaler = controller.bitmapScaler
        controller.vitalBoxFactory.VitalBox {
            VerticalPager(state = pagerState) {
                val adventure = adventures[it]
                Box {
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
                                bitmap = controller.adventureBitmaps.advImage,
                                contentDescription = "adv text"
                            )
                            bitmapScaler.ScaledBitmap(
                                bitmap = controller.adventureBitmaps.missionImage,
                                contentDescription = "mission text"
                            )
                        }
                        bitmapScaler.ScaledBitmap(bitmap = cardIcon, contentDescription = "card icon")
                        bitmapScaler.ScaledBitmap(bitmap = controller.adventureBitmaps.stageImage, contentDescription = "stage text")
                        Text(text = formatNumber(it+1, 2), fontWeight = FontWeight.Bold, fontSize = 3.em)
                    }
                }
            }
        }
    }
}

private fun createAdventureEntity(adventureId: Int, steps: Int, characterId: Int): AdventureEntity {
    return AdventureEntity(
        cardName = "TestCard",
        adventureId = adventureId,
        steps = steps,
        characterId = characterId,
        bp = null,
        hp = null,
        ap = null,
        attackId = null,
        criticalAttackId = null,
        walkingBackgroundId = 0,
        bossBackgroundId = 0,
        hiddenBoss = false,
        characterIdJoined = null
    )
}

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
private fun ZoneSelectionPreview() {
    val firmware = Firmware.loadPreviewFirmwareFromDisk(LocalContext.current)
    val imageScaler = ImageScaler.getContextImageScaler(LocalContext.current)
    val bitmapScaler = BitmapScaler(imageScaler)
    val vitalBoxFactory = VitalBoxFactory(imageScaler)
    val controller = object: ZoneSelectionController {
        override val adventureBitmaps = firmware.adventureBitmaps
        override suspend fun getMaxAdventureForActiveCharacter(cardName: String): Int {
            return 4
        }
        override suspend fun getAdventuresForCard(cardName: String): List<AdventureEntity> {
            return listOf(
                createAdventureEntity(0, 500, 2),
                createAdventureEntity(1, 500, 3),
                createAdventureEntity(2, 500, 4),
                createAdventureEntity(3, 500, 5),
                createAdventureEntity(4, 500, 6),
            )
        }
        override val vitalBoxFactory = vitalBoxFactory
        override val bitmapScaler = bitmapScaler

    }
    val cardMetaEntity = CardMetaEntity(
        cardName = "TestCard",
        cardId = 0,
        cardChecksum = 0,
        cardType = CardType.DIM,
        franchise = 0,
        maxAdventureCompletion = null
    )
    val background = BitmapFactory.decodeStream(LocalContext.current.assets.open("test_background.png"))
    val backgrounds = listOf(background)
    val cardIcon = CardSpriteLoader.loadTestCardSprite(LocalContext.current, 0)
    ZoneSelection(controller, cardMetaEntity, backgrounds, cardIcon, {})
}

interface ZoneConfirmController {
    val bitmapScaler: BitmapScaler
    val vitalBoxFactory: VitalBoxFactory
    val adventureBitmaps: AdventureBitmaps

    suspend fun loadBossCharacterBitmap(cardName: String, characterId: Int): Bitmap
}

@Composable
fun ZoneConfirm(controller: ZoneConfirmController, cardMetaEntity: CardMetaEntity, adventureEntity: AdventureEntity, backgrounds: List<Bitmap>, onConfirm: () -> Unit) {
    val bitmapScaler = controller.bitmapScaler
    val adventureFirmwareSprites = controller.adventureBitmaps
    var loading by remember { mutableStateOf(true) }
    var boss by remember { mutableStateOf<Bitmap?>(null) }
    if(loading) {
        Loading(scope = Dispatchers.IO) {
            boss = if(adventureEntity.hiddenBoss) {
                adventureFirmwareSprites.hiddenImage
            } else {
                controller.loadBossCharacterBitmap(cardMetaEntity.cardName, adventureEntity.characterId)
            }
            loading = false
        }
    } else {
        controller.vitalBoxFactory.VitalBox {
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

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
private fun ZoneConfirmPreview() {
    val firmware = Firmware.loadPreviewFirmwareFromDisk(LocalContext.current)
    val imageScaler = ImageScaler.getContextImageScaler(LocalContext.current)
    val bitmapScaler = BitmapScaler(imageScaler)
    val vitalBoxFactory = VitalBoxFactory(imageScaler)
    val controllerContext = LocalContext.current
    val controller = object: ZoneConfirmController {
        override val adventureBitmaps = firmware.adventureBitmaps
        override val vitalBoxFactory = vitalBoxFactory
        override val bitmapScaler = bitmapScaler

        override suspend fun loadBossCharacterBitmap(cardName: String, characterId: Int): Bitmap {
            return CardSpriteLoader.loadTestCharacterSprites(controllerContext, characterId).sprites[CharacterSprites.IDLE_1]
        }
    }
    val cardMetaEntity = CardMetaEntity(
        cardName = "TestCard",
        cardId = 0,
        cardChecksum = 0,
        cardType = CardType.DIM,
        franchise = 0,
        maxAdventureCompletion = null
    )
    val adventureEntity = createAdventureEntity(3, 500, 5)
    val background = BitmapFactory.decodeStream(LocalContext.current.assets.open("test_background.png"))
    val backgrounds = listOf(background)
    ZoneConfirm(controller, cardMetaEntity, adventureEntity, backgrounds, {})
}

interface ReadyController {
    val firmware: Firmware
    val bitmapScaler: BitmapScaler
    val vitalBoxFactory: VitalBoxFactory
}

@Composable
fun Ready(controller: ReadyController, background: Bitmap, onFinish: () -> Unit) {
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
    controller.vitalBoxFactory.VitalBox {
        controller.bitmapScaler.ScaledBitmap(
            bitmap = background,
            contentDescription = "background"
        )
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
            controller.bitmapScaler.ScaledBitmap(bitmap = controller.firmware.readyIcon, contentDescription = "ready", modifier = Modifier.padding(0.dp, 30.dp))
            Text(text = "$seconds", fontSize = 5.em)
        }
    }
}

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
private fun ReadyPreview() {
    val firmware = Firmware.loadPreviewFirmwareFromDisk(LocalContext.current)
    val imageScaler = ImageScaler.getContextImageScaler(LocalContext.current)
    val bitmapScaler = BitmapScaler(imageScaler)
    val vitalBoxFactory = VitalBoxFactory(imageScaler)
    val controller = object: ReadyController {
        override val firmware = firmware
        override val vitalBoxFactory = vitalBoxFactory
        override val bitmapScaler = bitmapScaler
    }
    Ready(controller, BitmapFactory.decodeStream(LocalContext.current.assets.open("test_background.png")), {})
}

interface GoController {
    val firmware: Firmware
    val vitalBoxFactory: VitalBoxFactory
    val backgroundHeight: Dp
    val bitmapScaler: BitmapScaler
    val characterSprites: CharacterSprites
}

@Composable
fun Go(controller: GoController, background: Bitmap, onFinish: () -> Unit) {
    var state by remember { mutableIntStateOf(CharacterSprites.IDLE_1) }
    LaunchedEffect(key1 = state) {
        Handler.createAsync(Looper.getMainLooper()).postDelayed({
            if(state == CharacterSprites.IDLE_1) {
                state = CharacterSprites.WIN
            } else {
                onFinish.invoke()
            }
        }, 500)
    }
    controller.vitalBoxFactory.VitalBox {
        controller.bitmapScaler.ScaledBitmap(
            bitmap = background,
            contentDescription = "background"
        )
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            controller.bitmapScaler.ScaledBitmap(bitmap = controller.firmware.goIcon, contentDescription = "go", modifier = Modifier.padding(0.dp, 20.dp))
            controller.bitmapScaler.ScaledBitmap(bitmap = controller.characterSprites.sprites[state], contentDescription = "partner", modifier = Modifier.offset(y = controller.backgroundHeight.times(-.05f)))
        }
    }
}

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
private fun GoPreview() {
    val firmware = Firmware.loadPreviewFirmwareFromDisk(LocalContext.current)
    val imageScaler = ImageScaler.getContextImageScaler(LocalContext.current)
    val bitmapScaler = BitmapScaler(imageScaler)
    val vitalBoxFactory = VitalBoxFactory(imageScaler)
    val backgroundHeight = imageScaler.calculateBackgroundHeight()
    val controller = object: GoController {
        override val firmware = firmware
        override val vitalBoxFactory = vitalBoxFactory
        override val backgroundHeight = backgroundHeight
        override val bitmapScaler = bitmapScaler
        override val characterSprites: CharacterSprites = CardSpriteLoader.loadTestCharacterSprites(
            LocalContext.current, 3)
    }
    Go(controller, BitmapFactory.decodeStream(LocalContext.current.assets.open("test_background.png")), {})
}
