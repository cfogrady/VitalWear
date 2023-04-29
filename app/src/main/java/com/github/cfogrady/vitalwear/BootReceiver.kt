package com.github.cfogrady.vitalwear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.cfogrady.vitalwear.steps.SensorStepService
import java.time.LocalDate

class BootReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "BootReceiver"
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
            Log.i(TAG, "Starting up the device")
            val sensorStepService = (context!!.applicationContext as VitalWearApp).stepService
            sensorStepService.handleBoot(LocalDate.now())
        } else {
            Log.w(TAG, "Called for unexpected intent: ${intent.action}")
        }
    }
}