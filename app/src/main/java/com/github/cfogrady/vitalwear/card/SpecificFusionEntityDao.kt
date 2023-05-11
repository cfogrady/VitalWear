package com.github.cfogrady.vitalwear.card

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface SpecificFusionEntityDao {
    @Insert
    fun insert(specificFusionEntity: SpecificFusionEntity)
}