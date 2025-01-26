package com.github.cfogrady.vitalwear.character.transformation.history

import androidx.room.*

@Dao
interface TransformationHistoryDao {
    @Query("select * from ${TransformationHistoryEntity.TABLE} where characterId = :id")
    fun getByCharacterId(id: Int): List<TransformationHistoryEntity>

    @Insert
    fun insert(transformationHistoryEntities: Collection<TransformationHistoryEntity>)

    @Upsert
    fun upsert(transformationHistoryEntity: TransformationHistoryEntity)

    @Query("delete from ${TransformationHistoryEntity.TABLE} where characterId = :id")
    fun deleteByCharacterId(id: Int)
}