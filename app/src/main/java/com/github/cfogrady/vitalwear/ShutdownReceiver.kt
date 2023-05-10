package com.github.cfogrady.vitalwear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.time.LocalDateTime

class ShutdownReceiver(private val shutdownManager: ShutdownManager) :BroadcastReceiver() {
    companion object {
        const val TAG = "ShutdownReceiver"
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent!!.action == Intent.ACTION_SHUTDOWN) {
            Log.i(TAG, "Shutting down the device")
            shutdownManager.shutdown()
        } else {
            Log.w(TAG, "Called for unexpected intent: ${intent!!.action}")
        }
    }
}