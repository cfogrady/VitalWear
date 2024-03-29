package com.github.cfogrady.vitalwear.workmanager

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.character.transformation.VBTransformationWorkerProvider
import com.github.cfogrady.vitalwear.character.transformation.VBTransformationWorker

class VitalWearWorkerFactory(
    private val workProviderDependencies: WorkProviderDependencies,
    private val workerProviders: Map<String?, WorkerProvider> =
    mapOf(Pair(VBTransformationWorker::class.qualifiedName, VBTransformationWorkerProvider()),
    )
) : WorkerFactory() {
    private val TAG: String = "VitalWearWorkerFactory"

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return workerProviders[workerClassName]?.createWorker(workProviderDependencies, appContext, workerParameters)
    }

}