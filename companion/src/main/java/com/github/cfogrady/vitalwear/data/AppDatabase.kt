package com.github.cfogrady.vitalwear.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import com.github.cfogrady.vitalwear.card.ValidatedCardEntity
import com.github.cfogrady.vitalwear.card.ValidatedCardEntityDao
import com.github.cfogrady.vitalwear.common.data.LocalDateTimeConverters

@Database(entities = [
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
    abstract fun speciesEntityDao(): SpeciesEntityDao
    abstract fun cardMetaEntityDao(): CardMetaEntityDao
    abstract fun transformationEntityDao(): TransformationEntityDao

    abstract fun adventureEntityDao(): AdventureEntityDao
    abstract fun attributeFusionEntityDao(): AttributeFusionEntityDao
    abstract fun specificFusionEntityDao(): SpecificFusionEntityDao
    abstract fun validatedCardEntityDao(): ValidatedCardEntityDao
}