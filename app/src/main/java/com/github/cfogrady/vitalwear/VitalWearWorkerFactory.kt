package com.github.cfogrady.vitalwear

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.character.*
import com.github.cfogrady.vitalwear.character.BemTransformationWorkerProvider
import com.github.cfogrady.vitalwear.steps.DailyStepHandler
import com.github.cfogrady.vitalwear.steps.DailyStepWorker
import com.github.cfogrady.vitalwear.steps.DailyStepsWorkerProvider

class VitalWearWorkerFactory(
    private val characterManager: CharacterManager,
    private val dailyStepHandler: DailyStepHandler,
    private val workerProviders: Map<String?, WorkerProvider> =
    mapOf(Pair(BemTransformationWorker::class.qualifiedName, BemTransformationWorkerProvider()),
        Pair(CharacterUpdateWorker::class.qualifiedName, CharacterUpdateWorkerProvider()),
        Pair(DailyStepWorker::class.qualifiedName, DailyStepsWorkerProvider())
    )
) : WorkerFactory() {
    private val TAG: String = "BEMWorkerFactory"

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return workerProviders[workerClassName]?.createWorker(characterManager, dailyStepHandler, appContext, workerParameters)
    }

}