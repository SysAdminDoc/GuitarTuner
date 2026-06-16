package com.sysadmindoc.guitartuner.pitch

import com.sysadmindoc.guitartuner.tuning.StandardGuitarTuning
import com.sysadmindoc.guitartuner.tuning.TuningAnalyzer
import com.sysadmindoc.guitartuner.tuning.TuningStatus
import java.util.Random
import kotlin.math.sin
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NoiseProfileTest {
    private val detector = YinPitchDetector()
    private val analyzer = TuningAnalyzer(StandardGuitarTuning.strings)
    private val sampleRate = 44_100
    private val frameSize = 4_096

    @Test
    fun whiteNoiseProducesNoStringDetection() {
        val rng = Random(42)
        val samples = FloatArray(frameSize) { (rng.nextFloat() * 2f - 1f) * 0.3f }
        val estimate = detector.detect(samples, sampleRate)
        val measurement = analyzer.analyze(estimate)

        assertTrue(
            "White noise should yield HighNoise or Unstable, got ${estimate.status}",
            estimate.status == SignalStatus.HighNoise || estimate.status == SignalStatus.Unstable,
        )
        assertNotEquals(
            "White noise must not detect a guitar string",
            TuningStatus.InTune,
            measurement.status,
        )
    }

    @Test
    fun quietAmbientNoiseIsSilentOrUnstable() {
        val rng = Random(123)
        val samples = FloatArray(frameSize) { (rng.nextFloat() * 2f - 1f) * 0.0005f }
        val estimate = detector.detect(samples, sampleRate)

        assertTrue(
            "Quiet ambient noise should be Silence or Unstable, got ${estimate.status}",
            estimate.status == SignalStatus.Silence || estimate.status == SignalStatus.Unstable,
        )
        assertNull(
            "Quiet ambient noise should not produce a frequency",
            estimate.frequencyHz,
        )
    }

    @Test
    fun impulseClickDoesNotFalseDetect() {
        val samples = FloatArray(frameSize)
        samples[frameSize / 4] = 0.9f
        samples[frameSize / 4 + 1] = -0.8f
        samples[frameSize / 2] = 0.7f
        samples[frameSize / 2 + 1] = -0.6f

        val estimate = detector.detect(samples, sampleRate)
        val measurement = analyzer.analyze(estimate)

        assertNotEquals(
            "Impulse clicks must not be detected as in-tune strings",
            TuningStatus.InTune,
            measurement.status,
        )
    }

    @Test
    fun loudWhiteNoiseReportsHighNoise() {
        val rng = Random(777)
        val samples = FloatArray(frameSize) { (rng.nextFloat() * 2f - 1f) * 0.8f }
        val estimate = detector.detect(samples, sampleRate)

        assertTrue(
            "Loud white noise should report HighNoise, got ${estimate.status}",
            estimate.status == SignalStatus.HighNoise || estimate.status == SignalStatus.Unstable,
        )
    }

    @Test
    fun guitarSignalBuriedInNoiseDoesNotFalseDetectWrongString() {
        val rng = Random(999)
        val targetHz = 110.0 // A2
        val signalAmplitude = 0.05f
        val noiseAmplitude = 0.15f
        val samples = FloatArray(frameSize) { i ->
            val signal = sin(2.0 * Math.PI * targetHz * i / sampleRate).toFloat() * signalAmplitude
            val noise = (rng.nextFloat() * 2f - 1f) * noiseAmplitude
            signal + noise
        }

        val estimate = detector.detect(samples, sampleRate)
        val measurement = analyzer.analyze(estimate)

        if (measurement.status == TuningStatus.InTune || measurement.status == TuningStatus.TuneUp || measurement.status == TuningStatus.TuneDown) {
            assertTrue(
                "If a string is detected through noise, it must be the correct one (A2=string 5), got string ${measurement.target?.stringNumber}",
                measurement.target?.stringNumber == 5,
            )
        }
    }
}
