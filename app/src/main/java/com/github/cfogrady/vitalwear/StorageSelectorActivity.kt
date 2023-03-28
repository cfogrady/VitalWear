package com.github.cfogrady.vitalwear

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ScalingLazyColumn
import com.github.cfogrady.vb.dim.sprite.SpriteData
import com.github.cfogrady.vitalwear.data.FirmwareManager
import com.github.cfogrady.vitalwear.data.SpriteBitmapConverter
import java.io.File


class StorageSelectorActivity : ComponentActivity() {
    val TAG = "StorageSelectorActivity"
    val INITIAL_PATH = "InitialPath"
    private lateinit var firmwareManager: FirmwareManager
    private lateinit var spriteBitmapConverter: SpriteBitmapConverter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firmwareManager = (application as VitalWearApp).firmwareManager
        spriteBitmapConverter = (application as VitalWearApp).spriteBitmapConverter
        var partner = (application as VitalWearApp).characterManager.activePartner
        var file = applicationContext.filesDir
        Log.i("VitalWear", file.absolutePath)
        var testFile = File(file, "test.txt")
        testFile.createNewFile()
        setContent {
            ScalingLazyColumn(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize(),) {
                item {
                    Button(onClick = { Log.i("VitalWear", "onClick")}) {
                        Text(file.name, color = Color.Red, modifier = Modifier.padding(10.dp))
                    }
                }
                item {
                    var firmware = firmwareManager.firmware
                    var sprite = firmware.spriteData.get(287)
                    Image(bitmap = spriteBitmapConverter.getBitmap(sprite).asImageBitmap(), contentDescription = "Icon")
                }
                item {
                    Image(bitmap = partner.sprites.get(1).asImageBitmap(), contentDescription = "Partner")
                }
                if(file.isDirectory) {
                    Log.i("VitalWear", "directory")
                    for(child in file.listFiles()) {
                        Log.i("VitalWear", "child: " + child.name)
                        item {
                            Button(onClick = { Log.i("VitalWear", "child onClick")}) {
                                Text(child.name, color = Color.Blue, modifier = Modifier.padding(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}