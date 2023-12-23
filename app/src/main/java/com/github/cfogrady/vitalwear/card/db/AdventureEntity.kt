package com.github.cfogrady.vitalwear.card.db

import androidx.room.Entity
import com.github.cfogrady.vitalwear.card.db.AdventureEntity.Companion.TABLE

@Entity(tableName = TABLE, primaryKeys = ["cardName", "adventureId"])
data class AdventureEntity(
    val cardName: String,
    val adventureId: Int,
    val steps: Int,
    val characterId: Int,
    val bp: Int?, //use character default when null
    val hp: Int?, //use character default when null
    val ap: Int?,  //use character default when null
    val attackId: Int?, //use character default when null
    val criticalAttackId: Int?, //use character default when null
    val walkingBackgroundId: Int,
    val bossBackgroundId: Int,
    val hiddenBoss: Boolean,
    val characterIdJoined: Int?,
    val cleared: Boolean,
) {
    companion object {
        const val TABLE = "adventure"
    }
}