package com.github.cfogrady.vitalwear.character.transformation.history

import androidx.room.Entity

@Entity(tableName = TransformationHistoryEntity.TABLE, primaryKeys = ["characterId", "phase"])
data class TransformationHistoryEntity(
    val characterId: Int,
    val phase: Int,
    val cardName: String,
    val speciesId: Int,
) {
    companion object {
        const val TABLE = "transformation_history"
    }
}
