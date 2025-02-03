package com.github.cfogrady.vitalwear.character

import android.graphics.Bitmap
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.character.data.CharacterFirmwareSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import com.github.cfogrady.vitalwear.steps.StepSensorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

internal class PartnerScreenControllerImpl(
    override val backgroundHeight: Dp,
    override val bitmapScaler: BitmapScaler,
    private val characterManager: CharacterManager,
    private val stepSensorService: StepSensorService,
    private val heartRateService: HeartRateService,
    private val dateTimeProvider: ()->LocalDateTime = { LocalDateTime.now() },
): PartnerScreenController {

    override val characterFirmwareSprites: CharacterFirmwareSprites
        get() = TODO("Not yet implemented")
    override val dailyStepCount: StateFlow<Int>
        get() = TODO("Not yet implemented")
    override val characterBitmaps: StateFlow<List<Bitmap>>
        get() = TODO("Not yet implemented")

    override fun getEmoteBitmaps(coroutineScope: CoroutineScope): StateFlow<List<Bitmap?>> {
        return characterManager.getCharacterFlow().combine(heartRateService.currentExerciseLevel) { character, exerciseLevel->
            if(character == null) {
                emptyList<Bitmap>()
            }
            character!!.getEmoteBitmaps(characterFirmwareSprites.emoteFirmwareSprites, exerciseLevel)
        }.stateIn(coroutineScope, SharingStarted.Lazily, emptyList<Bitmap>())
    }

    override fun getVitalsFlow(coroutineScope: CoroutineScope): StateFlow<Int> {
        return characterManager.getCharacterFlow().map {
            if(it == null) {
                return@map 0
            }
            return@map it.characterStats.vitals
        }.stateIn(coroutineScope, SharingStarted.Lazily, 0)
    }

    override fun getTimeFlow(coroutineScope: CoroutineScope): StateFlow<LocalDateTime> {
        val dateTimeFlow = MutableStateFlow(dateTimeProvider.invoke())
        coroutineScope.launch {
            while(isActive) {
                val currentNow = dateTimeFlow.value
                val millisUntilNextMinute = (60 - currentNow.second)*1_000.toLong()
                delay(millisUntilNextMinute)
                dateTimeFlow.value = dateTimeProvider.invoke()
            }
        }
        return dateTimeFlow
    }

    private fun walkedRecently(currentTime: LocalDateTime = dateTimeProvider.invoke()): Boolean {
        val timeFrom10StepsAgo = stepSensorService.timeFrom10StepsAgo.value
        val millisUntilIdle = 60_000 - ChronoUnit.MILLIS.between(timeFrom10StepsAgo, currentTime)
        Timber.i("Time To Idle: $millisUntilIdle")
        val nextUpdate = if(millisUntilIdle > 0) millisUntilIdle.coerceAtMost(millisUntilNextMinute) else millisUntilNextMinute
    }
}