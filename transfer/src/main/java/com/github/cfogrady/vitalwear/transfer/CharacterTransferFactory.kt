package com.github.cfogrady.vitalwear.transfer

import android.content.Context

class CharacterTransferFactory(private val context: Context) {
    fun createCharacterTransfer(): CharacterTransfer {
        return CharacterTransferImpl(context)
    }
}