package com.github.cfogrady.vitalwear.steps

import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

interface StepService {
    val dailySteps: StateFlow<Int>
    val timeFrom10StepsAgo: StateFlow<LocalDateTime>
}