package com.github.cfogrady.vitalwear.adventure.data

import androidx.room.*

@Dao
interface CharacterAdventureDao {
    @Query("select * from ${CharacterAdventureEntity.TABLE} where characterId = :id and cardName = :cardName limit 1")
    fun getByCharacterIdAndCardName(id: Int, cardName: String): CharacterAdventureEntity?

    @Query("select * from ${CharacterAdventureEntity.TABLE} where characterId = :id")
    fun getByCharacterId(id: Int): List<CharacterAdventureEntity>

    @Upsert
    fun upsert(characterAdventureEntity: CharacterAdventureEntity)

    @Insert
    fun insert(characterAdventureEntities: Collection<CharacterAdventureEntity>)

    @Query("delete from ${CharacterAdventureEntity.TABLE} where characterId = :id")
    fun deleteByCharacterId(id: Int)
}