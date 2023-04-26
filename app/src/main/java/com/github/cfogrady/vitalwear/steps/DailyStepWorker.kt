package com.github.cfogrady.vitalwear.steps

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.util.CompletableListenableFuture
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class DailyStepWorker(val context: Context, workerParameters: WorkerParameters, private val dailyStepHandler: DailyStepHandler) : ListenableWorker(context, workerParameters) {

    companion object {
        const val TAG = "DailyStepWorker"
    }

    private lateinit var workCompletion: CompletableListenableFuture<Result>

    override fun startWork(): ListenableFuture<Result> {
        workCompletion = CompletableListenableFuture()
        GlobalScope.launch {
            dailyStepHandler.handleDayTransition(LocalDate.now())
            workCompletion.complete(Result.success())
        }
        return workCompletion
    }
}