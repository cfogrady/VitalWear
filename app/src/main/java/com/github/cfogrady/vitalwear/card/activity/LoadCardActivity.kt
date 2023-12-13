package com.github.cfogrady.vitalwear.card.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.activity.FileExploreActivity
import com.github.cfogrady.vitalwear.activity.PROMPT_TEXT
import com.github.cfogrady.vitalwear.activity.SELECTED_FILE
import com.github.cfogrady.vitalwear.card.CardLoader
import com.github.cfogrady.vitalwear.card.NewCardLoader
import com.github.cfogrady.vitalwear.character.activity.NEW_CHARACTER_SELECTED_FLAG
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