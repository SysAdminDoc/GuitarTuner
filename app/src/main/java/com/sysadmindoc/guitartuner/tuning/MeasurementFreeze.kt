package com.sysadmindoc.guitartuner.tuning

import com.sysadmindoc.guitartuner.pitch.PitchEstimate

class MeasurementFreeze {
    private var lastStableFrame: FrozenMeasurementFrame? = null

    fun apply(
        estimate: PitchEstimate,
        measurement: TuningMeasurement,
        enabled: Boolean,
    ): FrozenMeasurementFrame {
        if (measurement.status.isStableTuningStatus()) {
            return FrozenMeasurementFrame(
                pitchEstimate = estimate,
                measurement = measurement,
                isFrozen = false,
            ).also { lastStableFrame = it }
        }

        if (!enabled || measurement.status != TuningStatus.WaitingForSignal) {
            return FrozenMeasurementFrame(
                pitchEstimate = estimate,
                measurement = measurement,
                isFrozen = false,
            )
        }

        return lastStableFrame?.copy(isFrozen = true)
            ?: FrozenMeasurementFrame(
                pitchEstimate = estimate,
                measurement = measurement,
                isFrozen = false,
            )
    }

    private fun TuningStatus.isStableTuningStatus(): Boolean = when (this) {
        TuningStatus.TuneUp,
        TuningStatus.TuneDown,
        TuningStatus.InTune,
        -> true

        TuningStatus.WaitingForSignal,
        TuningStatus.SignalClipping,
        TuningStatus.NoStringDetected,
        -> false
    }
}

data class FrozenMeasurementFrame(
    val pitchEstimate: PitchEstimate,
    val measurement: TuningMeasurement,
    val isFrozen: Boolean,
)
