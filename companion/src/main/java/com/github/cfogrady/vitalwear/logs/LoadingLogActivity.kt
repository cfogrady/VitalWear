package com.github.cfogrady.vitalwear.logs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearCompanion

class LoadingLogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = this
        setContent {
            Loading() {
                val logService = (application as VitalWearCompanion).logService
                logService.fetchWatchLogs(activity)
            }
        }
    }
}