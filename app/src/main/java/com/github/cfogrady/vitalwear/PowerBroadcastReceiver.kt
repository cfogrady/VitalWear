package com.github.cfogrady.vitalwear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class PowerBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.i("Action: ${intent?.action}")
    }
}