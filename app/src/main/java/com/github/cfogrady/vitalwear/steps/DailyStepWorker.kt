package com.github.cfogrady.vitalwear.steps

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.util.CompletableFutureListenableWrapper
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

class DailyStepWorker(val context: Context, workerParameters: WorkerParameters, private val dailyStepHandler: DailyStepHandler, private val characterManager: CharacterManager) : ListenableWorker(context, workerParameters) {

    companion object {
        const val TAG = "DailyStepWorker"
    }

    override fun startWork(): ListenableFuture<Result> {
        val future = dailyStepHandler.handleDayTransition(LocalDate.now())
            .thenApplyAsync {
                characterManager.updateActiveCharacter(LocalDateTime.now())
                Result.success()
            }
        return CompletableFutureListenableWrapper(future)
    }
}