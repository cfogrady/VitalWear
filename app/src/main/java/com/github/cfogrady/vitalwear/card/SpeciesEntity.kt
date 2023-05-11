package com.github.cfogrady.vitalwear.card

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.cfogrady.vitalwear.card.SpeciesEntity.Companion.TABLE

@Entity(tableName = TABLE)
data class SpeciesEntity (
    @PrimaryKey
    val cardName: String,
    @PrimaryKey
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
    }
}