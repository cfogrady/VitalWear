package com.github.cfogrady.vitalwear.steps

import kotlinx.coroutines.flow.StateFlow

interface StepService {
    val dailySteps: StateFlow<Int>
}