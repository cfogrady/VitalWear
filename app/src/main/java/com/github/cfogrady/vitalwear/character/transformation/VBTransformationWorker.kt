package com.github.cfogrady.vitalwear.character.transformation

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import androidx.work.Worker
import com.github.cfogrady.vitalwear.character.VBUpdater
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VBTransformationWorker (
    private val characterManager: CharacterManager,
    private val notificationChannelManager: NotificationChannelManager,
    private val bemUpdater: VBUpdater,
    val context: Context,
    workerParams: WorkerParameters) : Worker(context, workerParams) {

    val TAG = "VBTransformationWorker"
    override fun doWork(): Result {
        Log.i(TAG, "Transforming!")
        val character = characterManager.getCurrentCharacter()
        if(character != null) {
            CoroutineScope(Dispatchers.IO).launch {
                character.prepCharacterTransformation(characterManager.fetchSupportCharacter(context))
                if(character.readyToTransform.value != null) {
                    notificationChannelManager.sendGenericNotification(context, "Transformation!", "Character is ready for transformation", NotificationChannelManager.TRANSFORMATION_READY_ID)
                } else {
                    bemUpdater.setupTransformationChecker(character)
                }
            }
        }
        return Result.success()
    }
}