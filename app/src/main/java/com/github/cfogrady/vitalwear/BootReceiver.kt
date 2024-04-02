package com.github.cfogrady.vitalwear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
            Timber.i("Starting up the device")
            // Assume that application onCreate handled what we need
//            val applicationBootManager = (context!!.applicationContext as VitalWearApp).applicationBootManager
//            applicationBootManager.onStartup()
        } else {
            Timber.w("Called for unexpected intent: ${intent.action}")
        }
    }
}