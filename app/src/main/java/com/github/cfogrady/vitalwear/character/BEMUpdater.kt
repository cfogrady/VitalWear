package com.github.cfogrady.vitalwear.character

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import java.time.Duration

class BEMUpdater(val context: Context, val workManager: WorkManager = WorkManager.getInstance(context)) {
    fun initializeBEMUpdates(character: BEMCharacter) {

//        val moodWorkRequest = PeriodicWorkRequestBuilder<BemMoodWorker>(Duration.ofMinutes(5)).build()
//        workManager.enqueue(moodWorkRequest)
        val transformWorkRequest = OneTimeWorkRequestBuilder<BemTransformationWorker>()
            .setInitialDelay(Duration.ofSeconds(character.characterStats.timeUntilNextTransformation))
            .build()
        workManager.enqueue(transformWorkRequest)
    }

    fun cancel() {
        workManager.cancelAllWork()
    }
}