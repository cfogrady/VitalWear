package com.github.cfogrady.vitalwear.workmanager

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.character.*
import com.github.cfogrady.vitalwear.character.BemTransformationWorkerProvider
import com.github.cfogrady.vitalwear.character.mood.MoodUpdateWorker
import com.github.cfogrady.vitalwear.character.mood.MoodUpdateWorkerProvider
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import com.github.cfogrady.vitalwear.steps.DailyStepHandler
import com.github.cfogrady.vitalwear.steps.DailyStepWorker
import com.github.cfogrady.vitalwear.steps.DailyStepsWorkerProvider

class VitalWearWorkerFactory(
    private val workProviderDependencies: WorkProviderDependencies,
    private val workerProviders: Map<String?, WorkerProvider> =
    mapOf(Pair(BemTransformationWorker::class.qualifiedName, BemTransformationWorkerProvider()),
        Pair(MoodUpdateWorker::class.qualifiedName, MoodUpdateWorkerProvider()),
        Pair(DailyStepWorker::class.qualifiedName, DailyStepsWorkerProvider())
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