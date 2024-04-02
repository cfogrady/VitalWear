package com.github.cfogrady.vitalwear.character.mood

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber
import java.time.LocalDateTime

class MoodBroadcastReceiver(
    private val moodService: MoodService) : BroadcastReceiver() {
    companion object {
        const val MOOD_UPDATE = "MOOD_UPDATE_INTENT"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == MOOD_UPDATE) {
            Timber.i("MOOD_UPDATE_INTENT broadcast intent received")
            moodService.updateMood(LocalDateTime.now())
        }
    }
}