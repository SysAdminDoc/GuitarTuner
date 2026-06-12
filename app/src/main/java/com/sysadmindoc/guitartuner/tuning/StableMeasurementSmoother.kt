package com.sysadmindoc.guitartuner.tuning

import kotlin.math.abs

data class StableMeasurementSmootherConfig(
    val attackFramesToSkip: Int = 2,
    val stableWindowSize: Int = 4,
    val maxInterFrameCents: Double = 25.0,
    val inTuneCents: Double = 5.0,
)

class StableMeasurementSmoother(
    private val config: StableMeasurementSmootherConfig = StableMeasurementSmootherConfig(),
) {
    private val stableFrames = ArrayDeque<TuningMeasurement>()
    private var activeTarget: GuitarString? = null
    private var skippedAttackFrames = 0
    private var lastAcceptedCents: Double? = null

    @Synchronized
    fun apply(measurement: TuningMeasurement): TuningMeasurement {
        if (!measurement.status.isStableTuningStatus()) {
            reset()
            return measurement
        }

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
            resetFor(target)
            return skipAttackFrame(measurement)
        }
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
    }

    private fun resetFor(target: GuitarString) {
        stableFrames.clear()
        activeTarget = target
        skippedAttackFrames = 0
        lastAcceptedCents = null
    }

    private fun skipAttackFrame(measurement: TuningMeasurement): TuningMeasurement {
        skippedAttackFrames = 1
        lastAcceptedCents = measurement.cents
        return measurement
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
        -> true

        TuningStatus.WaitingForSignal,
        TuningStatus.SignalClipping,
        TuningStatus.HighNoise,
        TuningStatus.NoStringDetected,
        -> false
    }
}
