package com.github.cfogrady.vitalwear.common.card.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AdventureEntityDao {
    @Query("select * from ${AdventureEntity.TABLE} where cardName = :cardName order by adventureId")
    fun getByCard(cardName: String): List<AdventureEntity>

    @Update
    fun update(adventureEntity: AdventureEntity)

    @Insert
    fun insertAll(adventureEntities: Collection<AdventureEntity>)
}