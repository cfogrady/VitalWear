package com.github.cfogrady.vitalwear.training

import com.github.cfogrady.vitalwear.character.StatType

enum class TrainingType(val durationSeconds: Int, val standardTrainingIncrease: Int, val affectedStat: StatType) {
    SQUAT(20, 1, StatType.PP),
    CRUNCH(30, 5, StatType.HP),
    PUNCH(20, 5, StatType.AP),
    DASH(20, 5, StatType.BP);
}