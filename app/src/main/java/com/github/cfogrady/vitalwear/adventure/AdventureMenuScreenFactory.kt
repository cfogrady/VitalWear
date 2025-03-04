package com.github.cfogrady.vitalwear.adventure

import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.github.cfogrady.vitalwear.card.CardMeta
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.character.activity.LOADING_TEXT
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
import com.github.cfogrady.vitalwear.settings.CharacterSettings
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


}