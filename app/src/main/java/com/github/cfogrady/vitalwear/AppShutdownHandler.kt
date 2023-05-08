package com.github.cfogrady.vitalwear

import android.content.SharedPreferences
import android.util.Log
import java.time.LocalDateTime

class AppShutdownHandler(private val shutdownManager: ShutdownManager, private val sharedPreferences: SharedPreferences) : Thread() {
    companion object {
        private const val TAG = "AppShutdownHandler"
        const val GRACEFUL_SHUTDOWNS_KEY = "GRACEFUL_SHUTDOWNS"
    }

    override fun run() {
        sharedPreferences.edit().putInt(GRACEFUL_SHUTDOWNS_KEY, sharedPreferences.getInt(
            GRACEFUL_SHUTDOWNS_KEY, 0) + 1)
        Log.i(TAG, "App is shutting down")
        shutdownManager.shutdown()
    }
}