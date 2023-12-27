package com.github.cfogrady.vitalwear.common.card.db

import androidx.room.Entity
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntity.Companion.TABLE
import com.github.cfogrady.vitalwear.composable.util.formatNumber

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
    val raised: Boolean,
) {
    companion object {
        const val TAG = "SpeciesEntity"
        const val TABLE = "species"
        val EMPTY_SPECIES_ENTITY = SpeciesEntity("NONE", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "", false)
    }

    fun displayBp(): String {
        if (bp == DimReader.NONE_VALUE) {
            return formatNumber(0, 4)
        }
        return formatNumber(bp, 4)
    }

    fun displayHp(): String {
        if (hp == DimReader.NONE_VALUE) {
            return formatNumber(0, 4)
        }
        return formatNumber(hp, 4)
    }

    fun displayAp(): String {
        if (ap == DimReader.NONE_VALUE) {
            return formatNumber(0, 4)
        }
        return formatNumber(ap, 4)
    }
}