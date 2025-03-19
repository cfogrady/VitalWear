package com.github.cfogrady.vitalwear.character.data

import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.character.CharacterManager

class CharacterPreview(override val cardName: String, override val slotId: Int, override val characterId: Int, override val state: CharacterState, val idle : Bitmap):
    CharacterManager.SwapCharacterIdentifier {
    fun isActive(): Boolean {
        return state == CharacterState.ACTIVE
    }
}