package com.github.cfogrady.vitalwear.character

import android.content.Context
import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.card.CardMeta
import com.github.cfogrady.vitalwear.character.data.CharacterEntity
import com.github.cfogrady.vitalwear.character.data.CharacterPreview
import com.github.cfogrady.vitalwear.character.data.CharacterState
import com.github.cfogrady.vitalwear.character.data.SupportCharacter
import com.github.cfogrady.vitalwear.character.transformation.ExpectedTransformation
import com.github.cfogrady.vitalwear.character.transformation.history.TransformationHistoryEntity
import com.github.cfogrady.vitalwear.common.card.CharacterSpritesIO
import com.github.cfogrady.vitalwear.settings.CharacterSettings
import kotlinx.coroutines.flow.StateFlow

interface CharacterManager {

    interface SwapCharacterIdentifier {
        val cardName: String
        val characterId: Int
        val slotId: Int
        val state: CharacterState

        companion object {
            fun buildAnonymous(cardName: String, characterId: Int, slotId: Int, state: CharacterState): SwapCharacterIdentifier {
                return object: SwapCharacterIdentifier {
                    override val cardName: String = cardName
                    override val characterId: Int = characterId
                    override val slotId: Int = slotId
                    override val state: CharacterState = state

                }
            }
        }
    }

    val initialized: StateFlow<Boolean>
    fun getCurrentCharacter(): VBCharacter?

    fun getCharacterFlow() : StateFlow<VBCharacter?>

    suspend fun fetchSupportCharacter(context: Context): SupportCharacter?
    suspend fun doActiveCharacterTransformation(applicationContext: Context, transformationOption: ExpectedTransformation) : VBCharacter

    suspend fun createNewCharacter(applicationContext: Context, cardMeta: CardMeta, slotId: Int, characterSettings: CharacterSettings)

    suspend fun swapToCharacter(applicationContext: Context, swapCharacterIdentifier : SwapCharacterIdentifier)

    suspend fun setToSupport(characterPreview: CharacterPreview)


    fun deleteCharacter(characterPreview: CharacterPreview)

    fun deleteCurrentCharacter()

    suspend fun largestTransformationTimeSeconds(cardName: String, slotId: Int) : Long

    suspend fun getTransformationHistory(characterId: Int): List<TransformationHistoryEntity>

    // Adds a pre-created (transferred) character to storage and returns the characterId
    suspend fun addCharacter(cardName: String, characterEntity: CharacterEntity, characterSettings: CharacterSettings, transformationHistory: List<TransformationHistoryEntity>): Int

    /**
     * Checks to see if the currentCharacter is from the same card. If not do nothing. If so,
     * set the currentCharacter's cardMeta to the new cardMeta
     */
    fun maybeUpdateCardMeta(cardMeta: CardMeta)

    suspend fun getCharacterBitmap(context: Context, cardName: String, slotId: Int, sprite: String, backupSprite: String = CharacterSpritesIO.IDLE1): Bitmap
}