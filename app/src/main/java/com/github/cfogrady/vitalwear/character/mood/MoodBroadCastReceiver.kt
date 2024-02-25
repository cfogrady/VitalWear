package com.github.cfogrady.vitalwear.character.mood

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.time.LocalDateTime

class MoodBroadcastReceiver(
    private val moodService: MoodService) : BroadcastReceiver() {
    companion object {
        const val MOOD_UPDATE = "MOOD_UPDATE_INTENT"
        const val TAG = "MoodBroadcastReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == MOOD_UPDATE) {
            Log.i(TAG, "MOOD_UPDATE_INTENT broadcast intent received")
            moodService.updateMood(LocalDateTime.now())
        }
    }
}