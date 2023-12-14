package com.github.cfogrady.vitalwear.card.db

import androidx.room.Entity
import com.github.cfogrady.vitalwear.card.db.SpeciesEntity.Companion.TABLE

@Entity(tableName = TABLE, primaryKeys = ["cardName", "characterId"])
data class SpeciesEntity (
    val cardName: String,
    val characterId: Int,
    val phase: Int,
    val attribute: Int,
    val type: Int,
    val attackId: Int,
    val criticalAttackId: Int,
    val dpStars: Int, //just in case we support it someday
    val bp: Int,
    val hp: Int,
    val ap: Int,
    val battlePool1: Int,
    val battlePool2: Int,
    val battlePool3: Int,
    val spriteDirName: String, //path to sprites
) {
    companion object {
        const val TAG = "SpeciesEntity"
        const val TABLE = "species"
        val EMPTY_SPECIES_ENTITY = SpeciesEntity("NONE", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "")
    }
}