package com.github.cfogrady.vitalwear.workmanager

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

interface WorkerProvider {
    fun createWorker(workProviderDependencies: WorkProviderDependencies, appContext: Context, workerParameters: WorkerParameters) : ListenableWorker
}