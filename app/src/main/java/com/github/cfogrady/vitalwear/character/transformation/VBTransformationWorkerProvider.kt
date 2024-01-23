package com.github.cfogrady.vitalwear.character.transformation

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.workmanager.WorkProviderDependencies
import com.github.cfogrady.vitalwear.workmanager.WorkerProvider

class VBTransformationWorkerProvider : WorkerProvider {
    override fun createWorker(
        workProviderDependencies: WorkProviderDependencies,
        appContext: Context,
        workerParameters: WorkerParameters
    ): Worker {
        return VBTransformationWorker(
            workProviderDependencies.characterManager,
            workProviderDependencies.notificationChannelManager,
            workProviderDependencies.bemUpdater,
            appContext,
            workerParameters,
        )
    }
}