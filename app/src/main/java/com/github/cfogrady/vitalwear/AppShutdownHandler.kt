package com.github.cfogrady.vitalwear

import android.content.SharedPreferences
import timber.log.Timber

class AppShutdownHandler(private val shutdownManager: ShutdownManager) : Thread() {

    override fun run() {
        Timber.i("App is shutting down")
        shutdownManager.shutdown()
    }
}