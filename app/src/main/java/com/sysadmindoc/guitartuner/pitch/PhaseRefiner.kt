package com.sysadmindoc.guitartuner.pitch

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class PhaseRefiner(
    private val sampleRate: Int = 48_000,
) {
    private var previousPhase: Double = 0.0
    private var hasPrevious: Boolean = false
    private var previousFrequency: Double = 0.0

    fun refine(
        samples: FloatArray,
        coarseFrequencyHz: Double,
        sampleRate: Int = this.sampleRate,
    ): Double {
        val phase = goertzelPhase(samples, coarseFrequencyHz, sampleRate)

        if (!hasPrevious || abs(coarseFrequencyHz - previousFrequency) > FrequencyJumpThreshold) {
            previousPhase = phase
            previousFrequency = coarseFrequencyHz
            hasPrevious = true
            return coarseFrequencyHz
        }

        val hopSamples = samples.size / 2.0
        val expectedAdvance = 2.0 * PI * coarseFrequencyHz * hopSamples / sampleRate
        val rawAdvance = phase - previousPhase
        val deviation = wrapToPi(rawAdvance - expectedAdvance)
        val correction = deviation * sampleRate / (2.0 * PI * hopSamples)

        previousPhase = phase
        previousFrequency = coarseFrequencyHz

        val refined = coarseFrequencyHz + correction
        return if (refined > 0 && abs(correction) < MaxCorrectionHz) refined else coarseFrequencyHz
    }

    fun reset() {
        hasPrevious = false
        previousPhase = 0.0
        previousFrequency = 0.0
    }

    private companion object {
        const val FrequencyJumpThreshold = 10.0
        const val MaxCorrectionHz = 5.0

        fun goertzelPhase(samples: FloatArray, frequencyHz: Double, sampleRate: Int): Double {
            val omega = 2.0 * PI * frequencyHz / sampleRate
            var real = 0.0
            var imag = 0.0
            for (i in samples.indices) {
                real += samples[i] * cos(omega * i)
                imag -= samples[i] * sin(omega * i)
            }
            return atan2(imag, real)
        }

        fun wrapToPi(angle: Double): Double {
            var a = angle
            while (a > PI) a -= 2.0 * PI
            while (a < -PI) a += 2.0 * PI
            return a
        }
    }
}
