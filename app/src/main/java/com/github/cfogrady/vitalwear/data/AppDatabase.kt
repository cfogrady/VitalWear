package com.github.cfogrady.vitalwear.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.cfogrady.vitalwear.card.*
import com.github.cfogrady.vitalwear.card.db.*
import com.github.cfogrady.vitalwear.character.data.CharacterDao
import com.github.cfogrady.vitalwear.character.data.CharacterEntity

@Database(entities = [
    CharacterEntity::class,
    SpeciesEntity::class,
    CardMetaEntity::class,
    TransformationEntity::class,
    AdventureEntity::class,
    AttributeFusionEntity::class,
    SpecificFusionEntity::class,
    ValidatedCardEntity::class],
    version = 1)
@TypeConverters(LocalDateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun speciesEntityDao(): SpeciesEntityDao
    abstract fun cardMetaEntityDao(): CardMetaEntityDao
    abstract fun transformationEntityDao(): TransformationEntityDao

    abstract fun adventureEntityDao(): AdventureEntityDao
    abstract fun attributeFusionEntityDao(): AttributeFusionEntityDao
    abstract fun specificFusionEntityDao(): SpecificFusionEntityDao
    abstract fun validatedCardEntityDao(): ValidatedCardEntityDao
}