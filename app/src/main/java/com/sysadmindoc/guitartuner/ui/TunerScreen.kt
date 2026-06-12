package com.sysadmindoc.guitartuner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sysadmindoc.guitartuner.audio.TunerSessionState
import com.sysadmindoc.guitartuner.tuning.TuningStatus
import java.util.Locale
import kotlin.math.abs

@Composable
fun TunerScreen(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
    onPrimaryAction: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "GuitarTuner",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = statusText(state, hasAudioPermission),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    TargetString(state)
                    CentsMeter(state)
                    FrequencyReadout(state)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onPrimaryAction,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(primaryActionLabel(state, hasAudioPermission))
                }
                OutlinedButton(
                    onClick = onStop,
                    enabled = state.isListening,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Stop")
                }
            }
        }
    }
}

@Composable
private fun TargetString(state: TunerSessionState) {
    val measurement = state.measurement
    val target = measurement.target
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = target?.name ?: "--",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = target?.displayName ?: "Waiting for string",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CentsMeter(state: TunerSessionState) {
    val cents = state.measurement.cents
    val boundedCents = (cents ?: 0.0).coerceIn(-50.0, 50.0)
    val semanticCents = boundedCents.toFloat()
    val meterDescription = when {
        cents == null -> "Tuning meter waiting for a detected guitar string"
        abs(cents) <= 5.0 -> "String is in tune"
        cents < 0.0 -> "String is flat by ${formatOneDecimal(abs(cents))} cents"
        else -> "String is sharp by ${formatOneDecimal(abs(cents))} cents"
    }
    val trackColor = MaterialTheme.colorScheme.outline
    val centerColor = MaterialTheme.colorScheme.primary
    val markerColor = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .semantics {
                contentDescription = meterDescription
                progressBarRangeInfo = ProgressBarRangeInfo(semanticCents, -50f..50f)
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val y = size.height / 2f
            val left = 16.dp.toPx()
            val right = size.width - 16.dp.toPx()
            val centerX = size.width / 2f
            drawLine(
                color = trackColor,
                start = Offset(left, y),
                end = Offset(right, y),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = centerColor,
                start = Offset(centerX, y - 28.dp.toPx()),
                end = Offset(centerX, y + 28.dp.toPx()),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round,
            )
            val markerX = centerX + (boundedCents / 50.0).toFloat() * ((right - left) / 2f)
            drawCircle(
                color = markerColor,
                radius = 12.dp.toPx(),
                center = Offset(markerX, y),
            )
        }
    }
}

@Composable
private fun FrequencyReadout(state: TunerSessionState) {
    val measurement = state.measurement
    val cents = measurement.cents
    val frequency = measurement.frequencyHz
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = guidanceLabel(measurement.status),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = when (measurement.status) {
                TuningStatus.InTune -> MaterialTheme.colorScheme.primary
                TuningStatus.TuneUp,
                TuningStatus.TuneDown,
                -> MaterialTheme.colorScheme.tertiary

                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.size(2.dp))
        Text(
            text = if (frequency == null) "-- Hz" else "${formatOneDecimal(frequency)} Hz",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = if (cents == null) "-- cents" else "${formatSignedOneDecimal(cents)} cents",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun statusText(state: TunerSessionState, hasAudioPermission: Boolean): String = when {
    state.errorMessage != null -> state.errorMessage
    !hasAudioPermission -> "Microphone permission required"
    !state.isListening -> "Ready"
    state.measurement.status == TuningStatus.WaitingForSignal -> "Listening"
    state.measurement.status == TuningStatus.SignalClipping -> "Input is clipping"
    state.measurement.status == TuningStatus.NoStringDetected -> "No guitar string detected"
    else -> "Detected"
}

private fun primaryActionLabel(state: TunerSessionState, hasAudioPermission: Boolean): String = when {
    !hasAudioPermission -> "Allow mic"
    state.isListening -> "Pause"
    else -> "Start"
}

private fun guidanceLabel(status: TuningStatus): String = when (status) {
    TuningStatus.WaitingForSignal -> "Strum an open string"
    TuningStatus.SignalClipping -> "Play softer"
    TuningStatus.NoStringDetected -> "Try one string"
    TuningStatus.TuneUp -> "Tune up"
    TuningStatus.TuneDown -> "Tune down"
    TuningStatus.InTune -> "In tune"
}

private fun formatOneDecimal(value: Double): String =
    String.format(Locale.US, "%.1f", value)

private fun formatSignedOneDecimal(value: Double): String =
    String.format(Locale.US, "%+.1f", value)
