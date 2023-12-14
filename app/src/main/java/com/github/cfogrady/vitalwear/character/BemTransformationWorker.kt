package com.github.cfogrady.vitalwear.character

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import androidx.work.Worker
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import java.time.LocalDateTime

class BemTransformationWorker (private val characterManager: CharacterManager, val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    val TAG = "BemTransformationWorker"
    override fun doWork(): Result {
        Log.i(TAG, "Transforming!")
        val character = characterManager.getCurrentCharacter()
        if(character != BEMCharacter.DEFAULT_CHARACTER) {
            character.prepCharacterTransformation()
            characterManager.doActiveCharacterTransformation(context)
        }
        return Result.success()
    }
}