package com.sysadmindoc.guitartuner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sysadmindoc.guitartuner.R
import com.sysadmindoc.guitartuner.audio.AudioError
import com.sysadmindoc.guitartuner.audio.TunerSessionState
import com.sysadmindoc.guitartuner.tuning.TuningDefinition
import com.sysadmindoc.guitartuner.tuning.TuningMode
import com.sysadmindoc.guitartuner.tuning.TuningStatus

@Composable
internal fun TunerHeader(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
    activeTuning: TuningDefinition,
    tuningMode: TuningMode,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        val currentStatus = statusText(state, hasAudioPermission)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = statusAccentColor(state, hasAudioPermission),
                        shape = RoundedCornerShape(2.dp),
                    )
                    .semantics { contentDescription = currentStatus },
            )
            Text(
                text = currentStatus,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = stringResource(
                R.string.session_summary,
                activeTuning.name,
                tuningMode.label(),
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun TunerActionButtons(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
    permissionPermanentlyDenied: Boolean,
    onPrimaryAction: () -> Unit,
    onFullscreen: () -> Unit,
    onShowPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
    showPrivacy: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = if (showPrivacy) 106.dp else MinTouchTarget),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (state.isListening) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onPrimaryAction,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = MinTouchTarget),
                    shape = PanelShape,
                    contentPadding = CompactButtonPadding,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = primaryActionLabel(state, hasAudioPermission, permissionPermanentlyDenied),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                }
                OutlinedButton(
                    onClick = onFullscreen,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = MinTouchTarget),
                    shape = PanelShape,
                    contentPadding = CompactButtonPadding,
                    border = BorderStroke(PanelBorderWidth, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Text(
                        text = stringResource(R.string.action_fullscreen),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                }
            }
        } else {
            Button(
                onClick = onPrimaryAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = MinTouchTarget),
                shape = PanelShape,
                contentPadding = CompactButtonPadding,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = primaryActionLabel(state, hasAudioPermission, permissionPermanentlyDenied),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
        if (showPrivacy) {
            PrivacyDetailsButton(onShowPrivacy = onShowPrivacy)
        }
    }
}

@Composable
internal fun PrivacyDetailsButton(
    onShowPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onShowPrivacy,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MinTouchTarget),
        shape = PanelShape,
        contentPadding = CompactButtonPadding,
        border = BorderStroke(PanelBorderWidth, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Text(
            text = stringResource(R.string.action_privacy),
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
private fun audioErrorText(error: AudioError): String = when (error) {
    AudioError.MicInitFailed -> stringResource(R.string.error_mic_init)
    AudioError.CaptureStopped -> stringResource(R.string.error_capture_stopped)
}

@Composable
private fun statusText(state: TunerSessionState, hasAudioPermission: Boolean): String = when {
    !hasAudioPermission -> stringResource(R.string.status_permission_required)
    state.audioError != null -> audioErrorText(state.audioError)
    !state.isListening -> stringResource(R.string.status_ready)
    state.isFrozen -> stringResource(R.string.status_frozen)
    state.micStolen -> stringResource(R.string.status_mic_stolen)
    state.inputLevel.isEffectivelySilent &&
        state.measurement.status == TuningStatus.WaitingForSignal ->
        stringResource(R.string.status_mic_silent)
    state.measurement.status == TuningStatus.WaitingForSignal -> stringResource(R.string.status_listening)
    state.measurement.status == TuningStatus.SignalClipping -> stringResource(R.string.status_input_clipping)
    state.measurement.status == TuningStatus.HighNoise -> stringResource(R.string.status_high_noise)
    state.measurement.status == TuningStatus.NoStringDetected -> stringResource(R.string.status_no_string_detected)
    state.measurement.status == TuningStatus.Overshoot -> stringResource(R.string.status_overshoot)
    else -> stringResource(R.string.status_detected)
}

@Composable
private fun primaryActionLabel(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
    permissionPermanentlyDenied: Boolean,
): String = when {
    !hasAudioPermission && permissionPermanentlyDenied -> stringResource(R.string.action_open_settings)
    !hasAudioPermission -> stringResource(R.string.action_allow_mic)
    state.isListening -> stringResource(R.string.action_pause)
    else -> stringResource(R.string.action_start)
}

@Composable
private fun statusAccentColor(state: TunerSessionState, hasAudioPermission: Boolean) = when {
    !hasAudioPermission || state.audioError != null || state.micStolen -> MaterialTheme.colorScheme.error
    state.measurement.status == TuningStatus.InTune -> MaterialTheme.colorScheme.primary
    state.measurement.status == TuningStatus.Overshoot -> MaterialTheme.colorScheme.error
    state.measurement.status == TuningStatus.TuneUp ||
        state.measurement.status == TuningStatus.TuneDown ||
        state.measurement.status == TuningStatus.SignalClipping ||
        state.measurement.status == TuningStatus.HighNoise -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
