package com.github.cfogrady.vitalwear

import android.content.Context
import android.widget.Toast
import timber.log.Timber

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        Timber.e("Thread ${t.name} failed: $e")
        Toast.makeText(context, "VitalWear Crashed. Restart and send bug report.", Toast.LENGTH_SHORT).show()
    }
}