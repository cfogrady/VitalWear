package com.github.cfogrady.vitalwear.card

import androidx.room.*

@Dao
interface SpeciesEntityDao {
    @Query("select * from ${SpeciesEntity.TABLE} where cardName = :cardName and characterId = :characterId limit 1")
    fun getCharacterByCardAndCharacterId(cardName: String, characterId: Int): SpeciesEntity

    @Insert
    fun insertAll(speciesEntities: List<SpeciesEntity>)

}