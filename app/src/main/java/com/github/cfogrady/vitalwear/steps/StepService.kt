package com.github.cfogrady.vitalwear.steps

interface StepService {
    suspend fun addStepsToVitals()
    fun listenDailySteps(): ManyStepListener
}