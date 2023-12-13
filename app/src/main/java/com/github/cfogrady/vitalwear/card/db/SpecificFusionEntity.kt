package com.github.cfogrady.vitalwear.card.db

import androidx.room.Entity
import com.github.cfogrady.vitalwear.card.db.SpecificFusionEntity.Companion.TABLE

@Entity(tableName = TABLE, primaryKeys = ["cardName", "fromCharacterId", "toCharacterId", "supportCardId", "supportCharacterId"])
data class SpecificFusionEntity(
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