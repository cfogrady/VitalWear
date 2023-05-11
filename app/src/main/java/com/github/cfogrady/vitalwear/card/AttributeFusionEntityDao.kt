package com.github.cfogrady.vitalwear.card

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface AttributeFusionEntityDao {

    @Insert
    fun insert(attributeFusionEntity: AttributeFusionEntity)
}