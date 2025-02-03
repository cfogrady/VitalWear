package com.github.cfogrady.vitalwear.main

import com.github.cfogrady.vitalwear.*
import com.github.cfogrady.vitalwear.character.PartnerScreenComposable
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import java.util.ArrayList

class MainScreenComposable(
    private val saveService: SaveService,
    private val bitmapScaler: BitmapScaler,
    private val partnerScreenComposable: PartnerScreenComposable,
    private val vitalBoxFactory: VitalBoxFactory,
) {

}
