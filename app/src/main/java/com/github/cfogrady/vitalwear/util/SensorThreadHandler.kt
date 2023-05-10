package com.github.cfogrady.vitalwear.util

import android.os.Handler
import android.os.HandlerThread
import android.util.Log

class SensorThreadHandler {
    companion object {
        const val TAG = "SensorThreadHandler"
    }

    private val handlerThread = HandlerThread("Sensor Thread", Thread.MAX_PRIORITY)
    val handler: Handler

    init {
        Log.i(TAG, "Starting Sensor Thread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        Log.i(TAG, "Sensor Thread Started")
    }
}