package com.github.cfogrady.vitalwear.steps

import kotlinx.coroutines.Job
import java.time.LocalDate

interface DailyStepHandler {
    suspend fun handleDayTransition(newDay: LocalDate)
}