package com.github.cfogrady.vitalwear.character

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.Worker
import com.github.cfogrady.vitalwear.character.data.Character

class BemTransformationWorker (val character: Character, context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        character.readyToTransform = true
        return Result.success()
    }
}