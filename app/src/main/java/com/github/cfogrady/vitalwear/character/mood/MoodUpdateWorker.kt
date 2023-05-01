package com.github.cfogrady.vitalwear.character.mood

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import com.github.cfogrady.vitalwear.util.CompletableFutureListenableWrapper
import com.google.common.util.concurrent.ListenableFuture
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

class MoodUpdateWorker  (private val characterManager: CharacterManager, private val bemMoodUpdater: BEMMoodUpdater, context: Context, workerParams: WorkerParameters) : ListenableWorker(context, workerParams) {
    companion object {
        const val TAG = "MoodUpdateWorker"
    }

    override fun startWork(): ListenableFuture<Result> {
        Log.i(TAG, "Starting Mood Update")
        val liveCharacter = characterManager.getLiveCharacter()
        if(liveCharacter.value == BEMCharacter.DEFAULT_CHARACTER) {
            // no character to update
            return CompletableFutureListenableWrapper(CompletableFuture.completedFuture(Result.success()))
        }
        val character = liveCharacter.value!!
        val fullWorkFuture = bemMoodUpdater.updateMood(character, LocalDateTime.now()).thenApply { Result.success() }
        return CompletableFutureListenableWrapper(fullWorkFuture)
    }
}