package com.github.cfogrady.vitalwear.steps

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import java.util.concurrent.Future

interface StepService {
    val dailySteps: LiveData<Int>
    fun addStepsToVitals(): Future<Void>
    fun listenDailySteps(lifecycleOwner: LifecycleOwner): LiveData<Int>
}