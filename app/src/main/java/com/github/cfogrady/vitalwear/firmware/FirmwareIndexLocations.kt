package com.github.cfogrady.vitalwear.firmware

interface FirmwareIndexLocations {

    data class MenuIndexLocations(
        val characterSelectorIcon: Int = 267,
        val trainingMenuIcon: Int = 265,
        val adventureMenuIcon: Int = 266,
        val statsIconIdx: Int = 264,
        val connectIcon: Int = 268,
        val stopIcon: Int = 44,
        val stopText: Int = 169,
        val settingsMenuIcon: Int = 269,
        val sleep: Int = 76,
        val wakeup: Int = 75,
    )

    val spritePackageLocation: Int
    val spriteDimensionsLocation: Int

    val menuIndexLocations: MenuIndexLocations

    val timerIcon: Int
    val insertCardIcon: Int
    val greenBackground: Int
    val blackBackground: Int
    val newBackgroundStartIdx: Int
    val newBackgroundEndIdx: Int
    val rayOfLightBackground: Int
    val orangeBackground: Int
    val blueBackground: Int
    val stepsIcon: Int
    val vitalsIcon: Int
    val battleBackground: Int
    val smallAttackStartIdx: Int
    val smallAttackEndIdx: Int
    val bigAttackStartIdx: Int
    val bigAttackEndIdx: Int
    val readyIdx: Int
    val goIdx: Int
    val emptyHpIdx: Int
    val partnerHpStartIdx: Int
    val partnerHpEndIdx: Int
    val opponentHpStartIdx: Int
    val opponentHpEndIdx: Int
    val hitStartIdx: Int
    val hitEndIdx: Int
    val happyEmoteStartIdx: Int
    val happyEmoteEndIdx: Int // AlsoWin
    val loseEmoteStartIdx: Int
    val loseEmoteEndIdx: Int
    val sleepEmoteStartIdx: Int
    val sleepEmoteEndIdx: Int
    val sweatEmoteIdx: Int
    val injuredEmoteStartIdx: Int
    val injuredEmoteEndIdx: Int
    val squatTextIdx: Int
    val squatIconIdx: Int
    val crunchTextIdx: Int
    val crunchIconIdx: Int
    val punchTextIdx: Int
    val punchIconIdx: Int
    val dashTextIdx: Int
    val dashIconIdx: Int
    val trainingStateStartIdx: Int
    val trainingStateEndIdx: Int
    val clearIdx: Int
    val missionIdx: Int
    val failedIdx: Int
    val goodIdx: Int
    val greatIdx: Int
    val supportIdx: Int
    val bpIdx: Int
    val hpIdx: Int
    val apIdx: Int
    val ppIdx: Int
    val vitalsRangeStartIdx: Int
    val vitalsRangeEndIdx: Int
    val weakPulse: Int
    val strongPulse: Int
    val star: Int
    val transformationHourglass: Int
    val transformationVitalsIcon: Int
    val transformationBattlesIcon: Int
    val transformationWinRatioIcon: Int
    val transformationPpIcon: Int
    val transformationSquatIcon: Int
    val transformationLocked: Int
}