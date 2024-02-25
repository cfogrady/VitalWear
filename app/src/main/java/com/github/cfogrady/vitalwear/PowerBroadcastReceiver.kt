package com.github.cfogrady.vitalwear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PowerBroadcastReceiver: BroadcastReceiver() {
    companion object {
        const val TAG = "PowerBroadcastReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "Action: ${intent?.action}")
    }
}