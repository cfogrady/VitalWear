package com.github.cfogrady.vitalwear.main

import android.content.Intent
import android.os.Bundle
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
import com.github.cfogrady.vitalwear.VitalWearCompanion
import com.github.cfogrady.vitalwear.card.ImportCardActivity
import com.github.cfogrady.vitalwear.common.log.TinyLogTree
import com.github.cfogrady.vitalwear.firmware.FirmwareImportActivity
import com.github.cfogrady.vitalwear.logs.LoadingLogActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            Button(onClick = {
                startActivity(Intent(applicationContext, LoadingLogActivity::class.java))
            }) {
                Text(text = "Send Watch Logs", color = Color.Cyan)
            }
            Button(onClick = {
                val logService = (application as VitalWearCompanion).logService
                val file = TinyLogTree.getMostRecentLogFile(applicationContext)
                if(file == null) {
                    Toast.makeText(applicationContext, "No log files found", Toast.LENGTH_SHORT).show()
                } else {
                    logService.sendLogFile(applicationContext, file, this@MainActivity)
                }
            }) {
                Text(text = "Send Phone Logs", color = Color.Cyan)
            }
        }
    }
}