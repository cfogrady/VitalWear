package com.github.cfogrady.vitalwear.character

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.mood.MoodUpdateWorker
import java.time.Duration

class BEMUpdater(val context: Context, val workManager: WorkManager = WorkManager.getInstance(context)) {
    companion object {
        const val WORK_TAG = "BEMUpdater"
    }

    fun initializeBEMUpdates(character: BEMCharacter) {

//        val moodWorkRequest = PeriodicWorkRequestBuilder<BemMoodWorker>(Duration.ofMinutes(5)).build()
//        workManager.enqueue(moodWorkRequest)
        val transformWorkRequest = OneTimeWorkRequestBuilder<BemTransformationWorker>()
            .setInitialDelay(Duration.ofSeconds(character.characterStats.timeUntilNextTransformation))
            .addTag(WORK_TAG)
            .build()
        workManager.enqueue(transformWorkRequest)

        val moodUpdateWorkRequest = PeriodicWorkRequestBuilder<MoodUpdateWorker>(Duration.ofMinutes(5))
            .addTag(WORK_TAG)
            .build()
        workManager.enqueue(moodUpdateWorkRequest)
    }

    fun cancel() {
        workManager.cancelAllWorkByTag(WORK_TAG)
    }
}