package com.github.cfogrady.vitalwear.card

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AdventureEntityDao {
    @Query("select * from ${AdventureEntity.TABLE} where cardName = :cardName order by adventureId")
    fun getByCard(cardName: String): List<CardMetaEntity>

    @Insert
    fun insert(adventureEntity: AdventureEntity)
}