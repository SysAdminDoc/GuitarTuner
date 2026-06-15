package com.sysadmindoc.guitartuner.ui

import com.sysadmindoc.guitartuner.R
import com.sysadmindoc.guitartuner.tuning.TuningMeasurement
import com.sysadmindoc.guitartuner.tuning.TuningStatus
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

data class TuningAccessibilityInfo(
    val contentDescription: String,
    val stateDescription: String,
    val progressCents: Float,
)

private val noArgs = emptyArray<Any>()

fun tuningMeterAccessibility(
    measurement: TuningMeasurement,
    getString: (Int, Array<out Any>) -> String,
): TuningAccessibilityInfo {
    val cents = measurement.cents
    val progress = (cents ?: 0.0).coerceIn(-50.0, 50.0).toFloat()
    val target = measurement.target
    val stringLabel = target?.let {
        getString(R.string.a11y_string_label, arrayOf<Any>(it.stringNumber, it.name, it.scientificPitch))
    }

    val stateDescription = when (measurement.status) {
        TuningStatus.WaitingForSignal -> getString(R.string.a11y_state_waiting, noArgs)
        TuningStatus.SignalClipping -> getString(R.string.a11y_state_clipping, noArgs)
        TuningStatus.HighNoise -> getString(R.string.a11y_state_high_noise, noArgs)
        TuningStatus.NoStringDetected -> getString(R.string.a11y_state_no_string, noArgs)
        TuningStatus.InTune -> if (stringLabel == null) {
            getString(R.string.a11y_in_tune, noArgs)
        } else {
            getString(R.string.a11y_string_in_tune, arrayOf<Any>(stringLabel))
        }
        TuningStatus.Overshoot -> if (stringLabel == null) {
            getString(R.string.a11y_tune_down_risk, noArgs)
        } else {
            getString(R.string.a11y_string_tune_down_risk, arrayOf<Any>(stringLabel))
        }
        TuningStatus.TuneUp,
        TuningStatus.TuneDown,
        -> {
            val centsBucket = bucketCents(cents ?: 0.0)
            val direction = if (measurement.status == TuningStatus.TuneUp) {
                getString(R.string.a11y_direction_tune_up, noArgs)
            } else {
                getString(R.string.a11y_direction_tune_down, noArgs)
            }
            val flatSharp = if ((cents ?: 0.0) < 0.0) {
                getString(R.string.a11y_pitch_flat, noArgs)
            } else {
                getString(R.string.a11y_pitch_sharp, noArgs)
            }
            val formattedCents = formatCents(abs(centsBucket.toDouble()))
            if (stringLabel == null) {
                getString(R.string.a11y_tune_direction, arrayOf<Any>(direction, formattedCents, flatSharp))
            } else {
                getString(R.string.a11y_string_tune_direction, arrayOf<Any>(stringLabel, direction, formattedCents, flatSharp))
            }
        }
    }

    return TuningAccessibilityInfo(
        contentDescription = getString(R.string.a11y_meter_description, noArgs),
        stateDescription = stateDescription,
        progressCents = progress,
    )
}

private fun bucketCents(cents: Double): Int =
    (cents / CentsBucketSize).roundToInt() * CentsBucketSize

private fun formatCents(cents: Double): String =
    String.format(Locale.US, "%.0f", cents)

private const val CentsBucketSize = 5
