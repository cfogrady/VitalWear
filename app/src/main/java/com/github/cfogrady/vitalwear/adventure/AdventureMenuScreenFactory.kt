package com.github.cfogrady.vitalwear.adventure

import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.common.card.CharacterSpritesIO
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntityDao
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory

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