package com.github.cfogrady.vitalwear.util

import android.os.Handler
import android.os.HandlerThread
import timber.log.Timber

class SensorThreadHandler {

    private val handlerThread = HandlerThread("Sensor Thread", Thread.MAX_PRIORITY)
    val handler: Handler

    init {
        Timber.i("Starting Sensor Thread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        Timber.i("Sensor Thread Started")
    }
}