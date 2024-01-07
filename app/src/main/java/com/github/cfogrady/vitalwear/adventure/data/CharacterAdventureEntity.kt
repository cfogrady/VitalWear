package com.github.cfogrady.vitalwear.adventure.data

import androidx.room.Entity
import com.github.cfogrady.vitalwear.adventure.data.CharacterAdventureEntity.Companion.TABLE

@Entity(tableName = TABLE, primaryKeys = ["cardName", "characterId"])
data class CharacterAdventureEntity(
    val cardName: String,
    val characterId: Int,
    val adventureId: Int,
) {
    companion object {
        const val TABLE = "character_adventures"
    }
}