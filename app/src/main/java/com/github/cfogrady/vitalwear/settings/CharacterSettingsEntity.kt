package com.github.cfogrady.vitalwear.settings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.cfogrady.vitalwear.settings.CharacterSettingsEntity.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class CharacterSettingsEntity (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "character_id")    var characterId: Int,
    @ColumnInfo(name = "train_in_background") var trainInBackground: Boolean,
    @ColumnInfo(name = "allowed_battles") var allowedBattles: AllowedBattles,
) {
    companion object {
        const val TABLE_NAME = "character_settings"
        val DEFAULT_SETTINGS = CharacterSettingsEntity(0, true, AllowedBattles.CARD_ONLY)
    }

    enum class AllowedBattles(val descr: String) {
        CARD_ONLY("Random Battles Against Card Only"),
        ALL_FRANCHISE("Random Battles Against Any Card In Franchise"),
        // ALL_FRANCHISE_AND_DIM("Random Battles Against Any Card In Franchise And DIMs"),
        ALL("Random Battles Against Any Card");
    }
}