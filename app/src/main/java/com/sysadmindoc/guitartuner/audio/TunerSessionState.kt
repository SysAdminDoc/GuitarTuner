package com.sysadmindoc.guitartuner.audio

import com.sysadmindoc.guitartuner.pitch.PitchEstimate
import com.sysadmindoc.guitartuner.tuning.PitchResult
import com.sysadmindoc.guitartuner.tuning.TuningMeasurement
import kotlin.math.max
import kotlin.math.sqrt

data class AudioInputLevel(
    val rms: Double = 0.0,
    val peak: Double = 0.0,
    val readCount: Long = 0,
    val sourceLabel: String? = null,
    val sampleRateHz: Int? = null,
    val lastReadSize: Int = 0,
) {
    val isEffectivelySilent: Boolean
        get() = readCount >= SilentReadCount && rms < SilentRms && peak < SilentPeak

    companion object {
        fun fromPcmRead(
            samples: ShortArray,
            length: Int,
            previous: AudioInputLevel = AudioInputLevel(),
            sourceLabel: String? = previous.sourceLabel,
            sampleRateHz: Int? = previous.sampleRateHz,
        ): AudioInputLevel {
            require(length in 0..samples.size) { "Read length must fit inside the PCM buffer." }
            if (length == 0) {
                return previous.copy(sourceLabel = sourceLabel, sampleRateHz = sampleRateHz)
            }

            var peakSample = 0
            var sumSquares = 0.0
            for (index in 0 until length) {
                val sample = samples[index].toInt()
                val absSample = kotlin.math.abs(sample)
                peakSample = max(peakSample, absSample)
                val normalized = sample / PcmFullScale
                sumSquares += normalized * normalized
            }

            val instantRms = sqrt(sumSquares / length)
            val instantPeak = peakSample / PcmFullScale
            val smoothing = if (previous.readCount == 0L) 1.0 else RmsSmoothing
            val smoothedRms = previous.rms * (1.0 - smoothing) + instantRms * smoothing
            val decayedPeak = max(instantPeak, previous.peak * PeakDecay)

            return AudioInputLevel(
                rms = smoothedRms,
                peak = decayedPeak,
                readCount = previous.readCount + 1,
                sourceLabel = sourceLabel,
                sampleRateHz = sampleRateHz,
                lastReadSize = length,
            )
        }

        private const val PcmFullScale = 32768.0
        private const val RmsSmoothing = 0.25
        private const val PeakDecay = 0.82
        private const val SilentReadCount = 20L
        private const val SilentRms = 0.0002
        private const val SilentPeak = 0.0005
    }
}

data class TunerSessionState(
    val isListening: Boolean = false,
    val isFrozen: Boolean = false,
    val pitchEstimate: PitchEstimate = PitchEstimate.silence(),
    val measurement: TuningMeasurement = TuningMeasurement.waiting(),
    val inputLevel: AudioInputLevel = AudioInputLevel(),
    val errorMessage: String? = null,
) {
    val pitchResult: PitchResult
        get() = PitchResult.from(pitchEstimate, measurement)
}
