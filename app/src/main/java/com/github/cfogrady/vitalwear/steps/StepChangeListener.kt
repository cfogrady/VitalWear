package com.github.cfogrady.vitalwear.steps

interface StepChangeListener {
    fun processStepChanges(oldSteps: Int, newSteps: Int)
}