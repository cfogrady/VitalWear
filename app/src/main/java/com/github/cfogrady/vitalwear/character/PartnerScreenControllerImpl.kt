package com.github.cfogrady.vitalwear.character

import android.graphics.Bitmap
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.firmware.components.CharacterIconBitmaps
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import com.github.cfogrady.vitalwear.steps.StepSensorService
import com.github.cfogrady.vitalwear.util.flow.combineStates
import com.github.cfogrady.vitalwear.util.flow.mapState
import com.github.cfogrady.vitalwear.util.flow.transformState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    val characterIconBitmaps: CharacterIconBitmaps
        get() = firmwareManager.getFirmware().value!!.characterIconBitmaps
    override val dailyStepCount: StateFlow<Int> = stepSensorService.dailySteps
    override val emoteBitmaps = combineStates(characterManager.getCharacterFlow(), heartRateService.currentExerciseLevel) { character, exerciseLevel->
        character?.getEmoteBitmaps(characterIconBitmaps.emoteBitmaps, exerciseLevel)
            ?: emptyList<Bitmap>()
    }
    override val vitals = characterManager.getCharacterFlow().mapState {
        if(it == null) {
            return@mapState 0
        }
        return@mapState it.characterStats.vitals
    }

    private fun getIdleFlow(coroutineScope: CoroutineScope): StateFlow<Boolean> {
        var job: Job? = null
        return stepSensorService.timeFrom10StepsAgo.transformState(false) { timeFrom10StepsAgo->
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
        return combineStates(characterManager.getCharacterFlow(), getIdleFlow(coroutineScope), heartRateService.currentExerciseLevel) {character, idle, exerciseLevel->
            character?.getNormalBitmaps(!idle, exerciseLevel) ?: emptyList()
        }
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