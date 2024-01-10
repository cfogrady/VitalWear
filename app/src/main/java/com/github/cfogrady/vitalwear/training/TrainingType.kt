package com.github.cfogrady.vitalwear.training

enum class TrainingType(val durationSeconds: Int, val standardTrainingIncrease: Int) {
    SQUAT(20, 1),
    CRUNCH(30, 5),
    PUNCH(20, 5),
    DASH(20, 5);
}