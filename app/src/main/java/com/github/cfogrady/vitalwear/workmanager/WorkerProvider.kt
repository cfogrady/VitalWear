package com.github.cfogrady.vitalwear.workmanager

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import com.github.cfogrady.vitalwear.steps.DailyStepHandler

interface WorkerProvider {
    fun createWorker(workProviderDependencies: WorkProviderDependencies, appContext: Context, workerParameters: WorkerParameters) : ListenableWorker
}