package com.github.cfogrady.vitalwear.training

data class TrainingDataRow(
    val millis: Long,
    val x: Float,
    val y: Float,
    val z: Float,
    val count: Int,
) {
}