package com.github.cfogrady.vitalwear.steps

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.SaveService
import com.github.cfogrady.vitalwear.util.DeferredListenableWrapper
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class DailyStepWorker(val context: Context, workerParameters: WorkerParameters, private val dailyStepHandler: DailyStepHandler, private val saveService: SaveService, private val sharedPreferences: SharedPreferences) : ListenableWorker(context, workerParameters) {

    companion object {
        const val TAG = "DailyStepWorker"
    }

    override fun startWork(): ListenableFuture<Result> {
        val deferred = GlobalScope.async() {
            Log.i(TAG, "Handling step counter day transition")
            dailyStepHandler.handleDayTransition(LocalDate.now())
            saveService.save(sharedPreferences.edit().putLong(
                SensorStepService.LAST_MIDNIGHT_KEY, LocalDateTime.now().toEpochSecond(
                    ZoneOffset.UTC)))
            Log.i(TAG, "Day transition completed")
            Result.success()
        }
        return DeferredListenableWrapper(deferred)
    }
}