package com.github.cfogrady.vitalwear.steps

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

interface StepService {
    val dailySteps: LiveData<Int>
    fun addStepsToVitals(): CompletableFuture<Void>
    fun listenDailySteps(): StepListener
}