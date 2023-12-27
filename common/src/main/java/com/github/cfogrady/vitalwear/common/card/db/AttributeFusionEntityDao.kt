package com.github.cfogrady.vitalwear.common.card.db

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface AttributeFusionEntityDao {

    @Insert
    fun insertAll(attributeFusionEntities: Collection<AttributeFusionEntity>)
}