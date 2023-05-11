package com.github.cfogrady.vitalwear.card

import androidx.room.*

@Dao
interface CardMetaEntityDao {
    @Query("select * from ${CardMetaEntity.TABLE} order by cardName")
    fun getAll(franchise: Int): List<CardMetaEntity>

    @Query("select * from ${CardMetaEntity.TABLE} where franchise = :franchise")
    fun getByFranchise(franchise: Int): List<CardMetaEntity>

    @Query("select * from ${CardMetaEntity.TABLE} where cardName = :cardName limit 1")
    fun getByName(cardName: String): CardMetaEntity

    @Insert
    fun insert(cardMetaEntity: CardMetaEntity)
}