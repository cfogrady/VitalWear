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
import com.github.cfogrady.vitalwear.data.AppDatabase

class VitalWearCompanion : Application() {
    lateinit var cardLoader: CardLoader
    lateinit var validatedCardManager: ValidatedCardManager
    lateinit var cardMetaEntityDao: CardMetaEntityDao

    override fun onCreate() {
        super.onCreate()
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