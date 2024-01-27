package com.github.cfogrady.vitalwear.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.github.cfogrady.vb.dim.card.DimWriter
import com.github.cfogrady.vitalwear.common.commonLog
import com.github.cfogrady.vitalwear.card.ImportCardActivity
import com.github.cfogrady.vitalwear.common.communication.ChannelTypes
import com.github.cfogrady.vitalwear.firmware.FirmwareImportActivity
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.charset.Charset

class MainActivity : ComponentActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commonLog("Test shared data")
        setContent {
            MainComposable()
        }
    }

    @Preview
    @Composable fun MainComposable() {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                startActivity(Intent(applicationContext, ImportCardActivity::class.java))
            }) {
                Text(text = "Import Card Image", color = Color.Cyan)
            }
            Button(onClick = {
                startActivity(Intent(applicationContext, FirmwareImportActivity::class.java))
            }) {
                Text(text = "Import Firmware", color = Color.Cyan)
            }
        }
    }
}