package com.github.cfogrady.vitalwear.card

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.cfogrady.vitalwear.card.TransformationEntity.Companion.TABLE

@Entity(tableName = TABLE)
data class TransformationEntity(
    @PrimaryKey
    val cardName: String,
    @PrimaryKey
    val fromCharacterId: Int,
    @PrimaryKey
    val toCharacterId: Int?,
    val timeToTransformationMinutes: Int,
    val requiredVitals: Int,
    val requiredPp: Int,
    val requiredBattles: Int,
    val requiredWinRatio: Int,
    val minAdventureCompletionRequired: Int?,
    val isSecret: Boolean?,
    val order: Int,
) {
    companion object {
        const val TABLE = "transformation"
    }
}