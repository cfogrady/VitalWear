package com.github.cfogrady.vitalwear.firmware.components

data class EmoteSpriteIndexes(
    val happyEmoteStartIdx: Int,
    val happyEmoteEndIdx: Int,
    val loseEmoteStartIdx: Int,
    val loseEmoteEndIdx: Int,
    val sweatEmoteIdx: Int,
    val injuredEmoteStartIdx: Int,
    val injuredEmoteEndIdx: Int,
    val sleepEmoteStartIdx: Int,
    val sleepEmoteEndIdx: Int,
) {}