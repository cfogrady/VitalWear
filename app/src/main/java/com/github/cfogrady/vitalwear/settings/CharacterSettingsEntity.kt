package com.github.cfogrady.vitalwear.settings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.cfogrady.vitalwear.settings.CharacterSettingsEntity.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class CharacterSettingsEntity (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "character_id")    val characterId: Int,
    @ColumnInfo(name = "train_in_background") val trainInBackground: Boolean,
    @ColumnInfo(name = "allowed_battles") val allowedBattles: CharacterSettings.AllowedBattles,
    @ColumnInfo(name = "assumedFranchise") val assumedFranchise: Int?,
) {
    companion object {
        const val TABLE_NAME = "character_settings"
    }
}