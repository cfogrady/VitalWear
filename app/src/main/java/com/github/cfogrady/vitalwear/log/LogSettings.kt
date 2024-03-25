package com.github.cfogrady.vitalwear.log

import android.content.SharedPreferences
import timber.log.Timber

class LogSettings(private val sharedPreferences: SharedPreferences, private val tinyLogTree: TinyLogTree) {
    companion object {
        const val LOGGING_ENABLED = "LOGGING_ENABLED"
    }
    val debugTree = Timber.DebugTree()

    fun loggingEnabled(): Boolean {
        return sharedPreferences.getBoolean(LOGGING_ENABLED, false)
    }

    fun toggleLogging(): Boolean {
        var enabled = sharedPreferences.getBoolean(LOGGING_ENABLED, false)
        enabled = !enabled
        sharedPreferences.edit().putBoolean(LOGGING_ENABLED, enabled).apply()
        setupLogging(enabled)
        return enabled
    }

    fun setupLogging() {
        setupLogging(loggingEnabled())
    }

    private fun setupLogging(loggingEnabled: Boolean) {
        Timber.uprootAll()
        if(loggingEnabled) {
            Timber.plant(debugTree)
            Timber.plant(tinyLogTree)
        }
    }
}