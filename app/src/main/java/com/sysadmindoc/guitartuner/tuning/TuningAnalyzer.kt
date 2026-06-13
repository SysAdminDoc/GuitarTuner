package com.sysadmindoc.guitartuner.tuning

import com.sysadmindoc.guitartuner.pitch.PitchEstimate
import com.sysadmindoc.guitartuner.pitch.SignalStatus
import kotlin.math.abs
import kotlin.math.ln

class TuningAnalyzer(
    private val strings: List<GuitarString>,
    private val targetSelection: TuningTargetSelection = TuningTargetSelection.auto(),
    private val inTuneCents: Double = 5.0,
    private val maxAutoDetectCents: Double = 250.0,
    private val maxGuidedDetectCents: Double = 300.0,
    private val octaveCorrectionMinImprovementCents: Double = 80.0,
    private val overshootWarningCents: Double = 300.0,
    private val overshootCeilingCents: Double = 450.0,
    private val a4Hz: Double = 440.0,
) {
    fun analyze(estimate: PitchEstimate): TuningMeasurement {
        val frequency = estimate.frequencyHz
        return when {
            estimate.status == SignalStatus.Clipping -> TuningMeasurement.signalClipping()
            estimate.status == SignalStatus.HighNoise -> TuningMeasurement.highNoise()
            estimate.status == SignalStatus.Silence -> TuningMeasurement.waiting()
            frequency == null || estimate.status == SignalStatus.Unstable -> TuningMeasurement.noStringDetected()
            targetSelection.mode == TuningMode.Chromatic -> analyzeChromaticFrequency(frequency, estimate.confidence)
            else -> analyzeFrequency(frequency, estimate.confidence)
        }
    }

    private fun analyzeChromaticFrequency(frequencyHz: Double, confidence: Double): TuningMeasurement {
        val chromatic = ChromaticNote.resolve(frequencyHz, a4Hz)
        val target = GuitarString(
            stringNumber = 0,
            name = chromatic.noteName,
            scientificPitch = chromatic.scientificPitch,
            frequencyHz = chromatic.targetFrequencyHz,
        )
        val direction = when {
            abs(chromatic.cents) <= inTuneCents -> TuningDirection.InTune
            chromatic.cents < 0.0 -> TuningDirection.TuneUp
            else -> TuningDirection.TuneDown
        }
        return TuningMeasurement.detected(
            target = target,
            frequencyHz = frequencyHz,
            cents = chromatic.cents,
            confidence = confidence,
            direction = direction,
        )
    }

    private fun analyzeFrequency(frequencyHz: Double, confidence: Double): TuningMeasurement {
        val candidate = resolveSecondHarmonicCandidate(frequencyHz)

        if (candidate.cents > overshootWarningCents && candidate.cents < overshootCeilingCents) {
            return TuningMeasurement.overshoot(
                target = candidate.string,
                frequencyHz = candidate.frequencyHz,
                cents = candidate.cents,
                confidence = confidence,
            )
        }

        val maxDetectCents = when (targetSelection.mode) {
            TuningMode.Auto, TuningMode.Chromatic -> maxAutoDetectCents
            TuningMode.Guided -> maxGuidedDetectCents
        }
        if (abs(candidate.cents) > maxDetectCents) {
            return TuningMeasurement.noStringDetected(frequencyHz = frequencyHz, confidence = confidence)
        }

        val direction = when {
            abs(candidate.cents) <= inTuneCents -> TuningDirection.InTune
            candidate.cents < 0.0 -> TuningDirection.TuneUp
            else -> TuningDirection.TuneDown
        }

        return TuningMeasurement.detected(
            target = candidate.string,
            frequencyHz = candidate.frequencyHz,
            cents = candidate.cents,
            confidence = confidence,
            direction = direction,
        )
    }

    private fun resolveSecondHarmonicCandidate(frequencyHz: Double): FrequencyCandidate {
        val rawCandidate = candidateFor(frequencyHz)
        val halfFrequency = frequencyHz / 2.0
        val minimumSupportedFrequency = strings.minOf { it.frequencyHz } * 0.75
        if (halfFrequency < minimumSupportedFrequency) {
            return rawCandidate
        }

        val halfCandidate = candidateFor(halfFrequency)
        val rawDistance = abs(rawCandidate.cents)
        val halfDistance = abs(halfCandidate.cents)
        return if (rawDistance - halfDistance >= octaveCorrectionMinImprovementCents) {
            halfCandidate
        } else {
            rawCandidate
        }
    }

    private fun candidateFor(frequencyHz: Double): FrequencyCandidate {
        val candidateStrings = targetSelection.selectedStrings(strings)
        val nearest = candidateStrings.minBy { string ->
            abs(centsBetween(frequencyHz, string.frequencyHz))
        }
        return FrequencyCandidate(
            frequencyHz = frequencyHz,
            string = nearest,
            cents = centsBetween(frequencyHz, nearest.frequencyHz),
        )
    }

    private fun centsBetween(frequencyHz: Double, targetHz: Double): Double =
        CentsPerOctave * ln(frequencyHz / targetHz) / ln(2.0)

    private companion object {
        const val CentsPerOctave = 1200.0
    }
}

private data class FrequencyCandidate(
    val frequencyHz: Double,
    val string: GuitarString,
    val cents: Double,
)

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

        fun overshoot(
            target: GuitarString,
            frequencyHz: Double,
            cents: Double,
            confidence: Double,
        ): TuningMeasurement = TuningMeasurement(
            status = TuningStatus.Overshoot,
            target = target,
            frequencyHz = frequencyHz,
            cents = cents,
            confidence = confidence,
            direction = TuningDirection.TuneDown,
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
    Overshoot,
}

enum class TuningDirection {
    TuneUp,
    TuneDown,
    InTune,
}
