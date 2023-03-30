package com.github.cfogrady.vitalwear.character

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.Worker
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.character.data.Character

class BemTransformationWorker (val context: Context, val workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        context.
        characterManager.prepCharacterTransformation(characterManager.activePartner)
        characterManager.doCharacterTransformation()
        return Result.success()
    }
}