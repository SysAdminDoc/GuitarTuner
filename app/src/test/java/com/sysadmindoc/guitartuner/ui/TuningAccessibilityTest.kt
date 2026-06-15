package com.sysadmindoc.guitartuner.ui

import com.sysadmindoc.guitartuner.R
import com.sysadmindoc.guitartuner.tuning.StandardGuitarTuning
import com.sysadmindoc.guitartuner.tuning.TuningDirection
import com.sysadmindoc.guitartuner.tuning.TuningMeasurement
import org.junit.Assert.assertEquals
import org.junit.Test

class TuningAccessibilityTest {

    private val englishStrings = mapOf(
        R.string.a11y_meter_description to "Guitar tuning meter",
        R.string.a11y_state_waiting to "Waiting for a detected guitar string",
        R.string.a11y_state_clipping to "Input is clipping; play softer",
        R.string.a11y_state_high_noise to "Background noise is too high",
        R.string.a11y_state_no_string to "No guitar string detected",
        R.string.a11y_in_tune to "In tune",
        R.string.a11y_string_in_tune to "%1\$s, in tune",
        R.string.a11y_tune_down_risk to "tune down, string at risk",
        R.string.a11y_string_tune_down_risk to "%1\$s, tune down, string at risk",
        R.string.a11y_tune_direction to "%1\$s, %2\$s cents %3\$s",
        R.string.a11y_string_tune_direction to "%1\$s, %2\$s, %3\$s cents %4\$s",
        R.string.a11y_string_label to "string %1\$d, %2\$s, %3\$s",
        R.string.a11y_direction_tune_up to "tune up",
        R.string.a11y_direction_tune_down to "tune down",
        R.string.a11y_pitch_flat to "flat",
        R.string.a11y_pitch_sharp to "sharp",
    )

    private val resolver: (Int, Array<out Any>) -> String = { resId, args ->
        val template = englishStrings[resId] ?: error("Unknown string resource: $resId")
        if (args.isEmpty()) template else String.format(template, *args)
    }

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
            resolver,
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
            resolver,
        )

        assertEquals("string 1, High E, E4, in tune", info.stateDescription)
    }

    @Test
    fun describesSignalProblemsWithoutPitchDetails() {
        assertEquals(
            "Waiting for a detected guitar string",
            tuningMeterAccessibility(TuningMeasurement.waiting(), resolver).stateDescription,
        )
        assertEquals(
            "Input is clipping; play softer",
            tuningMeterAccessibility(TuningMeasurement.signalClipping(), resolver).stateDescription,
        )
        assertEquals(
            "Background noise is too high",
            tuningMeterAccessibility(TuningMeasurement.highNoise(), resolver).stateDescription,
        )
        assertEquals(
            "No guitar string detected",
            tuningMeterAccessibility(TuningMeasurement.noStringDetected(), resolver).stateDescription,
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
        resolver,
    )
}
