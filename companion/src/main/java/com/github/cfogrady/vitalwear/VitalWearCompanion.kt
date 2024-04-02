package com.github.cfogrady.vitalwear

import android.app.Application
import androidx.room.Room
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vitalwear.common.card.CardLoader
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.common.card.CharacterSpritesIO
import com.github.cfogrady.vitalwear.common.card.SpriteBitmapConverter
import com.github.cfogrady.vitalwear.common.card.SpriteFileIO
import com.github.cfogrady.vitalwear.card.ValidatedCardManager
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.common.data.migrations.CreateAndPopulateMaxAdventureCompletionCardMeta
import com.github.cfogrady.vitalwear.common.log.TinyLogTree
import com.github.cfogrady.vitalwear.data.AppDatabase
import com.github.cfogrady.vitalwear.logs.LogService
import timber.log.Timber

class VitalWearCompanion : Application() {
    lateinit var cardLoader: CardLoader
    lateinit var validatedCardManager: ValidatedCardManager
    lateinit var cardMetaEntityDao: CardMetaEntityDao
    val logService = LogService()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.plant(TinyLogTree(this))
        Timber.i("Running VitalWear Companion. Version: ${BuildConfig.VERSION_NAME}  ${BuildConfig.VERSION_CODE}")
        Thread.setDefaultUncaughtExceptionHandler{thread, throwable ->
            Timber.e(throwable, "Thread ${thread.name} failed:")
        }
        buildDependencies()
    }

    private fun buildDependencies() {
        val database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "VitalWear")
            .addMigrations(CreateAndPopulateMaxAdventureCompletionCardMeta()).build()
        cardMetaEntityDao = database.cardMetaEntityDao()
        val spritesFileIO = SpriteFileIO()
        val spriteBitmapConverter = SpriteBitmapConverter()
        val characterSpritesIO = CharacterSpritesIO(spritesFileIO, spriteBitmapConverter)
        val cardSpritesIO = CardSpritesIO(spritesFileIO, spriteBitmapConverter)
        cardLoader = CardLoader(characterSpritesIO, cardSpritesIO, database.cardMetaEntityDao(), database.speciesEntityDao(), database.transformationEntityDao(), database.adventureEntityDao(), database.attributeFusionEntityDao(), database.specificFusionEntityDao(), DimReader())
        validatedCardManager = ValidatedCardManager(database.validatedCardEntityDao())
    }
}