package com.github.cfogrady.vitalwear.steps

import kotlinx.coroutines.flow.SharedFlow

interface ManyStepListener {
    val dailyStepsAtStart: Int
    val dailyStepObserver : SharedFlow<Int>

    fun unregister() {

    }
}