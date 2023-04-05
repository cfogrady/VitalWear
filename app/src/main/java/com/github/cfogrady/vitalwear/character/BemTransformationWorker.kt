package com.github.cfogrady.vitalwear.character

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import androidx.work.Worker

class BemTransformationWorker (private val characterManager: CharacterManager, val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    val TAG = "BemTransformationWorker"
    override fun doWork(): Result {
        Log.i(TAG, "Transforming!")
        val liveCharacter = characterManager.getActiveCharacter()
        if(liveCharacter.value != null) {
            val character = liveCharacter.value!!
            character.prepCharacterTransformation()
            characterManager.doActiveCharacterTransformation()
        }
        return Result.success()
    }
}