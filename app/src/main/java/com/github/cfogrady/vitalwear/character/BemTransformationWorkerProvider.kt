package com.github.cfogrady.vitalwear.character

import android.content.Context
import android.content.SharedPreferences
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.character.BemTransformationWorker
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.WorkerProvider
import com.github.cfogrady.vitalwear.steps.DailyStepHandler

class BemTransformationWorkerProvider : WorkerProvider {
    override fun createWorker(
        characterManager: CharacterManager,
        dailyStepHandler: DailyStepHandler,
        appContext: Context,
        workerParameters: WorkerParameters
    ): Worker {
        return BemTransformationWorker(characterManager, appContext, workerParameters)
    }
}