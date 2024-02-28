package com.github.cfogrady.vitalwear.steps

import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

class StepState {
    internal val dailySteps = MutableStateFlow(0)
    internal lateinit var dateOfLastRead: LocalDate
    internal var lastStepReading = 0
    internal var startOfDaySteps = 0
    internal var stepSensorEnabled = false
}