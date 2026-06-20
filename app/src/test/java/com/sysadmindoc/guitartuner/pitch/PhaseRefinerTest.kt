package com.sysadmindoc.guitartuner.pitch

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import org.junit.Assert.assertTrue
import org.junit.Test

class PhaseRefinerTest {
    @Test
    fun refinesFrequencyCloserToTruth() {
        val trueFrequency = 110.0
        val coarseFrequency = 111.0
        val sampleRate = 44_100
        val frameSize = 4096
        val hopSize = frameSize / 2
        val refiner = PhaseRefiner()

        val continuous = FloatArray(frameSize + hopSize) { i ->
            (sin(2.0 * PI * trueFrequency * i / sampleRate) * 0.5).toFloat()
        }
        val frame1 = continuous.copyOfRange(0, frameSize)
        val frame2 = continuous.copyOfRange(hopSize, hopSize + frameSize)

        val r1 = refiner.refine(frame1, coarseFrequency, sampleRate)
        val result = refiner.refine(frame2, coarseFrequency, sampleRate)

        val coarseError = abs(coarseFrequency - trueFrequency)
        val refinedError = abs(result - trueFrequency)
        assertTrue(
            "First=$r1, Refined=$result (err=$refinedError), coarse=$coarseFrequency (err=$coarseError), true=$trueFrequency",
            refinedError < coarseError,
        )
    }

    @Test
    fun explicitSampleRateRefinesFallbackRecorderFrames() {
        val trueFrequency = 110.0
        val coarseFrequency = 111.0
        val sampleRate = 44_100
        val frameSize = 4096
        val hopSize = frameSize / 2
        val refiner = PhaseRefiner()

        val continuous = FloatArray(frameSize + hopSize) { i ->
            (sin(2.0 * PI * trueFrequency * i / sampleRate) * 0.5).toFloat()
        }
        val frame1 = continuous.copyOfRange(0, frameSize)
        val frame2 = continuous.copyOfRange(hopSize, hopSize + frameSize)

        refiner.refine(frame1, coarseFrequency, sampleRate)
        val result = refiner.refine(frame2, coarseFrequency, sampleRate)

        assertTrue(
            "Fallback-rate refinement should improve coarse=$coarseFrequency toward true=$trueFrequency, got $result",
            abs(result - trueFrequency) < abs(coarseFrequency - trueFrequency),
        )
    }

    @Test
    fun resetClearsState() {
        val refiner = PhaseRefiner()
        val frame = sineWave(440.0, 4096, 44_100)
        refiner.refine(frame, 440.0, 44_100)
        refiner.reset()
        val result = refiner.refine(frame, 440.0, 44_100)
        assertTrue(abs(result - 440.0) < 0.01)
    }

    @Test
    fun doesNotOverCorrectLargeJumps() {
        val refiner = PhaseRefiner()
        refiner.refine(sineWave(82.41, 4096, 44_100), 82.41, 44_100)
        val result = refiner.refine(sineWave(196.0, 4096, 44_100), 196.0, 44_100)
        assertTrue(abs(result - 196.0) < 0.01)
    }

    private fun sineWave(
        frequencyHz: Double,
        length: Int,
        sampleRate: Int,
    ): FloatArray = FloatArray(length) { i ->
        (sin(2.0 * PI * frequencyHz * i / sampleRate) * 0.5).toFloat()
    }
}
