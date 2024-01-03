package com.github.cfogrady.vitalwear.settings

import androidx.room.*
import com.github.cfogrady.vitalwear.settings.CharacterSettingsEntity.Companion.TABLE_NAME

@Dao
interface CharacterSettingsDao {
    @Query("select * from $TABLE_NAME where character_id = :id limit 1")
    fun getByCharacterId(id: Int): CharacterSettingsEntity

    @Update
    fun update(characterSettingsEntity: CharacterSettingsEntity)

    @Insert
    fun insert(characterSettingsEntity: CharacterSettingsEntity)

    @Query("delete from $TABLE_NAME where character_id = :id")
    fun deleteById(id: Int)
}