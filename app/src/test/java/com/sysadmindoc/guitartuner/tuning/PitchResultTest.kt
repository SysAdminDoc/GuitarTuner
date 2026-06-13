package com.sysadmindoc.guitartuner.tuning

import com.sysadmindoc.guitartuner.pitch.PitchEstimate
import com.sysadmindoc.guitartuner.pitch.SignalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PitchResultTest {
    private val lowE = StandardGuitarTuning.strings.first { it.scientificPitch == "E2" }

    @Test
    fun detectedResultExposesFrequencyNearestNoteCentsConfidenceAndSignalState() {
        val result = PitchResult.from(
            estimate = estimate(frequencyHz = 82.41, confidence = 0.92, status = SignalStatus.Detected),
            measurement = TuningMeasurement.detected(
                target = lowE,
                frequencyHz = 82.41,
                cents = -1.2,
                confidence = 0.92,
                direction = TuningDirection.TuneUp,
            ),
        )

        assertEquals(82.41, result.frequencyHz ?: 0.0, 0.0)
        assertEquals("E2", result.nearestNote)
        assertEquals(-1.2, result.centsOffset ?: 0.0, 0.0)
        assertEquals(0.92, result.confidence, 0.0)
        assertEquals(PitchSignalState.Detected, result.signalState)
    }

    @Test
    fun noStringDetectedKeepsRawFrequencyWithoutNearestNote() {
        val result = PitchResult.from(
            estimate = estimate(frequencyHz = 61.0, confidence = 0.8, status = SignalStatus.Detected),
            measurement = TuningMeasurement.noStringDetected(frequencyHz = 61.0, confidence = 0.8),
        )

        assertEquals(61.0, result.frequencyHz ?: 0.0, 0.0)
        assertNull(result.nearestNote)
        assertNull(result.centsOffset)
        assertEquals(0.8, result.confidence, 0.0)
        assertEquals(PitchSignalState.NoStringDetected, result.signalState)
    }

    @Test
    fun signalStatesReflectCaptureGuardrails() {
        assertEquals(
            PitchSignalState.WaitingForSignal,
            PitchResult.from(PitchEstimate.silence(), TuningMeasurement.waiting()).signalState,
        )
        assertEquals(
            PitchSignalState.Clipping,
            PitchResult.from(
                estimate(frequencyHz = null, confidence = 0.0, status = SignalStatus.Clipping),
                TuningMeasurement.signalClipping(),
            ).signalState,
        )
        assertEquals(
            PitchSignalState.HighNoise,
            PitchResult.from(
                estimate(frequencyHz = null, confidence = 0.0, status = SignalStatus.HighNoise),
                TuningMeasurement.highNoise(),
            ).signalState,
        )
        assertEquals(
            PitchSignalState.Unstable,
            PitchResult.from(
                estimate(frequencyHz = null, confidence = 0.0, status = SignalStatus.Unstable),
                TuningMeasurement.noStringDetected(),
            ).signalState,
        )
    }

    private fun estimate(
        frequencyHz: Double?,
        confidence: Double,
        status: SignalStatus,
    ): PitchEstimate = PitchEstimate(
        frequencyHz = frequencyHz,
        confidence = confidence,
        rms = 0.2,
        clipping = status == SignalStatus.Clipping,
        status = status,
    )
}
