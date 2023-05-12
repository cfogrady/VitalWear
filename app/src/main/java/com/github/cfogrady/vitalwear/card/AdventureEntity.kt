package com.github.cfogrady.vitalwear.card

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.cfogrady.vitalwear.card.AdventureEntity.Companion.TABLE

@Entity(tableName = TABLE, primaryKeys = ["cardName", "adventureId"])
data class AdventureEntity(
    val cardName: String,
    val adventureId: Int,
    val characterId: Int,
    val bp: Int?,
    val hp: Int?,
    val ap: Int?,
    val attackId: Int?,
    val criticalAttackId: Int?,
    val walkingBackgroundId: Int,
    val bossBackgroundId: Int,
    val hiddenBoss: Boolean,
    val characterIdJoined: Int?,
) {
    companion object {
        const val TABLE = "adventure"
    }
}