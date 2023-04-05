package com.github.cfogrady.vitalwear.character

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

interface WorkerProvider {
    fun createWorker(characterManager: CharacterManager, appContext: Context, workerParameters: WorkerParameters) : Worker
}