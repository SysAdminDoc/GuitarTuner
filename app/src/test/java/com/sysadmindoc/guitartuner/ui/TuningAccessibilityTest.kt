package com.sysadmindoc.guitartuner.ui

import com.sysadmindoc.guitartuner.tuning.StandardGuitarTuning
import com.sysadmindoc.guitartuner.tuning.TuningDirection
import com.sysadmindoc.guitartuner.tuning.TuningMeasurement
import org.junit.Assert.assertEquals
import org.junit.Test

class TuningAccessibilityTest {
    @Test
    fun describesDetectedStringDirectionAndCentsBucket() {
        val info = tuningMeterAccessibility(
            TuningMeasurement.detected(
                target = StandardGuitarTuning.strings.first(),
                frequencyHz = 81.82,
                cents = -12.4,
                confidence = 0.93,
                direction = TuningDirection.TuneUp,
            ),
        )

        assertEquals("Guitar tuning meter", info.contentDescription)
        assertEquals("string 6, Low E, E2, tune up, 10 cents flat", info.stateDescription)
        assertEquals(-12.4f, info.progressCents)
    }

    @Test
    fun usesStableBucketsForSmallFrameToFrameJitter() {
        val first = accessibilityForCents(-12.1).stateDescription
        val second = accessibilityForCents(-12.4).stateDescription

        assertEquals(first, second)
    }

    @Test
    fun describesInTuneStringWithoutCentsNoise() {
        val info = tuningMeterAccessibility(
            TuningMeasurement.detected(
                target = StandardGuitarTuning.strings.last(),
                frequencyHz = 329.63,
                cents = 1.8,
                confidence = 0.98,
                direction = TuningDirection.InTune,
            ),
        )

        assertEquals("string 1, High E, E4, in tune", info.stateDescription)
    }

    @Test
    fun describesSignalProblemsWithoutPitchDetails() {
        assertEquals(
            "Waiting for a detected guitar string",
            tuningMeterAccessibility(TuningMeasurement.waiting()).stateDescription,
        )
        assertEquals(
            "Input is clipping; play softer",
            tuningMeterAccessibility(TuningMeasurement.signalClipping()).stateDescription,
        )
        assertEquals(
            "Background noise is too high",
            tuningMeterAccessibility(TuningMeasurement.highNoise()).stateDescription,
        )
        assertEquals(
            "No guitar string detected",
            tuningMeterAccessibility(TuningMeasurement.noStringDetected()).stateDescription,
        )
    }

    private fun accessibilityForCents(cents: Double) = tuningMeterAccessibility(
        TuningMeasurement.detected(
            target = StandardGuitarTuning.strings.first(),
            frequencyHz = 81.82,
            cents = cents,
            confidence = 0.93,
            direction = TuningDirection.TuneUp,
        ),
    )
}
