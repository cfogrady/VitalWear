package com.github.cfogrady.vitalwear.logs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearCompanion
import com.github.cfogrady.vitalwear.util.ConfirmationComposableFactory

class LoadingLogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = this
        setContent {
            var accepted by remember { mutableStateOf(false) }
            if(!accepted) {
                ConfirmationComposableFactory.Confirmation(prompt = "WARNING: Logs have personal health information such as heart rate measurements and steps per day.") {
                    if(it) {
                        accepted = true
                    } else {
                        finish()
                    }
                }
            } else {
                Loading() {
                    val logService = (application as VitalWearCompanion).logService
                    logService.fetchWatchLogs(activity)
                }
            }
        }
    }
}