package com.github.cfogrady.vitalwear.steps

interface StepChangeListener {

    // If false, no state was changed. If True, state has changed and we should save
    fun processStepChanges(oldSteps: Int, newSteps: Int): Boolean
}