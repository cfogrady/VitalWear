package com.github.cfogrady.vitalwear.character

import androidx.lifecycle.LiveData
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.CharacterPreview
import java.io.File

interface CharacterManager {
    val initialized: LiveData<Boolean>
    fun getCurrentCharacter(): BEMCharacter
    fun getLiveCharacter() : LiveData<BEMCharacter>

    fun activeCharacterIsPresent() : Boolean

    fun doActiveCharacterTransformation()

    fun createNewCharacter(file: File)

    suspend fun swapToCharacter(selectedCharacterPreview : CharacterPreview)

    fun deleteCharacter(characterPreview: CharacterPreview)
}