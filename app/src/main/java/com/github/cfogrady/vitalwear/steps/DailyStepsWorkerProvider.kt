package com.github.cfogrady.vitalwear.steps

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.workmanager.WorkerProvider
import com.github.cfogrady.vitalwear.workmanager.WorkProviderDependencies

class DailyStepsWorkerProvider : WorkerProvider {
    override fun createWorker(
        workProviderDependencies: WorkProviderDependencies,
        appContext: Context,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return DailyStepWorker(appContext, workerParameters, workProviderDependencies.dailyStepHandler)
    }
}