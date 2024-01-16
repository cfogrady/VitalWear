package com.github.cfogrady.vitalwear.adventure.data

import androidx.room.*

@Dao
interface CharacterAdventureDao {
    @Query("select * from ${CharacterAdventureEntity.TABLE} where characterId = :id and cardName = :cardName limit 1")
    fun getByCharacterIdAndCardName(id: Int, cardName: String): CharacterAdventureEntity?

    @Upsert
    fun upsert(characterAdventureEntity: CharacterAdventureEntity)

    @Query("delete from ${CharacterAdventureEntity.TABLE} where characterId = :id")
    fun deleteByCharacterId(id: Int)
}