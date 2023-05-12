package com.github.cfogrady.vitalwear.card

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransformationEntityDao {

    @Query("select * from ${TransformationEntity.TABLE} where cardName = :cardName and fromCharacterId = :fromCharacterId order by sortOrder")
    fun getByCardAndFromCharacterId(cardName: String, fromCharacterId: Int): List<TransformationEntity>

    @Insert
    fun insert(transformationEntity: TransformationEntity)
}