package com.github.cfogrady.vitalwear.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.cfogrady.vitalwear.adventure.data.CharacterAdventureEntity
import com.github.cfogrady.vitalwear.adventure.data.CharacterAdventureDao
import com.github.cfogrady.vitalwear.character.data.CharacterDao
import com.github.cfogrady.vitalwear.character.data.CharacterEntity
import com.github.cfogrady.vitalwear.character.transformation.history.TransformationHistoryDao
import com.github.cfogrady.vitalwear.character.transformation.history.TransformationHistoryEntity
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntityDao
import com.github.cfogrady.vitalwear.common.card.db.AttributeFusionEntity
import com.github.cfogrady.vitalwear.common.card.db.AttributeFusionEntityDao
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntity
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntityDao
import com.github.cfogrady.vitalwear.common.card.db.SpecificFusionEntity
import com.github.cfogrady.vitalwear.common.card.db.SpecificFusionEntityDao
import com.github.cfogrady.vitalwear.common.card.db.TransformationEntity
import com.github.cfogrady.vitalwear.common.card.db.TransformationEntityDao
import com.github.cfogrady.vitalwear.common.data.LocalDateTimeConverters
import com.github.cfogrady.vitalwear.common.data.migrations.DropClearedFromAdventure
import com.github.cfogrady.vitalwear.settings.CardSettingsDao
import com.github.cfogrady.vitalwear.settings.CardSettingsEntity
import com.github.cfogrady.vitalwear.settings.CharacterSettingsDao
import com.github.cfogrady.vitalwear.settings.CharacterSettingsEntity

@Database(entities = [
    CharacterEntity::class,
    SpeciesEntity::class,
    CardMetaEntity::class,
    TransformationEntity::class,
    AdventureEntity::class,
    AttributeFusionEntity::class,
    SpecificFusionEntity::class,
    CharacterSettingsEntity::class,
    CharacterAdventureEntity::class,
    TransformationHistoryEntity::class,
    CardSettingsEntity::class],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 2, to = 3, spec = DropClearedFromAdventure::class)
    ])
@TypeConverters(LocalDateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun speciesEntityDao(): SpeciesEntityDao
    abstract fun cardMetaEntityDao(): CardMetaEntityDao
    abstract fun transformationEntityDao(): TransformationEntityDao

    abstract fun adventureEntityDao(): AdventureEntityDao
    abstract fun attributeFusionEntityDao(): AttributeFusionEntityDao
    abstract fun specificFusionEntityDao(): SpecificFusionEntityDao

    abstract fun characterSettingsDao(): CharacterSettingsDao

    abstract fun characterAdventureDao(): CharacterAdventureDao

    abstract fun transformationHistoryDao(): TransformationHistoryDao

    abstract fun cardSettingsDao(): CardSettingsDao
}