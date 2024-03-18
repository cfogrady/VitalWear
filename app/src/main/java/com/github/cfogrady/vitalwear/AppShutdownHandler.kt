package com.github.cfogrady.vitalwear

import android.content.SharedPreferences
import timber.log.Timber

class AppShutdownHandler(private val shutdownManager: ShutdownManager, private val sharedPreferences: SharedPreferences) : Thread() {
    companion object {
        const val GRACEFUL_SHUTDOWNS_KEY = "GRACEFUL_SHUTDOWNS"
    }

    override fun run() {
        sharedPreferences.edit().putInt(GRACEFUL_SHUTDOWNS_KEY, sharedPreferences.getInt(
            GRACEFUL_SHUTDOWNS_KEY, 0) + 1)
        Timber.i("App is shutting down")
        shutdownManager.shutdown()
    }
}