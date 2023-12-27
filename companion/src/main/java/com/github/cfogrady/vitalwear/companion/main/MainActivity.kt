package com.github.cfogrady.vitalwear.companion.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vitalwear.common.commonLog
import com.github.cfogrady.vitalwear.companion.VitalWearCompanion
import com.github.cfogrady.vitalwear.companion.card.ImportCardActivity
import java.io.File

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
        }
    }

    private fun importCard() {

    }
}