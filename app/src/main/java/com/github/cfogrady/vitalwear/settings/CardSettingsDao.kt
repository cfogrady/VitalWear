package com.github.cfogrady.vitalwear.settings

import androidx.room.*

@Dao
interface CardSettingsDao {
    @Query("select * from ${CardSettingsEntity.TABLE_NAME} where card_name = :cardName limit 1")
    fun getByCharacterId(cardName: String): CardSettingsEntity

    @Upsert
    fun upsert(cardSettingsEntity: CardSettingsEntity)

    @Query("delete from ${CardSettingsEntity.TABLE_NAME} where card_name = :cardName")
    fun deleteByCardName(cardName: String)
}