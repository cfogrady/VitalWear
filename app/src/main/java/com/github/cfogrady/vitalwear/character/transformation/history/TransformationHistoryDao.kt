package com.github.cfogrady.vitalwear.character.transformation.history

import androidx.room.*

@Dao
interface TransformationHistoryDao {
    @Query("select * from ${TransformationHistoryEntity.TABLE} where characterId = :id")
    fun getByCharacterId(id: Int): List<TransformationHistoryEntity>

    @Upsert
    fun upsert(transformationHistoryDao: TransformationHistoryEntity)

    @Query("delete from ${TransformationHistoryEntity.TABLE} where characterId = :id")
    fun deleteByCharacterId(id: Int)
}