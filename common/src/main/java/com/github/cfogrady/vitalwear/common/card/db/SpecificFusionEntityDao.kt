package com.github.cfogrady.vitalwear.common.card.db

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface SpecificFusionEntityDao {
    @Insert
    fun insertAll(specificFusionEntity: Collection<SpecificFusionEntity>)
}