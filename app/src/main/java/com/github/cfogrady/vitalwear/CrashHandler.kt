package com.github.cfogrady.vitalwear

import android.content.Context
import android.widget.Toast
import com.github.cfogrady.vitalwear.log.LogSettings
import timber.log.Timber

class CrashHandler(private val context: Context, private val logSettings: LogSettings) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        if(!logSettings.loggingEnabled()) {
            logSettings.toggleLogging()
        }
        Timber.e(e, "Thread ${t.name} failed:")
        Toast.makeText(context, "VitalWear Crashed. Restart and send bug report from phone.", Toast.LENGTH_SHORT).show()
    }
}