package com.github.cfogrady.vitalwear.util

import android.os.Handler
import android.os.HandlerThread

class SensorThreadHandler {
    private val handlerThread = HandlerThread("Sensor Thread", Thread.MAX_PRIORITY)
    val handler: Handler

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }
}