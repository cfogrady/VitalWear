package com.github.cfogrady.vitalwear.common.card.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransformationEntityDao {

    @Query("select * from ${TransformationEntity.TABLE} where cardName = :cardName and fromCharacterId = :fromCharacterId order by sortOrder")
    fun getByCardAndFromCharacterId(cardName: String, fromCharacterId: Int): List<TransformationEntity>

    @Query(
        "SELECT ${TransformationEntity.TABLE}.*, ${SpeciesEntity.TABLE}.spriteDirName AS toCharDir FROM ${TransformationEntity.TABLE} " +
                "INNER JOIN ${SpeciesEntity.TABLE} ON ${SpeciesEntity.TABLE}.characterId = ${TransformationEntity.TABLE}.toCharacterId AND ${SpeciesEntity.TABLE}.cardName = ${TransformationEntity.TABLE}.cardName " +
                "where ${TransformationEntity.TABLE}.cardName = :cardName and ${TransformationEntity.TABLE}.fromCharacterId = :fromCharacterId order by sortOrder"
    )
    fun getByCardAndFromCharacterIdWithToCharDir(cardName: String, fromCharacterId: Int): List<TransformationEntityWithToCharDir>

    @Insert
    fun insertAll(transformationEntities: Collection<TransformationEntity>)
}