package com.github.cfogrady.vitalwear.common.card.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.cfogrady.vitalwear.common.card.CardType
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity.Companion.TABLE

@Entity(tableName = TABLE)
data class CardMetaEntity(
    @PrimaryKey
    val cardName: String,
    val cardId: Int,
    val cardChecksum: Int,
    val cardType: CardType,
    val franchise: Int,
    val maxAdventureCompletion: Int?,
) {
    companion object {
        const val TAG = "CardMetaEntity"
        const val TABLE = "card_meta"
    }
}