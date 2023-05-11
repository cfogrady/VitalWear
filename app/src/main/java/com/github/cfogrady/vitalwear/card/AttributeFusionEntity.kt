package com.github.cfogrady.vitalwear.card

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.cfogrady.vitalwear.card.AttributeFusionEntity.Companion.TABLE


@Entity(tableName = TABLE)
data class AttributeFusionEntity(
    @PrimaryKey
    val cardName: String,
    @PrimaryKey
    val fromCharacterId: Int,
    val attribute1Result: Int,
    val attribute2Result: Int,
    val attribute3Result: Int,
    val attribute4Result: Int,
) {
    companion object {
        const val TABLE = "attribute_fusion"
    }
}