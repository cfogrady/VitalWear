package com.github.cfogrady.vitalwear.card

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ValidatedCardEntityDao {
    @Query("select cardId from ${ValidatedCardEntity.TABLE}")
    fun getIds(): List<Int>

    @Insert
    fun insert(validatedCardEntity: ValidatedCardEntity)
}