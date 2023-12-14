package com.github.cfogrady.vitalwear.character

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.mood.MoodBroadcastReceiver
import java.time.Duration

class BEMUpdater(val context: Context) {
    companion object {
        const val WORK_TAG = "BEMUpdater"
    }

    fun initializeBEMUpdates(character: BEMCharacter, workManager: WorkManager = WorkManager.getInstance(context)) {
        cancel(workManager)
        if(character.transformationOptions.isNotEmpty()) { //don't queue up if no transformations are possible
            val durationUntilTransformUpdate = Duration.ofSeconds(character.characterStats.timeUntilNextTransformation)
            Log.i(WORK_TAG, "Queue Transform Update after $durationUntilTransformUpdate")
            val transformWorkRequest = OneTimeWorkRequestBuilder<BemTransformationWorker>()
                .setInitialDelay(durationUntilTransformUpdate)
                .addTag(WORK_TAG)
                .build()
            workManager.enqueue(transformWorkRequest)
        }
    }

    fun scheduleExactMoodUpdates() {
        val alarmManager = context.getSystemService(Service.ALARM_SERVICE) as AlarmManager
        val now = SystemClock.elapsedRealtime()
        val period = Duration.ofMinutes(5).toMillis()
        val moodUpdateIntent = PendingIntent.getBroadcast(context, 0, Intent(MoodBroadcastReceiver.MOOD_UPDATE), 0)
        alarmManager.cancel(moodUpdateIntent)
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, now + period, period, moodUpdateIntent)
    }

    fun cancel(workManager: WorkManager = WorkManager.getInstance(context)) {
        workManager.cancelAllWorkByTag(WORK_TAG)
    }
}