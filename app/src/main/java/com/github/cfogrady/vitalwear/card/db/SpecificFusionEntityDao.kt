package com.github.cfogrady.vitalwear.card.db

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface SpecificFusionEntityDao {
    @Insert
    fun insertAll(specificFusionEntity: Collection<SpecificFusionEntity>)
}