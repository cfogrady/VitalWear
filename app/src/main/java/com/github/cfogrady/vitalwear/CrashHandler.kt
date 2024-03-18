package com.github.cfogrady.vitalwear

import android.content.Context
import android.util.Log
import android.widget.Toast

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        Log.e("CrashHandler", "Thread ${t.name} failed: $e")
        Toast.makeText(context, "VitalWear Crashed. Restart and send bug report.", Toast.LENGTH_SHORT).show()
    }
}