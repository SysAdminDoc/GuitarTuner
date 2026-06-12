package com.sysadmindoc.guitartuner.tuning

import com.sysadmindoc.guitartuner.pitch.PitchEstimate
import com.sysadmindoc.guitartuner.pitch.SignalStatus
import kotlin.math.abs
import kotlin.math.ln

class TuningAnalyzer(
    private val strings: List<GuitarString>,
    private val inTuneCents: Double = 5.0,
    private val maxAutoDetectCents: Double = 250.0,
) {
    fun analyze(estimate: PitchEstimate): TuningMeasurement {
        val frequency = estimate.frequencyHz
        return when {
            estimate.status == SignalStatus.Clipping -> TuningMeasurement.signalClipping()
            estimate.status == SignalStatus.HighNoise -> TuningMeasurement.highNoise()
            estimate.status == SignalStatus.Silence -> TuningMeasurement.waiting()
            frequency == null || estimate.status == SignalStatus.Unstable -> TuningMeasurement.noStringDetected()
            else -> analyzeFrequency(frequency, estimate.confidence)
        }
    }

    private fun analyzeFrequency(frequencyHz: Double, confidence: Double): TuningMeasurement {
        val nearest = strings.minBy { string ->
            abs(centsBetween(frequencyHz, string.frequencyHz))
        }
        val cents = centsBetween(frequencyHz, nearest.frequencyHz)
        if (abs(cents) > maxAutoDetectCents) {
            return TuningMeasurement.noStringDetected(frequencyHz = frequencyHz, confidence = confidence)
        }

        val direction = when {
            abs(cents) <= inTuneCents -> TuningDirection.InTune
            cents < 0.0 -> TuningDirection.TuneUp
            else -> TuningDirection.TuneDown
        }

        return TuningMeasurement.detected(
            target = nearest,
            frequencyHz = frequencyHz,
            cents = cents,
            confidence = confidence,
            direction = direction,
        )
    }

    private fun centsBetween(frequencyHz: Double, targetHz: Double): Double =
        CentsPerOctave * ln(frequencyHz / targetHz) / ln(2.0)

    private companion object {
        const val CentsPerOctave = 1200.0
    }
}

data class TuningMeasurement(
    val status: TuningStatus,
    val target: GuitarString? = null,
    val frequencyHz: Double? = null,
    val cents: Double? = null,
    val confidence: Double = 0.0,
    val direction: TuningDirection? = null,
) {
    companion object {
        fun waiting(): TuningMeasurement = TuningMeasurement(status = TuningStatus.WaitingForSignal)

        fun signalClipping(): TuningMeasurement = TuningMeasurement(status = TuningStatus.SignalClipping)

        fun highNoise(): TuningMeasurement = TuningMeasurement(status = TuningStatus.HighNoise)

        fun noStringDetected(
            frequencyHz: Double? = null,
            confidence: Double = 0.0,
        ): TuningMeasurement = TuningMeasurement(
            status = TuningStatus.NoStringDetected,
            frequencyHz = frequencyHz,
            confidence = confidence,
        )

        fun detected(
            target: GuitarString,
            frequencyHz: Double,
            cents: Double,
            confidence: Double,
            direction: TuningDirection,
        ): TuningMeasurement = TuningMeasurement(
            status = when (direction) {
                TuningDirection.InTune -> TuningStatus.InTune
                TuningDirection.TuneUp -> TuningStatus.TuneUp
                TuningDirection.TuneDown -> TuningStatus.TuneDown
            },
            target = target,
            frequencyHz = frequencyHz,
            cents = cents,
            confidence = confidence,
            direction = direction,
        )
    }
}

enum class TuningStatus {
    WaitingForSignal,
    SignalClipping,
    HighNoise,
    NoStringDetected,
    TuneUp,
    TuneDown,
    InTune,
}

enum class TuningDirection {
    TuneUp,
    TuneDown,
    InTune,
}
