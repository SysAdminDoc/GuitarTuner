package com.sysadmindoc.guitartuner.tuning

import com.sysadmindoc.guitartuner.pitch.PitchEstimate
import com.sysadmindoc.guitartuner.pitch.SignalStatus

data class PitchResult(
    val frequencyHz: Double?,
    val nearestNote: String?,
    val centsOffset: Double?,
    val confidence: Double,
    val signalState: PitchSignalState,
) {
    companion object {
        fun waiting(): PitchResult = PitchResult(
            frequencyHz = null,
            nearestNote = null,
            centsOffset = null,
            confidence = 0.0,
            signalState = PitchSignalState.WaitingForSignal,
        )

        fun from(
            estimate: PitchEstimate,
            measurement: TuningMeasurement,
        ): PitchResult = PitchResult(
            frequencyHz = measurement.frequencyHz ?: estimate.frequencyHz,
            nearestNote = measurement.target?.scientificPitch,
            centsOffset = measurement.cents,
            confidence = measurement.confidence,
            signalState = signalStateFor(estimate, measurement),
        )

        private fun signalStateFor(
            estimate: PitchEstimate,
            measurement: TuningMeasurement,
        ): PitchSignalState = when {
            estimate.status == SignalStatus.Clipping ||
                measurement.status == TuningStatus.SignalClipping -> PitchSignalState.Clipping

            estimate.status == SignalStatus.HighNoise ||
                measurement.status == TuningStatus.HighNoise -> PitchSignalState.HighNoise

            estimate.status == SignalStatus.Unstable -> PitchSignalState.Unstable
            measurement.status == TuningStatus.NoStringDetected -> PitchSignalState.NoStringDetected
            measurement.status == TuningStatus.Overshoot -> PitchSignalState.Detected
            estimate.status == SignalStatus.Silence ||
                measurement.status == TuningStatus.WaitingForSignal -> PitchSignalState.WaitingForSignal

            else -> PitchSignalState.Detected
        }
    }
}

enum class PitchSignalState {
    WaitingForSignal,
    Clipping,
    HighNoise,
    Unstable,
    NoStringDetected,
    Detected,
}
