package com.github.cfogrady.vitalwear.character

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.mood.MoodUpdateWorker
import java.time.Duration

class BEMUpdater(val context: Context) {
    companion object {
        const val WORK_TAG = "BEMUpdater"
    }

    fun initializeBEMUpdates(character: BEMCharacter, workManager: WorkManager = WorkManager.getInstance(context)) {
        cancel(workManager)
        val durationUntilTransformUpdate = Duration.ofSeconds(character.characterStats.timeUntilNextTransformation)
        Log.i(WORK_TAG, "Queue Transform Update after $durationUntilTransformUpdate")
        val transformWorkRequest = OneTimeWorkRequestBuilder<BemTransformationWorker>()
            .setInitialDelay(durationUntilTransformUpdate)
            .addTag(WORK_TAG)
            .build()
        workManager.enqueue(transformWorkRequest)

        queueNextMoodUpdate(workManager)
    }

    fun queueNextMoodUpdate(workManager: WorkManager = WorkManager.getInstance(context)) {
        // Can't use periodic work request because minimum duraction is PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS
        // which is 15 minutes... Instead do a OneTimeWorkRequest that reschedules itself every 5.
        Log.i(WORK_TAG, "Queue Mood Update")
        val moodUpdateWorkRequest = OneTimeWorkRequestBuilder<MoodUpdateWorker>()
            .setInitialDelay(Duration.ofMinutes(5))
            .addTag(WORK_TAG)
            .build()
        workManager.enqueue(moodUpdateWorkRequest)
    }

    fun cancel(workManager: WorkManager = WorkManager.getInstance(context)) {
        workManager.cancelAllWorkByTag(WORK_TAG)
    }
}