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
import com.github.cfogrady.vitalwear.character.mood.MoodBroadcastReceiver
import com.github.cfogrady.vitalwear.character.transformation.VBTransformationWorker
import java.time.Duration

class VBUpdater(val context: Context) {
    companion object {
        const val WORK_TAG = "VBUpdater"
    }

    fun setupTransformationChecker(character: VBCharacter, workManager: WorkManager = WorkManager.getInstance(context)) {
        cancel(workManager)
        Log.i(WORK_TAG, "Setup Transformation Check")
        if(character.hasPotentialTransformations()) { //don't queue up if no transformations are possible
            val durationUntilTransformUpdate = Duration.ofSeconds(character.characterStats.timeUntilNextTransformation)
            Log.i(WORK_TAG, "Queue Transform Update after $durationUntilTransformUpdate")
            val transformWorkRequest = OneTimeWorkRequestBuilder<VBTransformationWorker>()
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
        val moodUpdateIntent = PendingIntent.getBroadcast(context, 0, Intent(MoodBroadcastReceiver.MOOD_UPDATE),
            PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(moodUpdateIntent)
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, now + period, period, moodUpdateIntent)
    }

    fun cancel(workManager: WorkManager = WorkManager.getInstance(context)) {
        workManager.cancelAllWorkByTag(WORK_TAG)
    }
}