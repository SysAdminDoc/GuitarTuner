package com.sysadmindoc.guitartuner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sysadmindoc.guitartuner.R
import com.sysadmindoc.guitartuner.audio.TunerSessionState
import com.sysadmindoc.guitartuner.settings.PegTurnDirection
import com.sysadmindoc.guitartuner.settings.opposite
import com.sysadmindoc.guitartuner.tuning.GuitarString
import com.sysadmindoc.guitartuner.tuning.TuningDirection
import com.sysadmindoc.guitartuner.tuning.TuningStatus
import java.util.Locale

@Composable
internal fun FrequencyReadout(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
    permissionPermanentlyDenied: Boolean,
    pegTurnDirections: Map<Int, PegTurnDirection>,
    guidedTarget: GuitarString?,
) {
    val measurement = state.measurement
    val pitchResult = state.pitchResult
    val cents = pitchResult.centsOffset
    val frequency = pitchResult.frequencyHz
    val confidence = pitchResult.confidence
    val inputLevel = state.inputLevel
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = guidanceLabel(
                state = state,
                hasAudioPermission = hasAudioPermission,
                permissionPermanentlyDenied = permissionPermanentlyDenied,
                turnDirection = measurement.turnDirection(pegTurnDirections),
                guidedTarget = guidedTarget,
            ),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = statusColor(measurement.status),
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        ReadoutMetrics(
            frequency = frequency,
            cents = cents,
            confidence = confidence,
            isListening = state.isListening,
            inputRms = inputLevel.rms,
        )
        InputLevelStrip(
            rms = inputLevel.rms,
            peak = inputLevel.peak,
            isListening = state.isListening,
        )
        if (state.isListening) {
            val sourceText = if (inputLevel.sourceLabel != null && inputLevel.sampleRateHz != null) {
                stringResource(
                    R.string.input_source_value,
                    inputLevel.sourceLabel,
                    inputLevel.sampleRateHz,
                )
            } else {
                " "
            }
            Text(
                text = sourceText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ReadoutMetrics(
    frequency: Double?,
    cents: Double?,
    confidence: Double,
    isListening: Boolean,
    inputRms: Double,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ReadoutMetric(
            label = stringResource(R.string.metric_frequency),
            value = frequency?.let { stringResource(R.string.frequency_value, formatOneDecimal(it)) }
                ?: stringResource(R.string.frequency_placeholder),
            modifier = Modifier.weight(1f),
        )
        ReadoutMetric(
            label = stringResource(R.string.metric_cents),
            value = cents?.let { stringResource(R.string.cents_value, formatSignedOneDecimal(it)) }
                ?: stringResource(R.string.cents_placeholder),
            modifier = Modifier.weight(1f),
        )
        ReadoutMetric(
            label = stringResource(R.string.metric_confidence),
            value = frequency?.let { formatPercent(confidence) } ?: stringResource(R.string.value_placeholder),
            modifier = Modifier.weight(1f),
        )
        ReadoutMetric(
            label = stringResource(R.string.metric_input),
            value = if (isListening) {
                formatLevelPercent(inputRms)
            } else {
                stringResource(R.string.value_placeholder)
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ReadoutMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun InputLevelStrip(
    rms: Double,
    peak: Double,
    isListening: Boolean,
) {
    val trackColor = MaterialTheme.colorScheme.outlineVariant
    val rmsColor = MaterialTheme.colorScheme.primary
    val peakColor = MaterialTheme.colorScheme.tertiary
    val inputDescription = stringResource(R.string.metric_input)
    val inputState = if (isListening) {
        stringResource(R.string.input_level_value, formatLevelPercent(rms), formatLevelPercent(peak))
    } else {
        stringResource(R.string.input_level_placeholder)
    }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .semantics {
                contentDescription = inputDescription
                stateDescription = inputState
                progressBarRangeInfo = ProgressBarRangeInfo(rms.coerceIn(0.0, 1.0).toFloat(), 0f..1f)
            },
    ) {
        val trackHeight = 4.dp.toPx()
        val y = (size.height - trackHeight) / 2f
        drawRect(
            color = trackColor,
            topLeft = Offset(0f, y),
            size = Size(size.width, trackHeight),
        )
        if (isListening) {
            drawRect(
                color = rmsColor,
                topLeft = Offset(0f, y),
                size = Size(size.width * rms.coerceIn(0.0, 1.0).toFloat(), trackHeight),
            )
            drawRect(
                color = peakColor,
                topLeft = Offset(size.width * peak.coerceIn(0.0, 1.0).toFloat(), 0f),
                size = Size(3.dp.toPx(), size.height),
            )
        }
    }
}

@Composable
internal fun TrustSignal() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PanelShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(PanelBorderWidth, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Text(
            text = stringResource(R.string.privacy_inline_note),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun statusColor(status: TuningStatus): Color = when (status) {
    TuningStatus.InTune -> MaterialTheme.colorScheme.primary
    TuningStatus.TuneUp,
    TuningStatus.TuneDown,
    TuningStatus.SignalClipping,
    TuningStatus.HighNoise,
    -> MaterialTheme.colorScheme.tertiary

    TuningStatus.Overshoot -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun guidanceLabel(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
    permissionPermanentlyDenied: Boolean,
    turnDirection: PegTurnDirection?,
    guidedTarget: GuitarString?,
): String {
    if (!hasAudioPermission && permissionPermanentlyDenied) return stringResource(R.string.guidance_open_settings)
    if (!hasAudioPermission) return stringResource(R.string.guidance_allow_mic)
    if (!state.isListening) return stringResource(R.string.guidance_start_listening)
    if (state.micStolen) {
        return stringResource(R.string.guidance_mic_stolen)
    }
    if (
        state.inputLevel.isEffectivelySilent &&
        state.measurement.status == TuningStatus.WaitingForSignal
    ) {
        return stringResource(R.string.guidance_check_mic_input)
    }

    val status = state.measurement.status
    val base = when (status) {
        TuningStatus.WaitingForSignal -> guidedTarget?.let {
            stringResource(R.string.guidance_strum_guided, it.walkthroughLabel())
        } ?: stringResource(R.string.guidance_strum_open_string)
        TuningStatus.SignalClipping -> stringResource(R.string.guidance_play_softer)
        TuningStatus.HighNoise -> stringResource(R.string.guidance_reduce_noise)
        TuningStatus.NoStringDetected -> stringResource(R.string.guidance_try_one_string)
        TuningStatus.TuneUp -> stringResource(R.string.guidance_tune_up)
        TuningStatus.TuneDown -> stringResource(R.string.guidance_tune_down)
        TuningStatus.InTune -> stringResource(R.string.guidance_in_tune)
        TuningStatus.Overshoot -> stringResource(R.string.guidance_overshoot)
    }
    return if (
        turnDirection == null ||
        status != TuningStatus.TuneUp && status != TuningStatus.TuneDown && status != TuningStatus.Overshoot
    ) {
        base
    } else {
        val turnLabel = when (turnDirection) {
            PegTurnDirection.Left -> stringResource(R.string.turn_left)
            PegTurnDirection.Right -> stringResource(R.string.turn_right)
        }
        stringResource(R.string.guidance_with_turn, base, turnLabel)
    }
}

private fun com.sysadmindoc.guitartuner.tuning.TuningMeasurement.turnDirection(
    pegTurnDirections: Map<Int, PegTurnDirection>,
): PegTurnDirection? {
    val tuneUpDirection = target?.let { pegTurnDirections[it.stringNumber] } ?: return null
    return when (direction) {
        TuningDirection.TuneUp -> tuneUpDirection
        TuningDirection.TuneDown -> tuneUpDirection.opposite()
        TuningDirection.InTune,
        null,
        -> null
    }
}

internal fun formatOneDecimal(value: Double): String =
    String.format(Locale.US, "%.1f", value)

internal fun formatSignedOneDecimal(value: Double): String =
    String.format(Locale.US, "%+.1f", value)

internal fun formatA4Hz(value: Double): String =
    if (value == kotlin.math.floor(value)) String.format(Locale.US, "%.0f Hz", value)
    else String.format(Locale.US, "%.1f Hz", value)

internal fun formatWholeNumber(value: Double): String =
    String.format(Locale.US, "%.0f", value)

internal fun formatFourDecimals(value: Double): String =
    String.format(Locale.US, "%.4f", value)

internal fun formatPercent(value: Double): String =
    String.format(Locale.US, "%.0f%%", value.coerceIn(0.0, 1.0) * 100.0)

internal fun formatLevelPercent(value: Double): String =
    String.format(Locale.US, "%.1f%%", value.coerceIn(0.0, 1.0) * 100.0)
