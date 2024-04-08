package com.github.cfogrady.vitalwear

import android.content.Context
import android.widget.Toast
import com.github.cfogrady.vitalwear.common.log.TinyLogTree
import com.github.cfogrady.vitalwear.log.LogSettings
import timber.log.Timber

class CrashHandler(private val context: Context, private val logSettings: LogSettings, private val originalHandler: Thread.UncaughtExceptionHandler? ) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        if(!logSettings.loggingEnabled()) {
            logSettings.toggleLogging()
        }
        Timber.e("Vital Wear version ${BuildConfig.VERSION_NAME}   ${BuildConfig.VERSION_CODE} crashed.")
        Timber.e(e, "Thread ${t.name} failed:")
        Toast.makeText(context, "VitalWear Crashed. Restarting.", Toast.LENGTH_SHORT).show()
        TinyLogTree.shutdown()
        originalHandler?.uncaughtException(t, e)
    }
}