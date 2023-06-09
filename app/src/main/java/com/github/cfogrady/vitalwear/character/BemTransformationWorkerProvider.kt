package com.github.cfogrady.vitalwear.character

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.workmanager.WorkProviderDependencies
import com.github.cfogrady.vitalwear.workmanager.WorkerProvider

class BemTransformationWorkerProvider : WorkerProvider {
    override fun createWorker(
        workProviderDependencies: WorkProviderDependencies,
        appContext: Context,
        workerParameters: WorkerParameters
    ): Worker {
        return BemTransformationWorker(workProviderDependencies.characterManager, appContext, workerParameters)
    }
}