package com.github.cfogrady.vitalwear.background

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.common.card.CardType
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.firmware.Firmware
import kotlinx.coroutines.flow.MutableStateFlow

class BackgroundSelectionActivity : ComponentActivity() {
    companion object {
        const val BACKGROUND_TYPE = "BACKGROUND_TYPE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val backgroundType = BackgroundManager.BackgroundType.entries[intent.getIntExtra(BACKGROUND_TYPE, 0)]
        val vitalWearApp = application as VitalWearApp
        val cardMetaEntityDao = vitalWearApp.cardMetaEntityDao
        val firmware = vitalWearApp.firmwareManager.getFirmware().value!!
        val cardSpritesIO = vitalWearApp.cardSpriteIO
        val vitalBoxFactory = vitalWearApp.vitalBoxFactory
        setContent {
            BackgroundSelectorContent(backgroundType, firmware, cardMetaEntityDao, cardSpritesIO, vitalBoxFactory)
        }
    }

    @Composable
    fun BackgroundSelectorContent(backgroundType: BackgroundManager.BackgroundType, firmware: Firmware, cardMetaEntityDao: CardMetaEntityDao, cardSpritesIO: CardSpritesIO, vitalBoxFactory: VitalBoxFactory) {
        var loading by remember { mutableStateOf(true) }
        var uninitializedPages by remember { mutableStateOf<ArrayList<BackgroundEntry>>(ArrayList()) }
        if(loading) {
            Loading {
                val cards = getCards(cardMetaEntityDao)
                uninitializedPages = buildUnpopulatedBackgroundEntryArray(firmware, cards)
                loading = false
            }
        } else {
            BackgroundSelector(backgroundType, backgroundPages = uninitializedPages, cardSpritesIO = cardSpritesIO, vitalBoxFactory)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun BackgroundSelector(backgroundType: BackgroundManager.BackgroundType, backgroundPages: ArrayList<BackgroundEntry>, cardSpritesIO: CardSpritesIO, vitalBoxFactory: VitalBoxFactory) {
        val pagerState = rememberPagerState {
            backgroundPages.size
        }
        val cardsBeingLoaded = remember { mutableSetOf<String>() }
        vitalBoxFactory.VitalBox {
            VerticalPager(state = pagerState) {
                val backgroundEntry = backgroundPages[it]
                LaunchedEffect(key1 = it) {
                    if (it+1 < backgroundPages.size && backgroundPages[it+1].bitmap.value == null && !cardsBeingLoaded.contains(backgroundPages[it+1].cardName)) {
                        cardsBeingLoaded.add(backgroundPages[it+1].cardName) // could technically be a race condition, but I doubt a human would swipe fast enough to trigger it.
                        val cardBackgrounds = cardSpritesIO.loadCardBackgrounds(applicationContext, backgroundPages[it+1].cardName)
                        cardBackgrounds.forEachIndexed{idx, background ->
                            backgroundPages[it+1+idx].bitmap.value = background
                        }
                    }
                }
                val background by backgroundEntry.bitmap.collectAsState()
                if(background == null) {
                    Loading{}
                } else {
                    (application as VitalWearApp).bitmapScaler.ScaledBitmap(
                        bitmap = background!!,
                        contentDescription = "background",
                        modifier = Modifier.clickable {
                            if (backgroundEntry.firmware) {
                                (application as VitalWearApp).backgroundManager.setFirmwareBackground(backgroundType, backgroundEntry.index)
                            } else {
                                (application as VitalWearApp).backgroundManager.setCardBackground(backgroundType, backgroundEntry.cardName, backgroundEntry.index, background!!)
                            }
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun getCards(cardMetaEntityDao: CardMetaEntityDao): List<CardMetaEntity> {
        return cardMetaEntityDao.getAll()
    }

    class BackgroundEntry(val firmware: Boolean, val cardName: String, val index: Int, val bitmap: MutableStateFlow<Bitmap?>)

    private fun buildUnpopulatedBackgroundEntryArray(firmware: Firmware, cards: List<CardMetaEntity>): ArrayList<BackgroundEntry> {
        var count = firmware.backgrounds.size
        for ( card in cards ) {
            count += if (card.cardType == CardType.DIM) 1 else 6
        }
        val backgroundsArray = ArrayList<BackgroundEntry>(count)
        firmware.backgrounds.forEachIndexed{index, bitmap ->
            backgroundsArray.add(BackgroundEntry(true, "", index, MutableStateFlow( bitmap)))
        }
        for (card in cards) {
            val cardBackgroundCount = if (card.cardType == CardType.DIM) 1 else 6
            for (idx in 0 until cardBackgroundCount) {
                backgroundsArray.add(BackgroundEntry(false, card.cardName, idx, MutableStateFlow(null)))
            }
        }
        return backgroundsArray
    }
}