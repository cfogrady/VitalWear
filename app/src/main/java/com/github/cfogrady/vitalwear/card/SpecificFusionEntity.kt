package com.github.cfogrady.vitalwear.card

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.cfogrady.vitalwear.card.SpecificFusionEntity.Companion.TABLE

@Entity(tableName = TABLE)
data class SpecificFusionEntity(
    @PrimaryKey
    val cardName: String,
    val fromCharacterId: Int,
    val toCharacterId: Int,
    val supportCardId: Int,
    val supportCharacterId: Int,
) {
    companion object {
        const val TABLE = "specific_fusion"
    }
}