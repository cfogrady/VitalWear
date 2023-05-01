package com.github.cfogrady.vitalwear.character.mood

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.workmanager.WorkerProvider
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import com.github.cfogrady.vitalwear.steps.DailyStepHandler
import com.github.cfogrady.vitalwear.workmanager.WorkProviderDependencies

class MoodUpdateWorkerProvider : WorkerProvider {
    override fun createWorker(
        workProviderDependencies: WorkProviderDependencies,
        appContext: Context,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return MoodUpdateWorker(workProviderDependencies.characterManager, workProviderDependencies.bemMoodUpdater, appContext, workerParameters)
    }
}