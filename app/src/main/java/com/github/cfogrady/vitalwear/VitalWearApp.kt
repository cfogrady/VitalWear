package com.github.cfogrady.vitalwear

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.github.cfogrady.vitalwear.data.CardLoader
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.data.*
import kotlinx.coroutines.launch

class VitalWearApp : Application() {
    val firmwareManager = FirmwareManager()
    val spriteBitmapConverter = SpriteBitmapConverter()
    lateinit var cardLoader : CardLoader
    lateinit var database : AppDatabase
    lateinit var characterManager : CharacterManager

    override fun onCreate() {
        super.onCreate()
        //TODO: Remove allowMainThread before release
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "VitalWear").allowMainThreadQueries().build()
        firmwareManager.loadFirmware(applicationContext)
        cardLoader = CardLoader(applicationContext, spriteBitmapConverter)
        characterManager = CharacterManager(database.characterDao(), cardLoader)
        characterManager.loadActive()
//        ProcessLifecycleOwner.get().lifecycleScope.launch {
//
//        }
    }
}