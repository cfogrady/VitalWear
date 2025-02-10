package com.github.cfogrady.vitalwear.character

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.character.data.CharacterFirmwareSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.common.composable.util.formatNumber
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import com.github.cfogrady.vitalwear.steps.StepService
import timber.log.Timber
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class PartnerScreenComposable(
    private val bitmapScaler: BitmapScaler,
    private val backgroundHeight: Dp,
    private val stepService: StepService,
    private val heartRateService: HeartRateService) {


}