package com.github.cfogrady.vitalwear

import android.app.Application
import androidx.room.Room
import androidx.work.Configuration
import com.github.cfogrady.vitalwear.character.BEMUpdater
import com.github.cfogrady.vitalwear.data.CardLoader
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.PreviewCharacterManager
import com.github.cfogrady.vitalwear.complications.PartnerComplicationState
import com.github.cfogrady.vitalwear.data.*

class VitalWearApp : Application(), Configuration.Provider {
    val spriteBitmapConverter = SpriteBitmapConverter()
    val firmwareManager = FirmwareManager(spriteBitmapConverter)
    val partnerComplicationState = PartnerComplicationState()
    lateinit var cardLoader : CardLoader
    lateinit var database : AppDatabase
    lateinit var characterManager: CharacterManager
    lateinit var previewCharacterManager: PreviewCharacterManager

    override fun onCreate() {
        super.onCreate()
        //TODO: Remove allowMainThread before release
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "VitalWear").allowMainThreadQueries().build()
        firmwareManager.loadFirmware(applicationContext)
        cardLoader = CardLoader(applicationContext, spriteBitmapConverter)
        characterManager = CharacterManager()
        characterManager.init(database.characterDao(), cardLoader, BEMUpdater(applicationContext))
        previewCharacterManager = PreviewCharacterManager(database.characterDao(), cardLoader)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        //characterManager = CharacterManager()
        return Configuration.Builder().setWorkerFactory(VitalWearWorkerFactory(characterManager)).build()
    }
}