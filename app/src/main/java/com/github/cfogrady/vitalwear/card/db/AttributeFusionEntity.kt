package com.github.cfogrady.vitalwear.card.db

import androidx.room.Entity
import com.github.cfogrady.vitalwear.card.db.AttributeFusionEntity.Companion.TABLE


@Entity(tableName = TABLE, primaryKeys = ["cardName", "fromCharacterId"])
data class AttributeFusionEntity(
    val cardName: String,
    val fromCharacterId: Int,
    val attribute1Result: Int?,
    val attribute2Result: Int?,
    val attribute3Result: Int?,
    val attribute4Result: Int?,
) {
    companion object {
        const val TABLE = "attribute_fusion"
    }
}