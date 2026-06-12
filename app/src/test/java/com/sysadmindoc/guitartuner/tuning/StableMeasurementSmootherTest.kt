package com.sysadmindoc.guitartuner.tuning

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class StableMeasurementSmootherTest {
    private val config = StableMeasurementSmootherConfig(
        attackFramesToSkip = 1,
        stableWindowSize = 3,
        maxInterFrameCents = 20.0,
        inTuneCents = 5.0,
    )
    private val smoother = StableMeasurementSmoother(config)
    private val lowE = StandardGuitarTuning.strings.first { it.scientificPitch == "E2" }
    private val aString = StandardGuitarTuning.strings.first { it.scientificPitch == "A2" }

    @Test
    fun averagesSameTargetFramesAfterAttack() {
        val attackFrame = measurement(82.9, 10.0)

        assertSame(attackFrame, smoother.apply(attackFrame))

        smoother.apply(measurement(82.7, 7.0))
        smoother.apply(measurement(82.8, 9.0))
        val smoothed = smoother.apply(measurement(82.5, 3.0))

        assertEquals(lowE, smoothed.target)
        assertTrue(abs((smoothed.cents ?: 99.0) - 6.33) < 0.05)
        assertTrue(abs((smoothed.frequencyHz ?: 0.0) - 82.67) < 0.05)
        assertEquals(TuningStatus.TuneDown, smoothed.status)
    }

    @Test
    fun resetsWhenTargetChanges() {
        smoother.apply(measurement(82.9, 10.0))
        smoother.apply(measurement(82.7, 7.0))
        smoother.apply(measurement(82.8, 9.0))

        val newTarget = smoother.apply(measurement(110.0, 0.0, aString))

        assertEquals(aString, newTarget.target)
        assertEquals(0.0, newTarget.cents ?: 99.0, 0.0)
        assertEquals(TuningStatus.InTune, newTarget.status)
    }

    @Test
    fun resetsOnNonStableMeasurements() {
        smoother.apply(measurement(82.9, 10.0))
        smoother.apply(measurement(82.7, 7.0))

        assertEquals(TuningStatus.HighNoise, smoother.apply(TuningMeasurement.highNoise()).status)
        val afterNoise = smoother.apply(measurement(82.4, 0.0))

        assertEquals(0.0, afterNoise.cents ?: 99.0, 0.0)
        assertEquals(TuningStatus.InTune, afterNoise.status)
    }

    @Test
    fun resetsOnLargeSameTargetJump() {
        smoother.apply(measurement(82.4, 0.0))
        smoother.apply(measurement(82.5, 2.0))

        val jump = smoother.apply(measurement(84.5, 45.0))

        assertEquals(45.0, jump.cents ?: 0.0, 0.0)
        assertEquals(TuningStatus.TuneDown, jump.status)
    }

    private fun measurement(
        frequencyHz: Double,
        cents: Double,
        target: GuitarString = lowE,
    ): TuningMeasurement = TuningMeasurement.detected(
        target = target,
        frequencyHz = frequencyHz,
        cents = cents,
        confidence = 0.9,
        direction = when {
            abs(cents) <= config.inTuneCents -> TuningDirection.InTune
            cents < 0.0 -> TuningDirection.TuneUp
            else -> TuningDirection.TuneDown
        },
    )
}
