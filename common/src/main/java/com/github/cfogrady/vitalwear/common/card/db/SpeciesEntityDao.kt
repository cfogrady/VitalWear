package com.github.cfogrady.vitalwear.common.card.db

import androidx.room.*

@Dao
interface SpeciesEntityDao {
    @Query("select * from ${SpeciesEntity.TABLE} where cardName = :cardName and characterId = :characterId limit 1")
    fun getCharacterByCardAndCharacterId(cardName: String, characterId: Int): SpeciesEntity

    @Query("select * from ${SpeciesEntity.TABLE} where cardName = :cardName")
    fun getCharacterByCard(cardName: String): List<SpeciesEntity>

    @Query("select * from ${SpeciesEntity.TABLE} where cardName = :cardName and characterId in (:characterIds)")
    fun getCharacterByCardAndInCharacterIds(cardName: String, characterIds: Collection<Int>): List<SpeciesEntity>

    @Insert
    fun insertAll(speciesEntities: Collection<SpeciesEntity>)

}