package com.github.cfogrady.vitalwear.common.card.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AttributeFusionEntityDao {

    @Query("select * from ${AttributeFusionEntity.TABLE} where cardName = :cardName and fromCharacterId = :speciesId")
    fun findByCardAndSpeciesId(cardName: String, speciesId: Int): AttributeFusionEntity?

    @Insert
    fun insertAll(attributeFusionEntities: Collection<AttributeFusionEntity>)
}