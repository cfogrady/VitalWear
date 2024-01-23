package com.github.cfogrady.vitalwear.character

import android.content.Context
import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.CharacterPreview
import com.github.cfogrady.vitalwear.character.data.SupportCharacter
import com.github.cfogrady.vitalwear.character.transformation.ExpectedTransformation
import com.github.cfogrady.vitalwear.common.card.CharacterSpritesIO
import kotlinx.coroutines.flow.StateFlow

interface CharacterManager {
    val initialized: StateFlow<Boolean>
    fun getCurrentCharacter(): VBCharacter?

    fun getCharacterFlow() : StateFlow<VBCharacter?>

    suspend fun fetchSupportCharacter(context: Context): SupportCharacter?
    fun doActiveCharacterTransformation(applicationContext: Context, transformationOption: ExpectedTransformation) : VBCharacter

    fun createNewCharacter(applicationContext: Context, cardMetaEntity: CardMetaEntity)

    suspend fun swapToCharacter(applicationContext: Context, selectedCharacterPreview : CharacterPreview)

    suspend fun setToSupport(characterPreview: CharacterPreview)

    suspend fun updateSettings()

    fun deleteCharacter(characterPreview: CharacterPreview)

    /**
     * Checks to see if the currentCharacter is from the same card. If not do nothing. If so,
     * set the currentCharacter's cardMeta to the new cardMeta
     */
    fun maybeUpdateCardMeta(cardMetaEntity: CardMetaEntity)

    suspend fun getCharacterBitmap(context: Context, cardName: String, slotId: Int, sprite: String, backupSprite: String = CharacterSpritesIO.IDLE1): Bitmap
}