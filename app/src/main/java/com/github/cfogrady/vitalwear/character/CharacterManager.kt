package com.github.cfogrady.vitalwear.character

import android.content.Context
import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.card.CardMeta
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.CharacterPreview
import com.github.cfogrady.vitalwear.character.data.SupportCharacter
import com.github.cfogrady.vitalwear.character.transformation.ExpectedTransformation
import com.github.cfogrady.vitalwear.common.card.CharacterSpritesIO
import com.github.cfogrady.vitalwear.settings.CharacterSettings
import kotlinx.coroutines.flow.StateFlow

interface CharacterManager {
    val initialized: StateFlow<Boolean>
    fun getCurrentCharacter(): VBCharacter?

    fun getCharacterFlow() : StateFlow<VBCharacter?>

    suspend fun fetchSupportCharacter(context: Context): SupportCharacter?
    suspend fun doActiveCharacterTransformation(applicationContext: Context, transformationOption: ExpectedTransformation) : VBCharacter

    suspend fun createNewCharacter(applicationContext: Context, cardMeta: CardMeta, slotId: Int, characterSettings: CharacterSettings)

    suspend fun swapToCharacter(applicationContext: Context, selectedCharacterPreview : CharacterPreview)

    suspend fun setToSupport(characterPreview: CharacterPreview)


    fun deleteCharacter(characterPreview: CharacterPreview)

    fun deleteCurrentCharacter()

    /**
     * Checks to see if the currentCharacter is from the same card. If not do nothing. If so,
     * set the currentCharacter's cardMeta to the new cardMeta
     */
    fun maybeUpdateCardMeta(cardMeta: CardMeta)

    suspend fun getCharacterBitmap(context: Context, cardName: String, slotId: Int, sprite: String, backupSprite: String = CharacterSpritesIO.IDLE1): Bitmap
}