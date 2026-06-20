package com.sysadmindoc.guitartuner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sysadmindoc.guitartuner.R
import com.sysadmindoc.guitartuner.settings.PegTurnDirection
import com.sysadmindoc.guitartuner.settings.StoredTunerPreferences
import com.sysadmindoc.guitartuner.tuning.TuningDefinition
import com.sysadmindoc.guitartuner.tuning.TuningMode
import com.sysadmindoc.guitartuner.tuning.guidedTuningStep
import com.sysadmindoc.guitartuner.tuning.nextGuidedStringNumber
import com.sysadmindoc.guitartuner.tuning.previousGuidedStringNumber

@Composable
internal fun TuningSection(
    activeTuning: TuningDefinition,
    tunings: List<TuningDefinition>,
    onTuningSelected: (TuningDefinition) -> Unit,
) {
    SettingsSection(
        title = stringResource(R.string.label_tuning),
        helper = stringResource(R.string.section_tuning_helper),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.label_current_tuning),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = activeTuning.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        TuningChoiceGrid(
            tunings = tunings,
            activeTuning = activeTuning,
            onTuningSelected = onTuningSelected,
        )
    }
}

@Composable
internal fun ModeSection(
    activeTuning: TuningDefinition,
    tuningMode: TuningMode,
    guidedStringNumber: Int,
    preferences: StoredTunerPreferences,
    stretchModeActive: Boolean,
    stretchPassNumber: Int,
    stretchMaxDrift: Double?,
    stretchSettled: Boolean,
    onStretchModeToggle: () -> Unit,
    onTuningModeSelected: (TuningMode) -> Unit,
    onGuidedStringSelected: (Int) -> Unit,
    onPegTurnDirectionChanged: (Int, PegTurnDirection) -> Unit,
    onPlayTone: (Double) -> Unit,
) {
    SettingsSection(
        title = stringResource(R.string.label_mode),
        helper = stringResource(R.string.section_mode_helper),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TuningModeButton(
                label = stringResource(R.string.mode_auto),
                selected = tuningMode == TuningMode.Auto,
                onClick = { onTuningModeSelected(TuningMode.Auto) },
                modifier = Modifier.weight(1f),
            )
            TuningModeButton(
                label = stringResource(R.string.mode_guided),
                selected = tuningMode == TuningMode.Guided,
                onClick = { onTuningModeSelected(TuningMode.Guided) },
                modifier = Modifier.weight(1f),
            )
            TuningModeButton(
                label = stringResource(R.string.mode_chromatic),
                selected = tuningMode == TuningMode.Chromatic,
                onClick = { onTuningModeSelected(TuningMode.Chromatic) },
                modifier = Modifier.weight(1f),
            )
        }
        if (tuningMode == TuningMode.Guided) {
            GuidedModeControls(
                activeTuning = activeTuning,
                guidedStringNumber = guidedStringNumber,
                preferences = preferences,
                stretchModeActive = stretchModeActive,
                stretchPassNumber = stretchPassNumber,
                stretchMaxDrift = stretchMaxDrift,
                stretchSettled = stretchSettled,
                onStretchModeToggle = onStretchModeToggle,
                onGuidedStringSelected = onGuidedStringSelected,
                onPegTurnDirectionChanged = onPegTurnDirectionChanged,
                onPlayTone = onPlayTone,
            )
        }
    }
}

@Composable
private fun GuidedModeControls(
    activeTuning: TuningDefinition,
    guidedStringNumber: Int,
    preferences: StoredTunerPreferences,
    stretchModeActive: Boolean,
    stretchPassNumber: Int,
    stretchMaxDrift: Double?,
    stretchSettled: Boolean,
    onStretchModeToggle: () -> Unit,
    onGuidedStringSelected: (Int) -> Unit,
    onPegTurnDirectionChanged: (Int, PegTurnDirection) -> Unit,
    onPlayTone: (Double) -> Unit,
) {
    val guidedStep = guidedTuningStep(activeTuning.strings, guidedStringNumber) ?: return

    OutlinedButton(
        onClick = onStretchModeToggle,
        modifier = Modifier.fillMaxWidth(),
        shape = PanelShape,
        contentPadding = CompactButtonPadding,
    ) {
        Text(
            if (stretchModeActive) stringResource(R.string.stretch_mode_stop)
            else stringResource(R.string.stretch_mode_start),
        )
    }
    if (stretchModeActive) {
        val passLabel = stringResource(R.string.stretch_pass, stretchPassNumber)
        val statusLabel = when {
            stretchSettled -> stringResource(R.string.stretch_settled)
            stretchMaxDrift != null -> stringResource(
                R.string.stretch_max_drift,
                formatOneDecimal(stretchMaxDrift),
            )
            else -> stringResource(R.string.stretch_prompt)
        }
        Text(
            text = "$passLabel — $statusLabel",
            style = MaterialTheme.typography.bodyMedium,
            color = if (stretchSettled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (stretchSettled) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(
                R.string.guided_step,
                guidedStep.stepNumber,
                guidedStep.total,
                guidedStep.string.walkthroughLabel(),
            ),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        OutlinedButton(
            onClick = { onPlayTone(guidedStep.string.frequencyHz) },
            shape = PanelShape,
            contentPadding = CompactButtonPadding,
            modifier = Modifier.defaultMinSize(minHeight = MinTouchTarget),
        ) {
            Text(stringResource(R.string.action_play_tone))
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = {
                onGuidedStringSelected(
                    previousGuidedStringNumber(activeTuning.strings, guidedStringNumber),
                )
            },
            enabled = guidedStep.index > 0,
            modifier = Modifier.weight(1f),
            shape = PanelShape,
            contentPadding = CompactButtonPadding,
        ) {
            Text(stringResource(R.string.action_previous_string))
        }
        OutlinedButton(
            onClick = {
                onGuidedStringSelected(
                    nextGuidedStringNumber(activeTuning.strings, guidedStringNumber),
                )
            },
            enabled = guidedStep.index < guidedStep.total - 1,
            modifier = Modifier.weight(1f),
            shape = PanelShape,
            contentPadding = CompactButtonPadding,
        ) {
            Text(stringResource(R.string.action_next_string))
        }
    }
    Text(
        text = stringResource(R.string.peg_tune_up_turn),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PegDirectionButton(
            label = stringResource(R.string.turn_left),
            selected = preferences.pegTurnDirections[guidedStringNumber] == PegTurnDirection.Left,
            onClick = { onPegTurnDirectionChanged(guidedStringNumber, PegTurnDirection.Left) },
            modifier = Modifier.weight(1f),
        )
        PegDirectionButton(
            label = stringResource(R.string.turn_right),
            selected = preferences.pegTurnDirections[guidedStringNumber] == PegTurnDirection.Right,
            onClick = { onPegTurnDirectionChanged(guidedStringNumber, PegTurnDirection.Right) },
            modifier = Modifier.weight(1f),
        )
    }
    GuidedStringGrid(
        strings = activeTuning.strings,
        guidedStringNumber = guidedStringNumber,
        onGuidedStringSelected = onGuidedStringSelected,
        reversed = preferences.leftHanded,
    )
}
