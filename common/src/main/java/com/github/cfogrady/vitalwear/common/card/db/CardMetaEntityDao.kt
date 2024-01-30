package com.github.cfogrady.vitalwear.common.card.db

import androidx.room.*

@Dao
interface CardMetaEntityDao {
    @Query("select * from ${CardMetaEntity.TABLE} order by cardName")
    fun getAll(): List<CardMetaEntity>

    @Query("select * from ${CardMetaEntity.TABLE} where franchise = :franchise")
    fun getByFranchise(franchise: Int): List<CardMetaEntity>

    @Query("select * from ${CardMetaEntity.TABLE} where cardName = :cardName limit 1")
    fun getByName(cardName: String): CardMetaEntity

    @Query("select distinct franchise from ${CardMetaEntity.TABLE} where franchise != 0")
    fun getNonDIMFranchises(): List<Int>

    @Update
    fun update(cardMetaEntity: CardMetaEntity)

    @Insert
    fun insert(cardMetaEntity: CardMetaEntity)
}