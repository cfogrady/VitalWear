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
) {

    companion object {
        const val TABLE_NAME = "character_settings"
        val DEFAULT_SETTINGS = CharacterSettingsEntity(0, true)
    }

}