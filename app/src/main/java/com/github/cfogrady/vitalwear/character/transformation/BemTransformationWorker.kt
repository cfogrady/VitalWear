package com.github.cfogrady.vitalwear.character.transformation

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import androidx.work.Worker
import com.github.cfogrady.vitalwear.character.BEMUpdater
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntityDao
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BemTransformationWorker (
    private val characterManager: CharacterManager,
    private val notificationChannelManager: NotificationChannelManager,
    private val bemUpdater: BEMUpdater,
    private val adventureEntityDao: AdventureEntityDao,
    val context: Context,
    workerParams: WorkerParameters) : Worker(context, workerParams) {

    val TAG = "BemTransformationWorker"
    override fun doWork(): Result {
        Log.i(TAG, "Transforming!")
        val character = characterManager.getCurrentCharacter()
        if(character != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val adventures = adventureEntityDao.getByCard(character.cardName())
                val highestCompleted = AdventureEntity.highestAdventureCompleted(adventures)
                // get support... maybe move to characterManager
                character.prepCharacterTransformation(highestCompleted)
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