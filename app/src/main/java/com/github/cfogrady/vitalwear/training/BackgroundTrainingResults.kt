package com.github.cfogrady.vitalwear.training

data class BackgroundTrainingResults(val great: Int, val good: Int, val failure: Int, val trainingType: TrainingType) {
    fun resultType(): TrainingResult {
        return if(great > 0) {
            TrainingResult.GREAT
        } else if(good > 0) {
            TrainingResult.GOOD
        } else {
            TrainingResult.FAIL
        }
    }
}