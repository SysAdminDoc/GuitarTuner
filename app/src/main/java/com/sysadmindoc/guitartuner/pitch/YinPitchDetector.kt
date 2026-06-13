package com.sysadmindoc.guitartuner.pitch

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class PitchDetectorConfig(
    val minFrequencyHz: Double = 70.0,
    val maxFrequencyHz: Double = 450.0,
    val silenceRms: Double = 0.0015,
    val highNoiseRms: Double = 0.04,
    val clippingRatio: Double = 0.02,
    val yinThreshold: Double = 0.15,
    val minConfidence: Double = 0.70,
    val autocorrelationMinConfidence: Double = 0.45,
)

class YinPitchDetector(
    private val config: PitchDetectorConfig = PitchDetectorConfig(),
) {
    fun detect(samples: FloatArray, sampleRate: Int): PitchEstimate {
        if (samples.size < MinimumSamples) {
            return PitchEstimate.silence()
        }

        val rms = calculateRms(samples)
        if (rms < config.silenceRms) {
            return PitchEstimate.silence(rms)
        }
        if (clippingRatio(samples) > config.clippingRatio) {
            return PitchEstimate(
                frequencyHz = null,
                confidence = 0.0,
                rms = rms,
                clipping = true,
                status = SignalStatus.Clipping,
            )
        }

        val minTau = (sampleRate / config.maxFrequencyHz)
            .roundToInt()
            .coerceAtLeast(2)
        val maxTau = (sampleRate / config.minFrequencyHz)
            .roundToInt()
            .coerceAtMost(samples.size - 2)

        if (minTau >= maxTau) {
            return PitchEstimate(
                frequencyHz = null,
                confidence = 0.0,
                rms = rms,
                clipping = false,
                status = SignalStatus.Unstable,
            )
        }

        val centeredSamples = removeDcOffset(samples)
        val yin = differenceFunction(centeredSamples, maxTau)
        cumulativeMeanNormalizedDifference(yin)
        val tau = absoluteThreshold(yin, minTau, maxTau)
        if (tau == NoTau) {
            val fallback = autocorrelationFallback(centeredSamples, sampleRate, minTau, maxTau)
            if (fallback != null) {
                return PitchEstimate(
                    frequencyHz = fallback.frequencyHz,
                    confidence = fallback.confidence,
                    rms = rms,
                    clipping = false,
                    status = SignalStatus.Detected,
                )
            }
            return PitchEstimate(
                frequencyHz = null,
                confidence = 0.0,
                rms = rms,
                clipping = false,
                status = if (rms >= config.highNoiseRms) SignalStatus.HighNoise else SignalStatus.Unstable,
            )
        }

        val refinedTau = parabolicInterpolation(yin, tau)
        val frequency = sampleRate / refinedTau
        val confidence = (1.0 - yin[tau]).coerceIn(0.0, 1.0)
        if (confidence < config.minConfidence) {
            val fallback = autocorrelationFallback(centeredSamples, sampleRate, minTau, maxTau)
            if (fallback != null && fallback.confidence > confidence) {
                return PitchEstimate(
                    frequencyHz = fallback.frequencyHz,
                    confidence = fallback.confidence,
                    rms = rms,
                    clipping = false,
                    status = SignalStatus.Detected,
                )
            }
        }
        return PitchEstimate(
            frequencyHz = frequency,
            confidence = confidence,
            rms = rms,
            clipping = false,
            status = if (confidence >= config.minConfidence) SignalStatus.Detected else SignalStatus.Unstable,
        )
    }

    private fun calculateRms(samples: FloatArray): Double {
        var sum = 0.0
        for (sample in samples) {
            sum += sample * sample
        }
        return sqrt(sum / samples.size)
    }

    private fun clippingRatio(samples: FloatArray): Double {
        var clipped = 0
        for (sample in samples) {
            if (abs(sample) >= ClipThreshold) {
                clipped += 1
            }
        }
        return clipped.toDouble() / samples.size
    }

    private fun removeDcOffset(samples: FloatArray): DoubleArray {
        var mean = 0.0
        for (sample in samples) {
            mean += sample
        }
        mean /= samples.size

        return DoubleArray(samples.size) { index ->
            samples[index] - mean
        }
    }

    private fun differenceFunction(samples: DoubleArray, maxTau: Int): DoubleArray {
        val yin = DoubleArray(maxTau + 1)
        val windowSize = samples.size - maxTau
        for (tau in 1..maxTau) {
            var difference = 0.0
            for (index in 0 until windowSize) {
                val delta = samples[index] - samples[index + tau]
                difference += delta * delta
            }
            yin[tau] = difference
        }
        return yin
    }

    private fun cumulativeMeanNormalizedDifference(yin: DoubleArray) {
        yin[0] = 1.0
        var runningSum = 0.0
        for (tau in 1 until yin.size) {
            runningSum += yin[tau]
            yin[tau] = if (runningSum == 0.0) 1.0 else yin[tau] * tau / runningSum
        }
    }

    private fun absoluteThreshold(yin: DoubleArray, minTau: Int, maxTau: Int): Int {
        var tau = minTau
        while (tau <= maxTau) {
            if (yin[tau] < config.yinThreshold) {
                while (tau + 1 <= maxTau && yin[tau + 1] < yin[tau]) {
                    tau += 1
                }
                return tau
            }
            tau += 1
        }

        var bestTau = NoTau
        var bestValue = Double.MAX_VALUE
        for (candidate in minTau..maxTau) {
            if (yin[candidate] < bestValue) {
                bestTau = candidate
                bestValue = yin[candidate]
            }
        }
        return if ((1.0 - bestValue) >= config.minConfidence) bestTau else NoTau
    }

    private fun autocorrelationFallback(
        samples: DoubleArray,
        sampleRate: Int,
        minTau: Int,
        maxTau: Int,
    ): PitchCandidate? {
        val correlations = DoubleArray(maxTau + 1)
        var bestTau = NoTau
        var bestCorrelation = Double.NEGATIVE_INFINITY

        for (tau in minTau..maxTau) {
            val correlation = normalizedCorrelation(samples, tau)
            correlations[tau] = correlation
            if (correlation > bestCorrelation) {
                bestCorrelation = correlation
                bestTau = tau
            }
        }

        for (tau in (minTau + 1) until maxTau) {
            val correlation = correlations[tau]
            if (
                correlation >= config.autocorrelationMinConfidence &&
                correlation >= correlations[tau - 1] &&
                correlation >= correlations[tau + 1]
            ) {
                return PitchCandidate(
                    frequencyHz = sampleRate / parabolicInterpolation(correlations, tau),
                    confidence = correlation.coerceIn(0.0, 1.0),
                )
            }
        }

        return if (bestCorrelation >= config.autocorrelationMinConfidence && bestTau != NoTau) {
            PitchCandidate(
                frequencyHz = sampleRate / parabolicInterpolation(correlations, bestTau),
                confidence = bestCorrelation.coerceIn(0.0, 1.0),
            )
        } else {
            null
        }
    }

    private fun normalizedCorrelation(samples: DoubleArray, tau: Int): Double {
        val windowSize = samples.size - tau
        if (windowSize <= 0) return 0.0

        var sum = 0.0
        var firstEnergy = 0.0
        var secondEnergy = 0.0
        for (index in 0 until windowSize) {
            val first = samples[index]
            val second = samples[index + tau]
            sum += first * second
            firstEnergy += first * first
            secondEnergy += second * second
        }

        val denominator = sqrt(firstEnergy * secondEnergy)
        return if (denominator == 0.0) 0.0 else sum / denominator
    }

    private fun parabolicInterpolation(values: DoubleArray, tau: Int): Double {
        if (tau <= 1 || tau >= values.lastIndex) return tau.toDouble()

        val left = values[tau - 1]
        val center = values[tau]
        val right = values[tau + 1]
        val denominator = left - 2.0 * center + right
        if (abs(denominator) < 1.0e-12) return tau.toDouble()

        return tau + (left - right) / (2.0 * denominator)
    }

    private companion object {
        const val MinimumSamples = 512
        const val NoTau = -1
        const val ClipThreshold = 0.98f
    }
}

private data class PitchCandidate(
    val frequencyHz: Double,
    val confidence: Double,
)
