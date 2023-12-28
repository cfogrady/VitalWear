package com.github.cfogrady.vitalwear.common.card.db

import androidx.room.Entity
import com.github.cfogrady.vitalwear.common.card.db.TransformationEntity.Companion.TABLE

@Entity(tableName = TABLE, primaryKeys = ["cardName", "fromCharacterId", "toCharacterId"])
data class TransformationEntity(
    val cardName: String,
    val fromCharacterId: Int,
    val toCharacterId: Int,
    val timeToTransformationMinutes: Int,
    val requiredVitals: Int,
    val requiredPp: Int,
    val requiredBattles: Int,
    val requiredWinRatio: Int,
    val minAdventureCompletionRequired: Int?,
    val isSecret: Boolean,
    val sortOrder: Int,
) {
    companion object {
        const val TABLE = "transformation"
    }
}