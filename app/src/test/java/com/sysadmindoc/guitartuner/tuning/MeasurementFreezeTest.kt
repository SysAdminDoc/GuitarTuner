package com.sysadmindoc.guitartuner.tuning

import com.sysadmindoc.guitartuner.pitch.PitchEstimate
import com.sysadmindoc.guitartuner.pitch.SignalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MeasurementFreezeTest {
    private val lowE = StandardGuitarTuning.strings.first()
    private val stableEstimate = PitchEstimate(
        frequencyHz = 82.41,
        confidence = 0.96,
        rms = 0.2,
        clipping = false,
        status = SignalStatus.Detected,
    )
    private val silenceEstimate = PitchEstimate.silence()
    private val stableMeasurement = TuningMeasurement.detected(
        target = lowE,
        frequencyHz = 82.41,
        cents = 0.0,
        confidence = 0.96,
        direction = TuningDirection.InTune,
    )

    @Test
    fun retainsLastStableMeasurementAfterDecayWhenEnabled() {
        val freeze = MeasurementFreeze()
        freeze.apply(stableEstimate, stableMeasurement, enabled = true)

        val frozen = freeze.apply(
            estimate = silenceEstimate,
            measurement = TuningMeasurement.waiting(),
            enabled = true,
        )

        assertTrue(frozen.isFrozen)
        assertEquals(stableMeasurement, frozen.measurement)
        assertEquals(stableEstimate, frozen.pitchEstimate)
    }

    @Test
    fun doesNotFreezeAfterDecayWhenDisabled() {
        val freeze = MeasurementFreeze()
        freeze.apply(stableEstimate, stableMeasurement, enabled = false)

        val frame = freeze.apply(
            estimate = silenceEstimate,
            measurement = TuningMeasurement.waiting(),
            enabled = false,
        )

        assertFalse(frame.isFrozen)
        assertEquals(TuningStatus.WaitingForSignal, frame.measurement.status)
    }

    @Test
    fun replacesFrozenMeasurementWhenNewStableNoteArrives() {
        val freeze = MeasurementFreeze()
        freeze.apply(stableEstimate, stableMeasurement, enabled = true)
        freeze.apply(silenceEstimate, TuningMeasurement.waiting(), enabled = true)

        val highE = StandardGuitarTuning.strings.last()
        val newMeasurement = TuningMeasurement.detected(
            target = highE,
            frequencyHz = 329.63,
            cents = 0.0,
            confidence = 0.98,
            direction = TuningDirection.InTune,
        )
        val frame = freeze.apply(
            estimate = stableEstimate.copy(frequencyHz = 329.63),
            measurement = newMeasurement,
            enabled = true,
        )

        assertFalse(frame.isFrozen)
        assertEquals(newMeasurement, frame.measurement)
    }

    @Test
    fun resetClearsStoredStableMeasurement() {
        val freeze = MeasurementFreeze()
        freeze.apply(stableEstimate, stableMeasurement, enabled = true)

        freeze.reset()
        val frame = freeze.apply(
            estimate = silenceEstimate,
            measurement = TuningMeasurement.waiting(),
            enabled = true,
        )

        assertFalse(frame.isFrozen)
        assertEquals(TuningStatus.WaitingForSignal, frame.measurement.status)
    }
}
