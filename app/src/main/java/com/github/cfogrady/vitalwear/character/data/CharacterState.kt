package com.github.cfogrady.vitalwear.character.data

enum class CharacterState {
    SYNCED,
    SUPPORT,
    @Deprecated("Replaced with STORED") BACKUP,
    STORED,
}