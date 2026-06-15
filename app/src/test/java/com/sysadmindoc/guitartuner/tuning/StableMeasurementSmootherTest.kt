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
    fun holdsOverBriefNonStableFrames() {
        smoother.apply(measurement(82.9, 10.0))
        smoother.apply(measurement(82.7, 7.0))
        val lastStable = smoother.apply(measurement(82.8, 9.0))

        val duringHoldover = smoother.apply(TuningMeasurement.highNoise())
        assertEquals("holdover returns last stable", lastStable.target, duringHoldover.target)

        val afterHoldover = smoother.apply(measurement(82.6, 4.0))
        assertEquals("resumes after holdover", lowE, afterHoldover.target)
    }

    @Test
    fun resetsAfterExhaustedHoldover() {
        smoother.apply(measurement(82.9, 10.0))
        smoother.apply(measurement(82.7, 7.0))
        smoother.apply(measurement(82.8, 9.0))

        smoother.apply(TuningMeasurement.highNoise())
        smoother.apply(TuningMeasurement.highNoise())
        val thirdNonStable = smoother.apply(TuningMeasurement.highNoise())

        assertEquals(TuningStatus.HighNoise, thirdNonStable.status)
    }

    @Test
    fun resetsOnLargeSameTargetJump() {
        smoother.apply(measurement(82.4, 0.0))
        smoother.apply(measurement(82.5, 2.0))

        val jump = smoother.apply(measurement(84.5, 45.0))

        assertEquals(45.0, jump.cents ?: 0.0, 0.0)
        assertEquals(TuningStatus.TuneDown, jump.status)
    }

    @Test
    fun resistsOctaveFlipBelowThreshold() {
        val g3 = GuitarString(stringNumber = 3, name = "G", scientificPitch = "G3", frequencyHz = 196.0)
        val g4 = GuitarString(stringNumber = 0, name = "G4", scientificPitch = "G4", frequencyHz = 392.0)

        smoother.apply(measurement(196.0, 0.0, g3))
        smoother.apply(measurement(196.1, 1.0, g3))
        val lastG3 = smoother.apply(measurement(195.8, -2.0, g3))

        val flip1 = smoother.apply(measurement(392.0, 0.0, g4))
        assertEquals("first octave flip suppressed", lastG3.target, flip1.target)

        val flip2 = smoother.apply(measurement(392.1, 1.0, g4))
        assertEquals("second octave flip suppressed", lastG3.target, flip2.target)
    }

    @Test
    fun acceptsOctaveFlipAfterThreshold() {
        val g3 = GuitarString(stringNumber = 3, name = "G", scientificPitch = "G3", frequencyHz = 196.0)
        val g4 = GuitarString(stringNumber = 0, name = "G4", scientificPitch = "G4", frequencyHz = 392.0)

        smoother.apply(measurement(196.0, 0.0, g3))
        smoother.apply(measurement(196.1, 1.0, g3))
        smoother.apply(measurement(195.8, -2.0, g3))

        smoother.apply(measurement(392.0, 0.0, g4))
        smoother.apply(measurement(392.1, 1.0, g4))
        val accepted = smoother.apply(measurement(392.2, 2.0, g4))

        assertEquals("octave flip accepted after threshold", g4, accepted.target)
    }

    @Test
    fun allowsNonOctaveTargetChangeImmediately() {
        smoother.apply(measurement(82.9, 10.0))
        smoother.apply(measurement(82.7, 7.0))
        smoother.apply(measurement(82.8, 9.0))

        val newTarget = smoother.apply(measurement(110.0, 0.0, aString))

        assertEquals("non-octave target change is immediate", aString, newTarget.target)
    }

    @Test
    fun preservesOvershootWarningAfterSmoothing() {
        val overshoot1 = overshoot(390.0, 292.5)
        val overshoot2 = overshoot(395.0, 314.5)
        val overshoot3 = overshoot(400.0, 336.3)

        assertSame(overshoot1, smoother.apply(overshoot1))
        smoother.apply(overshoot2)
        val smoothed = smoother.apply(overshoot3)

        assertEquals(TuningStatus.Overshoot, smoothed.status)
        assertEquals(TuningDirection.TuneDown, smoothed.direction)
    }

    @Test
    fun preservesAnalyzerInTuneDirectionForRelaxedTolerance() {
        val inTuneAtEightCents = measurementWithDirection(
            frequencyHz = 82.79,
            cents = 8.0,
            direction = TuningDirection.InTune,
        )

        smoother.apply(inTuneAtEightCents)
        smoother.apply(inTuneAtEightCents.copy(frequencyHz = 82.81, cents = 8.4))
        val smoothed = smoother.apply(inTuneAtEightCents.copy(frequencyHz = 82.77, cents = 7.6))

        assertEquals(TuningStatus.InTune, smoothed.status)
        assertEquals(TuningDirection.InTune, smoothed.direction)
    }

    private fun measurement(
        frequencyHz: Double,
        cents: Double,
        target: GuitarString = lowE,
    ): TuningMeasurement = measurementWithDirection(
        frequencyHz = frequencyHz,
        cents = cents,
        target = target,
        direction = when {
            abs(cents) <= InTuneCents -> TuningDirection.InTune
            cents < 0.0 -> TuningDirection.TuneUp
            else -> TuningDirection.TuneDown
        },
    )

    private fun measurementWithDirection(
        frequencyHz: Double,
        cents: Double,
        target: GuitarString = lowE,
        direction: TuningDirection,
    ): TuningMeasurement = TuningMeasurement.detected(
        target = target,
        frequencyHz = frequencyHz,
        cents = cents,
        confidence = 0.9,
        direction = direction,
    )

    private fun overshoot(
        frequencyHz: Double,
        cents: Double,
    ): TuningMeasurement = TuningMeasurement.overshoot(
        target = lowE,
        frequencyHz = frequencyHz,
        cents = cents,
        confidence = 0.9,
    )

    private companion object {
        const val InTuneCents = 5.0
    }
}
