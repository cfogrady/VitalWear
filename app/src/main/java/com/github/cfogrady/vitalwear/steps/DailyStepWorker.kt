package com.github.cfogrady.vitalwear.steps

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.util.CompletableFutureListenableWrapper
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.CompletableFuture

class DailyStepWorker(val context: Context, workerParameters: WorkerParameters, private val dailyStepHandler: DailyStepHandler) : ListenableWorker(context, workerParameters) {

    companion object {
        const val TAG = "DailyStepWorker"
    }

    private lateinit var workCompletion: CompletableFuture<Result>

    override fun startWork(): ListenableFuture<Result> {
        workCompletion = CompletableFuture<Result>()
        GlobalScope.launch {
            dailyStepHandler.handleDayTransition(LocalDate.now())
            workCompletion.complete(Result.success())
        }
        return CompletableFutureListenableWrapper(workCompletion)
    }
}