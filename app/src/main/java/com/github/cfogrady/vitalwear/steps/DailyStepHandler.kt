package com.github.cfogrady.vitalwear.steps

import android.content.Context
import kotlinx.coroutines.Job
import java.time.LocalDate
import java.util.concurrent.CompletableFuture

interface DailyStepHandler {
    fun handleDayTransition(newDay: LocalDate): CompletableFuture<Void>
}