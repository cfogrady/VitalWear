package com.github.cfogrady.vitalwear.common.card.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SpecificFusionEntityDao {

    @Query("select * from ${SpecificFusionEntity.TABLE} where cardName = :cardName and fromCharacterId = :speciesId")
    fun findByCardAndSpeciesId(cardName: String, speciesId: Int): List<SpecificFusionEntity>

    @Insert
    fun insertAll(specificFusionEntity: Collection<SpecificFusionEntity>)
}