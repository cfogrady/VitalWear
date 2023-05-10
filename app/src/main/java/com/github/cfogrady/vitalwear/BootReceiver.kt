package com.github.cfogrady.vitalwear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.cfogrady.vitalwear.steps.SensorStepService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class BootReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "BootReceiver"
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
            Log.i(TAG, "Starting up the device")
            // Assume that application onCreate handled what we need
//            val applicationBootManager = (context!!.applicationContext as VitalWearApp).applicationBootManager
//            applicationBootManager.onStartup()
        } else {
            Log.w(TAG, "Called for unexpected intent: ${intent.action}")
        }
    }
}