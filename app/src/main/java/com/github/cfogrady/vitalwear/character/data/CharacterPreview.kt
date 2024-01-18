package com.github.cfogrady.vitalwear.character.data

import android.graphics.Bitmap

class CharacterPreview(val cardName: String, val slotId: Int, val characterId: Int, val state: CharacterState, val idle : Bitmap) {
    fun isActive(): Boolean {
        return state == CharacterState.SYNCED
    }
}