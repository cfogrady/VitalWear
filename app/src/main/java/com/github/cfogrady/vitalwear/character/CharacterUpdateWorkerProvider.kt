package com.github.cfogrady.vitalwear.character

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class CharacterUpdateWorkerProvider : WorkerProvider {
    override fun createWorker(
        characterManager: CharacterManager,
        appContext: Context,
        workerParameters: WorkerParameters
    ): Worker {
        return CharacterUpdateWorker(characterManager, appContext, workerParameters)
    }
}