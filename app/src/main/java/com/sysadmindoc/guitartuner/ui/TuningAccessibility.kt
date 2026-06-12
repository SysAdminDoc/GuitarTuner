package com.sysadmindoc.guitartuner.ui

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

fun tuningMeterAccessibility(measurement: TuningMeasurement): TuningAccessibilityInfo {
    val cents = measurement.cents
    val progress = (cents ?: 0.0).coerceIn(-50.0, 50.0).toFloat()
    val target = measurement.target
    val stringLabel = target?.let { "string ${it.stringNumber}, ${it.name}, ${it.scientificPitch}" }

    val stateDescription = when (measurement.status) {
        TuningStatus.WaitingForSignal -> "Waiting for a detected guitar string"
        TuningStatus.SignalClipping -> "Input is clipping; play softer"
        TuningStatus.NoStringDetected -> "No guitar string detected"
        TuningStatus.InTune -> if (stringLabel == null) {
            "In tune"
        } else {
            "$stringLabel, in tune"
        }
        TuningStatus.TuneUp,
        TuningStatus.TuneDown,
        -> {
            val centsBucket = bucketCents(cents ?: 0.0)
            val direction = if (measurement.status == TuningStatus.TuneUp) "tune up" else "tune down"
            val flatSharp = if ((cents ?: 0.0) < 0.0) "flat" else "sharp"
            val prefix = if (stringLabel == null) "" else "$stringLabel, "
            "$prefix$direction, ${formatCents(abs(centsBucket.toDouble()))} cents $flatSharp"
        }
    }

    return TuningAccessibilityInfo(
        contentDescription = "Guitar tuning meter",
        stateDescription = stateDescription,
        progressCents = progress,
    )
}

private fun bucketCents(cents: Double): Int =
    (cents / CentsBucketSize).roundToInt() * CentsBucketSize

private fun formatCents(cents: Double): String =
    String.format(Locale.US, "%.0f", cents)

private const val CentsBucketSize = 5
