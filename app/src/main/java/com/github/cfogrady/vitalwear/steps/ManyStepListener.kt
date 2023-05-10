package com.github.cfogrady.vitalwear.steps

import kotlinx.coroutines.flow.StateFlow

interface ManyStepListener {
    val dailyStepObserver : StateFlow<Int>

    fun unregister() {

    }
}