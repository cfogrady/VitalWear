package com.github.cfogrady.vitalwear.character.transformation

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import androidx.work.Worker
import com.github.cfogrady.vitalwear.character.BEMUpdater
import com.github.cfogrady.vitalwear.character.CharacterManager
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
        if(character != null) {
            character.prepCharacterTransformation()
            if(character.readyToTransform.value.isPresent) {
                notificationChannelManager.sendGenericNotification(context, "Transformation!", "Character is ready for transformation", NotificationChannelManager.TRANSFORMATION_READY_ID)
            } else {
                bemUpdater.setupTransformationChecker(character)
            }
        }
        return Result.success()
    }
}