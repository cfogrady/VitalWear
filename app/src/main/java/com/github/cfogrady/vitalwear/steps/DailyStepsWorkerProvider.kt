package com.github.cfogrady.vitalwear.steps

import android.content.Context
import android.content.SharedPreferences
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.WorkerProvider

class DailyStepsWorkerProvider : WorkerProvider {
    override fun createWorker(
        characterManager: CharacterManager,
        dailyStepHandler: DailyStepHandler,
        appContext: Context,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return DailyStepWorker(appContext, workerParameters, dailyStepHandler)
    }
}