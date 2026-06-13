package com.sysadmindoc.guitartuner.tuning

import com.sysadmindoc.guitartuner.pitch.PitchEstimate
import com.sysadmindoc.guitartuner.pitch.SignalStatus
import kotlin.math.pow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TuningAnalyzerTest {
    private val analyzer = TuningAnalyzer(StandardGuitarTuning.strings)

    @Test
    fun mapsEveryStandardStringToItself() {
        for (string in StandardGuitarTuning.strings) {
            val measurement = analyzer.analyze(detectedPitch(string.frequencyHz))

            assertEquals("${string.scientificPitch} should be in tune", TuningStatus.InTune, measurement.status)
            assertEquals(string, measurement.target)
            assertTrue(kotlin.math.abs(measurement.cents ?: 99.0) < 0.5)
        }
    }

    @Test
    fun mapsDetectedLowEToInTuneString() {
        val measurement = analyzer.analyze(detectedPitch(82.41))

        assertEquals(TuningStatus.InTune, measurement.status)
        assertEquals("Low E", measurement.target?.name)
        assertTrue(kotlin.math.abs(measurement.cents ?: 99.0) < 0.5)
    }

    @Test
    fun correctsLowESecondHarmonicToFundamental() {
        val measurement = analyzer.analyze(detectedPitch(164.82))

        assertEquals(TuningStatus.InTune, measurement.status)
        assertEquals("Low E", measurement.target?.name)
        assertTrue(kotlin.math.abs((measurement.frequencyHz ?: 0.0) - 82.41) < 0.01)
        assertTrue(kotlin.math.abs(measurement.cents ?: 99.0) < 0.5)
    }

    @Test
    fun correctsASecondHarmonicToFundamental() {
        val measurement = analyzer.analyze(detectedPitch(220.0))

        assertEquals(TuningStatus.InTune, measurement.status)
        assertEquals("A", measurement.target?.name)
        assertTrue(kotlin.math.abs((measurement.frequencyHz ?: 0.0) - 110.0) < 0.01)
        assertTrue(kotlin.math.abs(measurement.cents ?: 99.0) < 0.5)
    }

    @Test
    fun guidedModeLocksOntoSelectedString() {
        val guidedLowE = TuningAnalyzer(
            strings = StandardGuitarTuning.strings,
            targetSelection = TuningTargetSelection.guided(6),
        )

        val measurement = guidedLowE.analyze(detectedPitch(80.0))

        assertEquals(TuningStatus.TuneUp, measurement.status)
        assertEquals("Low E", measurement.target?.name)
    }

    @Test
    fun guidedModeRejectsWrongOpenString() {
        val guidedLowE = TuningAnalyzer(
            strings = StandardGuitarTuning.strings,
            targetSelection = TuningTargetSelection.guided(6),
        )

        val measurement = guidedLowE.analyze(detectedPitch(110.0))

        assertEquals(TuningStatus.NoStringDetected, measurement.status)
        assertEquals(null, measurement.target)
    }

    @Test
    fun autoModeSelectsNearestStrummedString() {
        val measurement = analyzer.analyze(detectedPitch(110.0))

        assertEquals(TuningStatus.InTune, measurement.status)
        assertEquals("A", measurement.target?.name)
    }

    @Test
    fun keepsTrueDStringAtFundamental() {
        val measurement = analyzer.analyze(detectedPitch(146.83))

        assertEquals(TuningStatus.InTune, measurement.status)
        assertEquals("D", measurement.target?.name)
        assertTrue(kotlin.math.abs((measurement.frequencyHz ?: 0.0) - 146.83) < 0.01)
        assertTrue(kotlin.math.abs(measurement.cents ?: 99.0) < 0.5)
    }

    @Test
    fun reportsTuneUpWhenStringIsFlat() {
        val measurement = analyzer.analyze(detectedPitch(80.0))

        assertEquals(TuningStatus.TuneUp, measurement.status)
        assertEquals("Low E", measurement.target?.name)
    }

    @Test
    fun reportsTuneDownWhenStringIsSharp() {
        val measurement = analyzer.analyze(detectedPitch(85.0))

        assertEquals(TuningStatus.TuneDown, measurement.status)
        assertEquals("Low E", measurement.target?.name)
    }

    @Test
    fun usesConfiguredCentsToleranceForInTuneWindow() {
        val strict = TuningAnalyzer(StandardGuitarTuning.strings, inTuneCents = 5.0)
        val relaxed = TuningAnalyzer(StandardGuitarTuning.strings, inTuneCents = 10.0)
        val eightCentsSharp = frequencyWithCents(StandardGuitarTuning.strings.first().frequencyHz, 8.0)

        assertEquals(TuningStatus.TuneDown, strict.analyze(detectedPitch(eightCentsSharp)).status)
        assertEquals(TuningStatus.InTune, relaxed.analyze(detectedPitch(eightCentsSharp)).status)
    }

    @Test
    fun mapsHighNoiseEstimateToHighNoiseState() {
        val measurement = analyzer.analyze(
            PitchEstimate(
                frequencyHz = null,
                confidence = 0.0,
                rms = 0.2,
                clipping = false,
                status = SignalStatus.HighNoise,
            ),
        )

        assertEquals(TuningStatus.HighNoise, measurement.status)
    }

    private fun detectedPitch(frequencyHz: Double): PitchEstimate = PitchEstimate(
        frequencyHz = frequencyHz,
        confidence = 0.95,
        rms = 0.25,
        clipping = false,
        status = SignalStatus.Detected,
    )

    private fun frequencyWithCents(frequencyHz: Double, cents: Double): Double =
        frequencyHz * 2.0.pow(cents / 1200.0)
}
