package com.github.cfogrady.vitalwear.card

import androidx.room.Dao
import androidx.room.Query
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntity
import com.github.cfogrady.vitalwear.common.card.db.SpecificFusionEntity
import com.github.cfogrady.vitalwear.common.card.db.TransformationEntity

@Dao
interface StatConversionDao {

    @Query("select max(s.phase) from ${TransformationEntity.TABLE} as t " +
            "inner join ${SpeciesEntity.TABLE} as s on s.cardName = t.cardName and s.characterId = t.fromCharacterId " +
            "where t.cardName = :cardName and t.toCharacterId = :toCharacterId")
    fun getPhaseTransformationsToCharacter(cardName: String, toCharacterId: Int): Int?

    @Query("select max(s.phase) from ${SpecificFusionEntity.TABLE} as f " +
            "inner join ${SpeciesEntity.TABLE} as s on s.cardName = f.cardName and s.characterId = f.fromCharacterId " +
            "where f.cardName = :cardName and f.toCharacterId = :speciesId")
    fun getFusionPhasesToCharacter(cardName: String, speciesId: Int): Int?
}