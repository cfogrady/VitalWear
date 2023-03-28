package com.github.cfogrady.vitalwear.character

import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration

class BEMUpdater(val character : com.github.cfogrady.vitalwear.character.data.Character) {
    fun initializeBEM(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val workRequest = PeriodicWorkRequestBuilder<BemMoodWorker>(Duration.ofMinutes(5)).build()
        workManager.enqueue(workRequest)
    }
}