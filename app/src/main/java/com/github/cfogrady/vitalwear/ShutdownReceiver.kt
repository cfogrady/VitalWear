package com.github.cfogrady.vitalwear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class ShutdownReceiver(private val shutdownManager: ShutdownManager) :BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent!!.action == Intent.ACTION_SHUTDOWN) {
            Timber.i("Shutting down the device")
            shutdownManager.shutdown()
        } else {
            Timber.w("Called for unexpected intent: ${intent!!.action}")
        }
    }
}