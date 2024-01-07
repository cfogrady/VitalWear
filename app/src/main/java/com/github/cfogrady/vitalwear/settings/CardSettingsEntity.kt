package com.github.cfogrady.vitalwear.settings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.cfogrady.vitalwear.settings.CardSettingsEntity.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class CardSettingsEntity (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "card_name")    var cardName: String,
    @ColumnInfo(name = "appear_in_global_battles") var appearInGlobalBattles: Boolean,
) {
    companion object {
        const val TABLE_NAME = "card_settings"

        fun default(cardName: String): CardSettingsEntity {
            return CardSettingsEntity(cardName, true)
        }
    }
}