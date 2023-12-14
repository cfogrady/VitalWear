package com.github.cfogrady.vitalwear.card.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.activity.FileExploreActivity
import com.github.cfogrady.vitalwear.activity.PROMPT_TEXT
import com.github.cfogrady.vitalwear.activity.SELECTED_FILE
import com.github.cfogrady.vitalwear.card.NewCardLoader
import com.github.cfogrady.vitalwear.util.ActivityHelper
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream


const val TAG = "LoadCardActivity"
class LoadCardActivity : ComponentActivity() {

    lateinit var cardLoader : NewCardLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cardLoader = (application as VitalWearApp).newCardLoader
        val activityHelper = ActivityHelper(this)
        activityHelper.getActivityLauncherWithResultHandling(FileExploreActivity::class.java, addToIntent = {
            it.putExtra(PROMPT_TEXT, "Select card image to load")
        }, onResult = ::handleFileExplorerResult).invoke()
        setContent {
            Loading(loadingText = "Importing Card") {}
        }
    }
    private fun handleFileExplorerResult(result: ActivityResult) {
        val fileStr = result.data?.getStringExtra(SELECTED_FILE)
        if (fileStr != null) {
            val file = File(fileStr)
            if (!file.exists() || !file.isFile) {
                Log.w(TAG,
                    "Expected card image at $fileStr is not a file or does not exist!"
                )
                finish()
            } else {
                GlobalScope.launch(Dispatchers.IO) {
                    FileInputStream(file).use {inputStream ->
                        cardLoader.importCardImage(applicationContext, file.name, inputStream, false)
                    }
                    finish()
                }
            }
        } else {
            finish()
        }
    }
}