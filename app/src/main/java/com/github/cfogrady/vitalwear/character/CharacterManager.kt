package com.github.cfogrady.vitalwear.character

import android.content.Context
import androidx.lifecycle.LiveData
import com.github.cfogrady.vitalwear.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.CharacterPreview
import com.github.cfogrady.vitalwear.character.data.TransformationOption

interface CharacterManager {
    val initialized: LiveData<Boolean>
    fun getCurrentCharacter(): BEMCharacter

    fun getLiveCharacter() : LiveData<BEMCharacter>

    fun activeCharacterIsPresent() : Boolean

    fun doActiveCharacterTransformation(applicationContext: Context, transformationOption: TransformationOption) : BEMCharacter

    fun createNewCharacter(applicationContext: Context, cardMetaEntity: CardMetaEntity)

    suspend fun swapToCharacter(applicationContext: Context, selectedCharacterPreview : CharacterPreview)

    fun deleteCharacter(characterPreview: CharacterPreview)
}