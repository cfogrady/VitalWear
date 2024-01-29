package com.github.cfogrady.vitalwear.settings

import androidx.room.*
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity

@Dao
interface CardSettingsDao {
    @Query("select * from ${CardSettingsEntity.TABLE_NAME} where card_name = :cardName limit 1")
    fun getByCharacterId(cardName: String): CardSettingsEntity

    @Query("select cme.* from ${CardMetaEntity.TABLE} cme left join ${CardSettingsEntity.TABLE_NAME} cse on cme.cardName = cse.card_name and cse.appear_in_global_battles = 1")
    fun getAllBattleCards(): List<CardMetaEntity>

    @Query("select cme.* from ${CardMetaEntity.TABLE} cme left join ${CardSettingsEntity.TABLE_NAME} cse on cme.cardName = cse.card_name and cse.appear_in_global_battles = 1 where cme.franchise = :franchiseId")
    fun getAllFranchiseBattleCards(franchiseId: Int): List<CardMetaEntity>

    @Upsert
    fun upsert(cardSettingsEntity: CardSettingsEntity)

    @Query("delete from ${CardSettingsEntity.TABLE_NAME} where card_name = :cardName")
    fun deleteByCardName(cardName: String)
}