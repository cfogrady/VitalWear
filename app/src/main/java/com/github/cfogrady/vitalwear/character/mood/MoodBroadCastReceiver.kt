package com.github.cfogrady.vitalwear.character.mood

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.steps.SensorStepService
import java.time.LocalDateTime

class MoodBroadcastReceiver(
    private val bemMoodUpdater: BEMMoodUpdater,
    private val characterManager: CharacterManager) : BroadcastReceiver() {
    companion object {
        const val MOOD_UPDATE = "MOOD_UPDATE_INTENT"
        const val TAG = "MoodBroadcastReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == MOOD_UPDATE) {
            val currentCharacter = characterManager.getCurrentCharacter()
            if(currentCharacter != null) {
                Log.i(TAG, "MOOD_UPDATE_INTENT broadcast received")
                //TODO: This may only work when we have the complication service running
                // Might need to start persistent service to do this.
                bemMoodUpdater.updateMood(currentCharacter, LocalDateTime.now())
            }
        }
    }
}