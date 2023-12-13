package com.github.cfogrady.vitalwear.firmware

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.MutableLiveData
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.activity.FileExploreActivity
import com.github.cfogrady.vitalwear.activity.PROMPT_TEXT
import com.github.cfogrady.vitalwear.activity.SELECTED_FILE
import com.github.cfogrady.vitalwear.util.ActivityHelper

class LoadFirmwareActivity  : ComponentActivity() {
    companion object {
        val TAG = "FileExploreActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firmwareManager = (application as VitalWearApp).firmwareManager
        val activityHelper = ActivityHelper(this)
        val loading = MutableLiveData(false)
        val fileExplorerActivity = activityHelper.getActivityLauncherWithResultHandling(
            activityClass = FileExploreActivity::class.java,
            addToIntent = {intent ->
                intent.putExtra(PROMPT_TEXT, "Select Firmware File")
            }
        ) { result ->
            val file = result.data?.getStringExtra(SELECTED_FILE)
            if (file == null) {
                finish()
            } else {
                loading.postValue(true)
                firmwareManager.storeFirmware(applicationContext, file).invokeOnCompletion {
                    finish()
                }
            }
        }
        setContent {
            LaunchedEffect(true) {
                fileExplorerActivity.invoke()
            }
            val loadingState by loading.observeAsState()
            if (loadingState!!) {
                Loading(loadingText = "Loading firmware..."){}
            }
        }
    }
}