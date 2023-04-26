package com.github.cfogrady.vitalwear.character

import android.content.Context
import android.content.SharedPreferences
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.steps.DailyStepHandler

class CharacterUpdateWorkerProvider : WorkerProvider {
    override fun createWorker(
        characterManager: CharacterManager,
        dailyStepHandler: DailyStepHandler,
        appContext: Context,
        workerParameters: WorkerParameters
    ): Worker {
        return CharacterUpdateWorker(characterManager, appContext, workerParameters)
    }
}