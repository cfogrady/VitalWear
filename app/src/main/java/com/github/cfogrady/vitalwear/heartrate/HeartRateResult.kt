package com.github.cfogrady.vitalwear.heartrate

class HeartRateResult(val heartRate: Int, val heartRateError: HeartRateError) {
    companion object {
        enum class HeartRateError {
            NONE,
            UNRELIABLE,
            UNAVAILABLE,
            NO_CONTACT
        }
    }
}