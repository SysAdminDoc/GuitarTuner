package com.sysadmindoc.guitartuner.tuning

import com.sysadmindoc.guitartuner.pitch.PitchDetectorConfig
import com.sysadmindoc.guitartuner.pitch.SignalStatus
import com.sysadmindoc.guitartuner.pitch.YinPitchDetector
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AlternateTuningDetectionTest {

    @Test
    fun dropDDetectsLowDString() {
        val detector = YinPitchDetector()
        val analyzer = TuningAnalyzer(GuitarTunings.dropD.strings)
        val signal = guitarLikeSignal(73.42, secondHarmonicLevel = 0.20)
        val estimate = detector.detect(signal, SampleRate)
        val measurement = analyzer.analyze(estimate)

        assertEquals(SignalStatus.Detected, estimate.status)
        assertEquals(TuningStatus.InTune, measurement.status)
        assertEquals(6, measurement.target?.stringNumber)
        assertEquals("D2", measurement.target?.scientificPitch)
    }

    @Test
    fun dropDDetectsStandardAString() {
        val detector = YinPitchDetector()
        val analyzer = TuningAnalyzer(GuitarTunings.dropD.strings)
        val signal = guitarLikeSignal(110.0, secondHarmonicLevel = 0.15)
        val estimate = detector.detect(signal, SampleRate)
        val measurement = analyzer.analyze(estimate)

        assertEquals(SignalStatus.Detected, estimate.status)
        assertEquals(TuningStatus.InTune, measurement.status)
        assertEquals(5, measurement.target?.stringNumber)
    }

    @Test
    fun openGDetectsLowDString() {
        val detector = YinPitchDetector()
        val analyzer = TuningAnalyzer(GuitarTunings.openG.strings)
        val signal = guitarLikeSignal(73.42, secondHarmonicLevel = 0.22)
        val estimate = detector.detect(signal, SampleRate)
        val measurement = analyzer.analyze(estimate)

        assertEquals(SignalStatus.Detected, estimate.status)
        assertEquals(TuningStatus.InTune, measurement.status)
        assertEquals(6, measurement.target?.stringNumber)
        assertEquals("D2", measurement.target?.scientificPitch)
    }

    @Test
    fun openGDetectsHighDString() {
        val detector = YinPitchDetector()
        val analyzer = TuningAnalyzer(GuitarTunings.openG.strings)
        val signal = guitarLikeSignal(293.66, secondHarmonicLevel = 0.10)
        val estimate = detector.detect(signal, SampleRate)
        val measurement = analyzer.analyze(estimate)

        assertEquals(SignalStatus.Detected, estimate.status)
        assertEquals(TuningStatus.InTune, measurement.status)
        assertEquals(1, measurement.target?.stringNumber)
        assertEquals("D4", measurement.target?.scientificPitch)
    }

    @Test
    fun dadgadDetectsAllStrings() {
        val detector = YinPitchDetector()
        val analyzer = TuningAnalyzer(GuitarTunings.dadgad.strings)

        for (string in GuitarTunings.dadgad.strings) {
            val signal = guitarLikeSignal(string.frequencyHz, secondHarmonicLevel = 0.18)
            val estimate = detector.detect(signal, SampleRate)
            val measurement = analyzer.analyze(estimate)

            assertEquals("${string.scientificPitch} status", SignalStatus.Detected, estimate.status)
            assertNotNull("${string.scientificPitch} frequency", estimate.frequencyHz)
            assertTrue(
                "${string.scientificPitch} expected ${string.frequencyHz}, got ${estimate.frequencyHz}",
                abs(requireNotNull(estimate.frequencyHz) - string.frequencyHz) < 2.0,
            )
        }
    }

    @Test
    fun bassStandardDetectsLowEString() {
        val detector = YinPitchDetector(
            PitchDetectorConfig(minFrequencyHz = 35.0, maxFrequencyHz = 200.0),
        )
        val analyzer = TuningAnalyzer(GuitarTunings.bassStandard.strings)
        val signal = guitarLikeSignal(41.20, secondHarmonicLevel = 0.15, sampleRate = SampleRate48k)
        val estimate = detector.detect(signal, SampleRate48k)
        val measurement = analyzer.analyze(estimate)

        assertEquals(SignalStatus.Detected, estimate.status)
        assertNotNull(estimate.frequencyHz)
        assertTrue(abs(requireNotNull(estimate.frequencyHz) - 41.20) < 2.0)
        assertEquals(4, measurement.target?.stringNumber)
        assertEquals("E1", measurement.target?.scientificPitch)
    }

    @Test
    fun bassStandardDetectsGString() {
        val detector = YinPitchDetector(
            PitchDetectorConfig(minFrequencyHz = 35.0, maxFrequencyHz = 200.0),
        )
        val analyzer = TuningAnalyzer(GuitarTunings.bassStandard.strings)
        val signal = guitarLikeSignal(98.0, secondHarmonicLevel = 0.12)
        val estimate = detector.detect(signal, SampleRate)
        val measurement = analyzer.analyze(estimate)

        assertEquals(SignalStatus.Detected, estimate.status)
        assertEquals(TuningStatus.InTune, measurement.status)
        assertEquals(1, measurement.target?.stringNumber)
        assertEquals("G2", measurement.target?.scientificPitch)
    }

    @Test
    fun ukuleleStandardDetectsHighGString() {
        val detector = YinPitchDetector(
            PitchDetectorConfig(minFrequencyHz = 200.0, maxFrequencyHz = 520.0),
        )
        val analyzer = TuningAnalyzer(GuitarTunings.ukuleleStandard.strings)
        val signal = guitarLikeSignal(392.0, secondHarmonicLevel = 0.10)
        val estimate = detector.detect(signal, SampleRate)
        val measurement = analyzer.analyze(estimate)

        assertEquals(SignalStatus.Detected, estimate.status)
        assertNotNull(estimate.frequencyHz)
        assertTrue(abs(requireNotNull(estimate.frequencyHz) - 392.0) < 2.0)
        assertEquals(4, measurement.target?.stringNumber)
        assertEquals("G4", measurement.target?.scientificPitch)
    }

    @Test
    fun ukuleleStandardDetectsAString() {
        val detector = YinPitchDetector(
            PitchDetectorConfig(minFrequencyHz = 200.0, maxFrequencyHz = 520.0),
        )
        val analyzer = TuningAnalyzer(GuitarTunings.ukuleleStandard.strings)
        val signal = guitarLikeSignal(440.0, secondHarmonicLevel = 0.10)
        val estimate = detector.detect(signal, SampleRate)
        val measurement = analyzer.analyze(estimate)

        assertEquals(SignalStatus.Detected, estimate.status)
        assertEquals(TuningStatus.InTune, measurement.status)
        assertEquals(1, measurement.target?.stringNumber)
        assertEquals("A4", measurement.target?.scientificPitch)
    }

    @Test
    fun halfStepDownDetectsLowEbString() {
        val detector = YinPitchDetector()
        val analyzer = TuningAnalyzer(GuitarTunings.halfStepDown.strings)
        val signal = guitarLikeSignal(77.78, secondHarmonicLevel = 0.22)
        val estimate = detector.detect(signal, SampleRate)
        val measurement = analyzer.analyze(estimate)

        assertEquals(SignalStatus.Detected, estimate.status)
        assertEquals(6, measurement.target?.stringNumber)
        assertEquals("Eb2", measurement.target?.scientificPitch)
    }

    private fun guitarLikeSignal(
        frequencyHz: Double,
        secondHarmonicLevel: Double,
        noiseLevel: Double = 0.0,
        fundamentalLevel: Double = 1.0,
        sampleRate: Int = SampleRate,
    ): FloatArray {
        val length = (sampleRate * 0.35).toInt()
        val random = Random(7)
        return FloatArray(length) { index ->
            val time = index / sampleRate.toDouble()
            val fundamental = sin(2.0 * PI * frequencyHz * time) * fundamentalLevel
            val harmonic = sin(2.0 * PI * frequencyHz * 2.0 * time) * secondHarmonicLevel
            val noise = (random.nextDouble() * 2.0 - 1.0) * noiseLevel
            ((fundamental + harmonic + noise) * 0.58).toFloat()
        }
    }

    private companion object {
        const val SampleRate = 44_100
        const val SampleRate48k = 48_000
    }
}
