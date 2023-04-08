package com.github.cfogrady.vitalwear.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.activity.NewCardActivity
import com.github.cfogrady.vitalwear.data.FirmwareManager


class MainActivity : ComponentActivity() {
    private lateinit var firmwareManager: FirmwareManager
    private lateinit var characterManager: CharacterManager
    private lateinit var mainScreenComposable: MainScreenComposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        characterManager = (application as VitalWearApp).characterManager
        firmwareManager = (application as VitalWearApp).firmwareManager
        mainScreenComposable = (application as VitalWearApp).mainScreenComposable
        val intent = Intent(applicationContext, NewCardActivity::class.java)
        val contract = ActivityResultContracts.StartActivityForResult()
        contract.createIntent(applicationContext, intent)
        val newCharacterLauncher = registerForActivityResult(contract) {result ->
            //if(newCharacterWasSelected(result)) {
            finish()
            //}
        }
        setContent {
            mainScreenComposable.mainScreen {
                newCharacterLauncher.launch(intent)
            }
        }
    }
}