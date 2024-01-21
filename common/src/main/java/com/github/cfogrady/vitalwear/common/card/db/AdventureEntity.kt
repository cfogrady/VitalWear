package com.github.cfogrady.vitalwear.common.card.db

import androidx.room.Entity
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity.Companion.TABLE
import kotlin.math.max

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

        fun highestAdventureCompleted(adventures: List<AdventureEntity>): Int? {
            var highestCompleted: Int? = null
            for(adventure in adventures) {
                if(adventure.cleared) {
                    highestCompleted = max(adventure.adventureId, highestCompleted ?: -1)
                }
            }
            return highestCompleted
        }
    }
}