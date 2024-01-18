package com.github.cfogrady.vitalwear.character.data

import androidx.room.*

@Dao
interface CharacterDao {
    @Query("select * from character where id = :id limit 1")
    fun getCharacterById(id: Int): CharacterEntity

    @Query("select * from character order by last_update desc")
    fun getCharactersOrderByRecent(): List<CharacterEntity>

    @Query("select * from character where state = :state")
    fun getCharactersByState(state: CharacterState) : List<CharacterEntity>

    @Update
    fun update(characterEntity: CharacterEntity)

    @Update
    fun updateMany(characterEntities: Collection<CharacterEntity>)

    @Insert
    fun insert(characterEntity: CharacterEntity) : Long

    @Query("delete from character where id = :id")
    fun deleteById(id: Int)
}