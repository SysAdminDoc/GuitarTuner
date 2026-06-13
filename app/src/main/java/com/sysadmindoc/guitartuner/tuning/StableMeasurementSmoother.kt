package com.sysadmindoc.guitartuner.tuning

import kotlin.math.abs
import kotlin.math.ln

data class StableMeasurementSmootherConfig(
    val attackFramesToSkip: Int = 2,
    val stableWindowSize: Int = 4,
    val maxInterFrameCents: Double = 25.0,
    val inTuneCents: Double = 5.0,
    val maxHoldoverFrames: Int = 2,
    val octaveFlipThreshold: Int = 3,
)

class StableMeasurementSmoother(
    private val config: StableMeasurementSmootherConfig = StableMeasurementSmootherConfig(),
) {
    private val stableFrames = ArrayDeque<TuningMeasurement>()
    private var activeTarget: GuitarString? = null
    private var skippedAttackFrames = 0
    private var lastAcceptedCents: Double? = null
    private var holdoverFrames = 0
    private var octaveFlipCandidate: GuitarString? = null
    private var octaveFlipCount = 0

    @Synchronized
    fun apply(measurement: TuningMeasurement): TuningMeasurement {
        if (!measurement.status.isStableTuningStatus()) {
            if (activeTarget != null && holdoverFrames < config.maxHoldoverFrames) {
                holdoverFrames++
                return if (stableFrames.isNotEmpty()) stableFrames.last() else measurement
            }
            reset()
            return measurement
        }

        holdoverFrames = 0

        val target = measurement.target
        val cents = measurement.cents
        val frequencyHz = measurement.frequencyHz
        if (target == null || cents == null || frequencyHz == null) {
            reset()
            return measurement
        }

        val previousTarget = activeTarget
        val previousCents = lastAcceptedCents
        if (previousTarget != null && previousTarget != target) {
            if (isOctaveRelated(previousTarget, target)) {
                octaveFlipCount = if (octaveFlipCandidate == target) octaveFlipCount + 1 else 1
                octaveFlipCandidate = target
                if (octaveFlipCount < config.octaveFlipThreshold) {
                    lastAcceptedCents = cents
                    return if (stableFrames.isNotEmpty()) stableFrames.last() else measurement
                }
            }
            octaveFlipCandidate = null
            octaveFlipCount = 0
            resetFor(target)
            return skipAttackFrame(measurement)
        }

        octaveFlipCandidate = null
        octaveFlipCount = 0

        if (previousCents != null && abs(cents - previousCents) > config.maxInterFrameCents) {
            resetFor(target)
            return skipAttackFrame(measurement)
        }

        if (activeTarget == null) {
            resetFor(target)
        }
        lastAcceptedCents = cents

        if (skippedAttackFrames < config.attackFramesToSkip) {
            skippedAttackFrames += 1
            return measurement
        }

        stableFrames.addLast(measurement)
        while (stableFrames.size > config.stableWindowSize) {
            stableFrames.removeFirst()
        }

        return averageStableFrames()
    }

    @Synchronized
    fun reset() {
        stableFrames.clear()
        activeTarget = null
        skippedAttackFrames = 0
        lastAcceptedCents = null
        holdoverFrames = 0
        octaveFlipCandidate = null
        octaveFlipCount = 0
    }

    private fun resetFor(target: GuitarString) {
        stableFrames.clear()
        activeTarget = target
        skippedAttackFrames = 0
        lastAcceptedCents = null
        holdoverFrames = 0
    }

    private fun skipAttackFrame(measurement: TuningMeasurement): TuningMeasurement {
        skippedAttackFrames = 1
        lastAcceptedCents = measurement.cents
        return measurement
    }

    private fun isOctaveRelated(a: GuitarString, b: GuitarString): Boolean {
        val ratio = if (a.frequencyHz > b.frequencyHz) {
            a.frequencyHz / b.frequencyHz
        } else {
            b.frequencyHz / a.frequencyHz
        }
        val centsDifference = abs(CentsPerOctave * ln(ratio) / Ln2)
        val distanceFromOctave = abs(centsDifference - CentsPerOctave)
        return distanceFromOctave < OctaveProximityCents
    }

    private fun averageStableFrames(): TuningMeasurement {
        val frames = stableFrames.toList()
        val target = activeTarget ?: return frames.last()
        val frequencyHz = frames.mapNotNull { it.frequencyHz }.average()
        val cents = frames.mapNotNull { it.cents }.average()
        val confidence = frames.map { it.confidence }.average()
        val direction = when {
            abs(cents) <= config.inTuneCents -> TuningDirection.InTune
            cents < 0.0 -> TuningDirection.TuneUp
            else -> TuningDirection.TuneDown
        }

        return TuningMeasurement.detected(
            target = target,
            frequencyHz = frequencyHz,
            cents = cents,
            confidence = confidence,
            direction = direction,
        )
    }

    private fun TuningStatus.isStableTuningStatus(): Boolean = when (this) {
        TuningStatus.TuneUp,
        TuningStatus.TuneDown,
        TuningStatus.InTune,
        TuningStatus.Overshoot,
        -> true

        TuningStatus.WaitingForSignal,
        TuningStatus.SignalClipping,
        TuningStatus.HighNoise,
        TuningStatus.NoStringDetected,
        -> false
    }

    private companion object {
        const val CentsPerOctave = 1200.0
        val Ln2 = ln(2.0)
        const val OctaveProximityCents = 50.0
    }
}
