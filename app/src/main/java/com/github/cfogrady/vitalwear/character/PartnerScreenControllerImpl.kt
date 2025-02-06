package com.github.cfogrady.vitalwear.character

import android.graphics.Bitmap
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.character.data.CharacterFirmwareSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import com.github.cfogrady.vitalwear.steps.StepSensorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

internal class PartnerScreenControllerImpl(
    override val backgroundHeight: Dp,
    override val bitmapScaler: BitmapScaler,
    private val firmwareManager: FirmwareManager,
    private val characterManager: CharacterManager,
    private val stepSensorService: StepSensorService,
    private val heartRateService: HeartRateService,
    private val dateTimeProvider: ()->LocalDateTime = { LocalDateTime.now() }, // we have a dateTimeProvider so we can test with controlled and changing times
    private val delayWrapper: suspend (Long)->Unit = { delay(it) } // we wrap delay so we can test without actually delaying test code
): PartnerScreenController {

    override val characterFirmwareSprites: CharacterFirmwareSprites
        get() = firmwareManager.getFirmware().value!!.characterFirmwareSprites
    override val dailyStepCount: StateFlow<Int>
        get() = stepSensorService.dailySteps

    private fun getIdleFlow(coroutineScope: CoroutineScope): Flow<Boolean> {
        var job: Job? = null
        return stepSensorService.timeFrom10StepsAgo.transform { timeFrom10StepsAgo->
            job?.let {
                if(it.isActive) {
                    it.cancel()
                }
                job = null
            }
            val now = dateTimeProvider()
            val millisUntilIdle = 60_000 - ChronoUnit.MILLIS.between(timeFrom10StepsAgo, now)
            if(millisUntilIdle > 0) {
                emit(false)
                job = coroutineScope.launch {
                    delayWrapper(millisUntilIdle)
                    emit(true)
                }
            } else {
                emit(true)
            }
        }
    }

    override fun getCharacterBitmaps(coroutineScope: CoroutineScope): StateFlow<List<Bitmap>> {
        return combine(characterManager.getCharacterFlow(), getIdleFlow(coroutineScope), heartRateService.currentExerciseLevel) {character, idle, exerciseLevel->
            if(character == null) {
                emptyList<Bitmap>()
            }
            character!!.getNormalBitmaps(!idle, exerciseLevel)
        }.stateIn(coroutineScope, SharingStarted.Lazily, emptyList())
    }

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
}