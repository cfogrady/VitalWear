package com.github.cfogrady.vitalwear.steps

import java.time.LocalDate

interface DailyStepHandler {
    suspend fun handleDayTransition(newDay: LocalDate)
}