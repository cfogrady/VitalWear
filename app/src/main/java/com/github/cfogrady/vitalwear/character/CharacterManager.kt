package com.github.cfogrady.vitalwear.character

import android.content.Context
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.CharacterPreview
import com.github.cfogrady.vitalwear.character.data.TransformationOption
import kotlinx.coroutines.flow.StateFlow

interface CharacterManager {
    val initialized: StateFlow<Boolean>
    fun getCurrentCharacter(): BEMCharacter?

    fun getCharacterFlow() : StateFlow<BEMCharacter?>

    fun doActiveCharacterTransformation(applicationContext: Context, transformationOption: TransformationOption) : BEMCharacter

    fun createNewCharacter(applicationContext: Context, cardMetaEntity: CardMetaEntity)

    suspend fun swapToCharacter(applicationContext: Context, selectedCharacterPreview : CharacterPreview)

    suspend fun updateSettings()

    fun deleteCharacter(characterPreview: CharacterPreview)
}