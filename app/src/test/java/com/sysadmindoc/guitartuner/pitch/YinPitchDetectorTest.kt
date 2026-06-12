package com.sysadmindoc.guitartuner.pitch

import com.sysadmindoc.guitartuner.tuning.StandardGuitarTuning
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YinPitchDetectorTest {
    private val detector = YinPitchDetector()

    @Test
    fun detectsAllStandardGuitarStrings() {
        for (string in StandardGuitarTuning.strings) {
            val estimate = detector.detect(
                samples = guitarLikeSignal(frequencyHz = string.frequencyHz, secondHarmonicLevel = 0.18),
                sampleRate = SampleRate,
            )

            assertEquals("${string.scientificPitch} should be detected", SignalStatus.Detected, estimate.status)
            val frequencyHz = requireNotNull(estimate.frequencyHz)
            assertTrue(
                "${string.scientificPitch} expected ${string.frequencyHz}, got $frequencyHz",
                abs(frequencyHz - string.frequencyHz) < 1.5,
            )
        }
    }

    @Test
    fun detectsLowEWithSecondHarmonic() {
        val estimate = detector.detect(
            samples = guitarLikeSignal(frequencyHz = 82.41, secondHarmonicLevel = 0.32),
            sampleRate = SampleRate,
        )

        assertEquals(SignalStatus.Detected, estimate.status)
        assertNotNull(estimate.frequencyHz)
        assertTrue(abs(estimate.frequencyHz!! - 82.41) < 1.0)
    }

    @Test
    fun detectsNoisyLowE() {
        val estimate = detector.detect(
            samples = guitarLikeSignal(
                frequencyHz = 82.41,
                secondHarmonicLevel = 0.24,
                noiseLevel = 0.08,
            ),
            sampleRate = SampleRate,
        )

        assertEquals(SignalStatus.Detected, estimate.status)
        val frequencyHz = requireNotNull(estimate.frequencyHz)
        assertTrue(abs(frequencyHz - 82.41) < 1.25)
    }

    @Test
    fun avoidsG3ToG4OctaveJumpWhenSecondHarmonicIsPresent() {
        val estimate = detector.detect(
            samples = guitarLikeSignal(frequencyHz = 196.0, secondHarmonicLevel = 0.40),
            sampleRate = SampleRate,
        )

        assertEquals(SignalStatus.Detected, estimate.status)
        assertNotNull(estimate.frequencyHz)
        assertTrue(abs(estimate.frequencyHz!! - 196.0) < 1.5)
        val frequencyHz = requireNotNull(estimate.frequencyHz)
        assertTrue(abs(frequencyHz - 392.0) > 100.0)
    }

    @Test
    fun reportsSilenceForQuietInput() {
        val estimate = detector.detect(FloatArray(4096), SampleRate)

        assertEquals(SignalStatus.Silence, estimate.status)
        assertEquals(null, estimate.frequencyHz)
    }

    @Test
    fun reportsHighNoiseForLoudUnpitchedInput() {
        val random = Random(11)
        val noise = FloatArray(4096) {
            ((random.nextDouble() * 2.0 - 1.0) * 0.35).toFloat()
        }

        val estimate = detector.detect(noise, SampleRate)

        assertEquals(SignalStatus.HighNoise, estimate.status)
        assertEquals(null, estimate.frequencyHz)
    }

    private fun guitarLikeSignal(
        frequencyHz: Double,
        secondHarmonicLevel: Double,
        noiseLevel: Double = 0.0,
    ): FloatArray {
        val length = (SampleRate * 0.35).toInt()
        val random = Random(7)
        return FloatArray(length) { index ->
            val time = index / SampleRate.toDouble()
            val fundamental = sin(2.0 * PI * frequencyHz * time)
            val harmonic = sin(2.0 * PI * frequencyHz * 2.0 * time) * secondHarmonicLevel
            val noise = (random.nextDouble() * 2.0 - 1.0) * noiseLevel
            ((fundamental + harmonic + noise) * 0.58).toFloat()
        }
    }

    private companion object {
        const val SampleRate = 44_100
    }
}
