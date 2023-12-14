package com.github.cfogrady.vitalwear.card.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.cfogrady.vitalwear.card.CardType
import com.github.cfogrady.vitalwear.card.db.CardMetaEntity.Companion.TABLE

@Entity(tableName = TABLE)
data class CardMetaEntity(
    @PrimaryKey
    val cardName: String,
    val cardId: Int,
    val cardChecksum: Int,
    val cardType: CardType,
    val franchise: Int,
) {
    companion object {
        const val TAG = "CardMetaEntity"
        const val TABLE = "card_meta"
        val EMPTY_CARD_META = CardMetaEntity("NONE", 0, 0, CardType.BEM, 0)
    }
}