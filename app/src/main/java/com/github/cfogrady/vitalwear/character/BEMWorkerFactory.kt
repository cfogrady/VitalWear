package com.github.cfogrady.vitalwear.character

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.character.data.BemTransformationWorkerProvider

class BEMWorkerFactory(
    private val characterManager: CharacterManager,
    private val workerProviders: Map<String?, BemTransformationWorkerProvider> =
    mapOf(Pair(BemTransformationWorker::class.qualifiedName, BemTransformationWorkerProvider()))
) : WorkerFactory() {
    private val TAG: String = "BEMWorkerFactory"

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return workerProviders[workerClassName]?.createWorker(characterManager, appContext, workerParameters)
    }
}