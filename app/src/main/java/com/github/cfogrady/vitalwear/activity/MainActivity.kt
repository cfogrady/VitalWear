package com.github.cfogrady.vitalwear.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.activity.CharacterSelectActivity
import com.github.cfogrady.vitalwear.character.activity.NewCardActivity
import com.github.cfogrady.vitalwear.data.FirmwareManager
import com.github.cfogrady.vitalwear.databinding.ActivityMainBinding


class MainActivity : ComponentActivity() {
    private lateinit var firmwareManager: FirmwareManager
    private lateinit var characterManager: CharacterManager
    private lateinit var mainScreenComposable: MainScreenComposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        characterManager = (application as VitalWearApp).characterManager
        firmwareManager = (application as VitalWearApp).firmwareManager
        mainScreenComposable = (application as VitalWearApp).mainScreenComposable
        setContent {
            mainScreenComposable.mainScreen {
                val intent = Intent(applicationContext, NewCardActivity::class.java)
                val contract = ActivityResultContracts.StartActivityForResult()
                contract.createIntent(applicationContext, intent)
                val newCharacterLauncher = registerForActivityResult(contract) {result ->
                    //if(newCharacterWasSelected(result)) {
                        finish()
                    //}
                }
            }
        }
    }
}