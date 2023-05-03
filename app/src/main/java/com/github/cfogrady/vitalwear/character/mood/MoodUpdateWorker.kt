package com.github.cfogrady.vitalwear.character.mood

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.character.BEMUpdater
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import com.github.cfogrady.vitalwear.util.CompletableFutureListenableWrapper
import com.google.common.util.concurrent.ListenableFuture
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

class MoodUpdateWorker  (private val characterManager: CharacterManager, private val bemMoodUpdater: BEMMoodUpdater, private val bemUpdater: BEMUpdater, context: Context, workerParams: WorkerParameters) : ListenableWorker(context, workerParams) {
    companion object {
        const val TAG = "MoodUpdateWorker"
    }

    override fun startWork(): ListenableFuture<Result> {
        Log.i(TAG, "Starting Mood Update")
        val currentCharacter = characterManager.getCurrentCharacter()
        if(currentCharacter == BEMCharacter.DEFAULT_CHARACTER) {
            // no character to update
            return CompletableFutureListenableWrapper(CompletableFuture.completedFuture(Result.success()))
        }
        val fullWorkFuture = bemMoodUpdater.updateMood(currentCharacter, LocalDateTime.now()).thenApply {
            bemUpdater.queueNextMoodUpdate()
            Result.success()
        }
        return CompletableFutureListenableWrapper(fullWorkFuture)
    }
}