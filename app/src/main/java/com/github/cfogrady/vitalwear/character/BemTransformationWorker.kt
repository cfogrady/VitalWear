package com.github.cfogrady.vitalwear.character

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import androidx.work.Worker
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager

class BemTransformationWorker (
    private val characterManager: CharacterManager,
    private val notificationChannelManager: NotificationChannelManager,
    private val bemUpdater: BEMUpdater,
    val context: Context,
    workerParams: WorkerParameters) : Worker(context, workerParams) {

    val TAG = "BemTransformationWorker"
    override fun doWork(): Result {
        Log.i(TAG, "Transforming!")
        val character = characterManager.getCurrentCharacter()
        if(character != BEMCharacter.DEFAULT_CHARACTER) {
            character.prepCharacterTransformation()
            if(characterManager.getCurrentCharacter().readyToTransform.isPresent) {
                notificationChannelManager.sendGenericNotification(context, "Transformation!", "Character is ready for transformation")
                characterManager.doActiveCharacterTransformation(context)
            } else {
                bemUpdater.initializeBEMUpdates(character)
            }
        }
        return Result.success()
    }
}