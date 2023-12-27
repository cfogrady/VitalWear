package com.github.cfogrady.vitalwear.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.cfogrady.vitalwear.character.data.CharacterDao
import com.github.cfogrady.vitalwear.character.data.CharacterEntity
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
import com.github.cfogrady.vitalwear.common.card.db.ValidatedCardEntity
import com.github.cfogrady.vitalwear.common.card.db.ValidatedCardEntityDao

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