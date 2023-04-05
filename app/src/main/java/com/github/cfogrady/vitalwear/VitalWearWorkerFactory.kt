package com.github.cfogrady.vitalwear

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.character.*
import com.github.cfogrady.vitalwear.character.BemTransformationWorkerProvider

class VitalWearWorkerFactory(
    private val characterManager: CharacterManager,
    private val workerProviders: Map<String?, WorkerProvider> =
    mapOf(Pair(BemTransformationWorker::class.qualifiedName, BemTransformationWorkerProvider()),
        Pair(CharacterUpdateWorker::class.qualifiedName, CharacterUpdateWorkerProvider())
    )
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