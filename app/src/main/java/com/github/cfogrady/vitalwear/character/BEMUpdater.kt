package com.github.cfogrady.vitalwear.character

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.util.*

class BEMUpdater(val character : com.github.cfogrady.vitalwear.character.data.Character) {
    fun initializeBEM(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val moodWorkRequest = PeriodicWorkRequestBuilder<BemMoodWorker>(Duration.ofMinutes(5)).build()
        val transformWorkInput = Data.Builder().putString("test", "test").build()

        workManager.enqueue(moodWorkRequest)
        val workInfo = workManager.getWorkInfoByIdLiveData(UUID.randomUUID())
        val transformWorkRequest = OneTimeWorkRequestBuilder<BemTransformationWorker>()
            .setInitialDelay(Duration.ofSeconds(character.characterStats.timeUntilNextTransformation))
            .setInputData(transformWorkInput)

            .build()
        workManager.enqueue(transformWorkRequest)
    }
}