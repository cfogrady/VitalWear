package com.github.cfogrady.vitalwear.card.db

data class TransformationEntityWithToCharDir(
    val cardName: String,
    val fromCharacterId: Int,
    val toCharacterId: Int,
    val timeToTransformationMinutes: Int,
    val requiredVitals: Int,
    val requiredPp: Int,
    val requiredBattles: Int,
    val requiredWinRatio: Int,
    val minAdventureCompletionRequired: Int?,
    val isSecret: Boolean,
    val sortOrder: Int,
    val toCharDir: String,
) {
}