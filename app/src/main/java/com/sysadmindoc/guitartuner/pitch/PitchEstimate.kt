package com.sysadmindoc.guitartuner.pitch

data class PitchEstimate(
    val frequencyHz: Double?,
    val confidence: Double,
    val rms: Double,
    val clipping: Boolean,
    val status: SignalStatus,
) {
    companion object {
        fun silence(rms: Double = 0.0): PitchEstimate = PitchEstimate(
            frequencyHz = null,
            confidence = 0.0,
            rms = rms,
            clipping = false,
            status = SignalStatus.Silence,
        )
    }
}

enum class SignalStatus {
    Silence,
    Clipping,
    Unstable,
    Detected,
}
