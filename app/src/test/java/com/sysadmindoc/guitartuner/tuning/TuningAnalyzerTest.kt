package com.sysadmindoc.guitartuner.tuning

import com.sysadmindoc.guitartuner.pitch.PitchEstimate
import com.sysadmindoc.guitartuner.pitch.SignalStatus
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
}
