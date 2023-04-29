package com.github.cfogrady.vitalwear.steps

import android.content.Context
import java.time.LocalDate

interface DailyStepHandler {
    fun handleDayTransition(newDay: LocalDate)
}