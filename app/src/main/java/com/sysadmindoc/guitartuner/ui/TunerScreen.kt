package com.sysadmindoc.guitartuner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sysadmindoc.guitartuner.R
import com.sysadmindoc.guitartuner.audio.TunerSessionState
import com.sysadmindoc.guitartuner.settings.PegTurnDirection
import com.sysadmindoc.guitartuner.settings.StartupTuningMode
import com.sysadmindoc.guitartuner.settings.StoredTunerPreferences
import com.sysadmindoc.guitartuner.settings.ThemeMode
import com.sysadmindoc.guitartuner.settings.opposite
import com.sysadmindoc.guitartuner.tuning.GuitarTunings
import com.sysadmindoc.guitartuner.tuning.GuitarString
import com.sysadmindoc.guitartuner.tuning.TuningDefinition
import com.sysadmindoc.guitartuner.tuning.TuningDirection
import com.sysadmindoc.guitartuner.tuning.TuningMode
import com.sysadmindoc.guitartuner.tuning.TuningStatus
import com.sysadmindoc.guitartuner.tuning.guidedTuningStep
import com.sysadmindoc.guitartuner.tuning.nextGuidedStringNumber
import com.sysadmindoc.guitartuner.tuning.previousGuidedStringNumber
import com.sysadmindoc.guitartuner.ui.theme.GuitarTunerTheme
import java.util.Locale
import kotlin.math.abs

@Composable
fun TunerScreen(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
    activeTuning: TuningDefinition,
    tunings: List<TuningDefinition>,
    tuningMode: TuningMode,
    guidedStringNumber: Int,
    preferences: StoredTunerPreferences,
    onPrimaryAction: () -> Unit,
    onStop: () -> Unit,
    onTuningModeSelected: (TuningMode) -> Unit,
    onGuidedStringSelected: (Int) -> Unit,
    onStartupModeSelected: (StartupTuningMode) -> Unit,
    onSetFavoriteTuning: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onFreezeAfterDecayChanged: (Boolean) -> Unit,
    onA4CalibrationChanged: (Double) -> Unit,
    onCentsToleranceChanged: (Double) -> Unit,
    onNoiseGateChanged: (Double) -> Unit,
    onPegTurnDirectionChanged: (Int, PegTurnDirection) -> Unit,
    onTuningSelected: (TuningDefinition) -> Unit,
    onImportTunings: () -> Unit,
    onExportTunings: () -> Unit,
    tuningFileMessage: TuningFileMessage?,
    onShowPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            val wideLayout = maxWidth >= 720.dp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                TunerHeader(state = state, hasAudioPermission = hasAudioPermission)

                if (wideLayout) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        TunerMeterPanel(
                            state = state,
                            hasAudioPermission = hasAudioPermission,
                            pegTurnDirections = preferences.pegTurnDirections,
                            modifier = Modifier.weight(1f),
                        )
                        StartupTuningPanel(
                            activeTuning = activeTuning,
                            tunings = tunings,
                            tuningMode = tuningMode,
                            guidedStringNumber = guidedStringNumber,
                            preferences = preferences,
                            onTuningModeSelected = onTuningModeSelected,
                            onGuidedStringSelected = onGuidedStringSelected,
                            onStartupModeSelected = onStartupModeSelected,
                            onSetFavoriteTuning = onSetFavoriteTuning,
                            onThemeModeSelected = onThemeModeSelected,
                            onFreezeAfterDecayChanged = onFreezeAfterDecayChanged,
                            onA4CalibrationChanged = onA4CalibrationChanged,
                            onCentsToleranceChanged = onCentsToleranceChanged,
                            onNoiseGateChanged = onNoiseGateChanged,
                            onPegTurnDirectionChanged = onPegTurnDirectionChanged,
                            onTuningSelected = onTuningSelected,
                            onImportTunings = onImportTunings,
                            onExportTunings = onExportTunings,
                            tuningFileMessage = tuningFileMessage,
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    TunerMeterPanel(
                        state = state,
                        hasAudioPermission = hasAudioPermission,
                        pegTurnDirections = preferences.pegTurnDirections,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    StartupTuningPanel(
                        activeTuning = activeTuning,
                        tunings = tunings,
                        tuningMode = tuningMode,
                        guidedStringNumber = guidedStringNumber,
                        preferences = preferences,
                        onTuningModeSelected = onTuningModeSelected,
                        onGuidedStringSelected = onGuidedStringSelected,
                        onStartupModeSelected = onStartupModeSelected,
                        onSetFavoriteTuning = onSetFavoriteTuning,
                        onThemeModeSelected = onThemeModeSelected,
                        onFreezeAfterDecayChanged = onFreezeAfterDecayChanged,
                        onA4CalibrationChanged = onA4CalibrationChanged,
                        onCentsToleranceChanged = onCentsToleranceChanged,
                        onNoiseGateChanged = onNoiseGateChanged,
                        onPegTurnDirectionChanged = onPegTurnDirectionChanged,
                        onTuningSelected = onTuningSelected,
                        onImportTunings = onImportTunings,
                        onExportTunings = onExportTunings,
                        tuningFileMessage = tuningFileMessage,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                TunerActions(
                    state = state,
                    hasAudioPermission = hasAudioPermission,
                    onPrimaryAction = onPrimaryAction,
                    onStop = onStop,
                    onShowPrivacy = onShowPrivacy,
                )
            }
        }
    }
}

@Composable
private fun TunerHeader(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.app_name),
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
}

@Composable
private fun TunerMeterPanel(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
    pegTurnDirections: Map<Int, PegTurnDirection>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
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
            FrequencyReadout(
                state = state,
                hasAudioPermission = hasAudioPermission,
                pegTurnDirections = pegTurnDirections,
            )
        }
    }
}

@Composable
private fun StartupTuningPanel(
    activeTuning: TuningDefinition,
    tunings: List<TuningDefinition>,
    tuningMode: TuningMode,
    guidedStringNumber: Int,
    preferences: StoredTunerPreferences,
    onTuningModeSelected: (TuningMode) -> Unit,
    onGuidedStringSelected: (Int) -> Unit,
    onStartupModeSelected: (StartupTuningMode) -> Unit,
    onSetFavoriteTuning: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onFreezeAfterDecayChanged: (Boolean) -> Unit,
    onA4CalibrationChanged: (Double) -> Unit,
    onCentsToleranceChanged: (Double) -> Unit,
    onNoiseGateChanged: (Double) -> Unit,
    onPegTurnDirectionChanged: (Int, PegTurnDirection) -> Unit,
    onTuningSelected: (TuningDefinition) -> Unit,
    onImportTunings: () -> Unit,
    onExportTunings: () -> Unit,
    tuningFileMessage: TuningFileMessage?,
    modifier: Modifier = Modifier,
) {
    val hasCustomTunings = tunings.any { !it.isBuiltIn }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.label_tuning),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = activeTuning.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = stringResource(R.string.label_available_tunings),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            for (tuning in tunings) {
                TuningChoiceButton(
                    tuning = tuning,
                    selected = tuning.id == activeTuning.id,
                    onClick = { onTuningSelected(tuning) },
                )
            }
            Text(
                text = stringResource(R.string.label_mode),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TuningModeButton(
                    label = stringResource(R.string.mode_guided),
                    selected = tuningMode == TuningMode.Guided,
                    onClick = { onTuningModeSelected(TuningMode.Guided) },
                    modifier = Modifier.weight(1f),
                )
                TuningModeButton(
                    label = stringResource(R.string.mode_auto),
                    selected = tuningMode == TuningMode.Auto,
                    onClick = { onTuningModeSelected(TuningMode.Auto) },
                    modifier = Modifier.weight(1f),
                )
            }
            if (tuningMode == TuningMode.Guided) {
                val guidedStep = guidedTuningStep(activeTuning.strings, guidedStringNumber)
                Text(
                    text = stringResource(R.string.label_guided_string),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        R.string.guided_step,
                        guidedStep.stepNumber,
                        guidedStep.total,
                        guidedStep.string.walkthroughLabel(),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
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
                        shape = RoundedCornerShape(8.dp),
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
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(stringResource(R.string.action_next_string))
                    }
                }
                Text(
                    text = stringResource(R.string.setting_peg_direction),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                for (row in activeTuning.strings.chunked(3)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        for (string in row) {
                            GuidedStringButton(
                                string = string,
                                selected = string.stringNumber == guidedStringNumber,
                                onClick = { onGuidedStringSelected(string.stringNumber) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (mode in StartupTuningMode.entries) {
                    StartupModeButton(
                        mode = mode,
                        selected = preferences.startupMode == mode,
                        onClick = { onStartupModeSelected(mode) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            OutlinedButton(
                onClick = onSetFavoriteTuning,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.action_set_favorite))
            }
            Text(
                text = stringResource(R.string.setting_theme),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (mode in ThemeMode.entries) {
                    ThemeModeButton(
                        mode = mode,
                        selected = preferences.themeMode == mode,
                        onClick = { onThemeModeSelected(mode) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.setting_freeze_last_note),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Checkbox(
                    checked = preferences.freezeAfterDecay,
                    onCheckedChange = onFreezeAfterDecayChanged,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.setting_a4_calibration),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                )
                OutlinedButton(
                    onClick = { onA4CalibrationChanged((preferences.a4Hz - 1.0).coerceAtLeast(400.0)) },
                    enabled = preferences.a4Hz > 400.0,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text("-1")
                }
                Text(
                    text = formatWholeHz(preferences.a4Hz),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                OutlinedButton(
                    onClick = { onA4CalibrationChanged((preferences.a4Hz + 1.0).coerceAtMost(480.0)) },
                    enabled = preferences.a4Hz < 480.0,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text("+1")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.setting_noise_gate),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                )
                OutlinedButton(
                    onClick = {
                        onNoiseGateChanged((preferences.noiseGateRms - 0.001).coerceAtLeast(0.002))
                    },
                    enabled = preferences.noiseGateRms > 0.002,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text("-")
                }
                Text(
                    text = formatThreeDecimals(preferences.noiseGateRms),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                OutlinedButton(
                    onClick = {
                        onNoiseGateChanged((preferences.noiseGateRms + 0.001).coerceAtMost(0.030))
                    },
                    enabled = preferences.noiseGateRms < 0.030,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text("+")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.setting_cents_tolerance),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                )
                OutlinedButton(
                    onClick = {
                        onCentsToleranceChanged((preferences.centsTolerance - 1.0).coerceAtLeast(1.0))
                    },
                    enabled = preferences.centsTolerance > 1.0,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text("-1")
                }
                Text(
                    text = stringResource(R.string.cents_tolerance_value, formatWholeNumber(preferences.centsTolerance)),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                OutlinedButton(
                    onClick = {
                        onCentsToleranceChanged((preferences.centsTolerance + 1.0).coerceAtMost(25.0))
                    },
                    enabled = preferences.centsTolerance < 25.0,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text("+1")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onImportTunings,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.action_import))
                }
                OutlinedButton(
                    onClick = onExportTunings,
                    enabled = hasCustomTunings,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.action_export))
                }
            }
            TuningFileMessageText(tuningFileMessage)
        }
    }
}

@Composable
private fun TunerActions(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
    onPrimaryAction: () -> Unit,
    onStop: () -> Unit,
    onShowPrivacy: () -> Unit,
) {
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
            Text(stringResource(R.string.action_stop))
        }
    }
    OutlinedButton(
        onClick = onShowPrivacy,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(stringResource(R.string.action_privacy))
    }
}

@Composable
private fun TuningFileMessageText(message: TuningFileMessage?) {
    if (message == null) return
    val text = when (message) {
        is TuningFileMessage.Imported -> stringResource(R.string.file_imported_tunings, message.count)
        is TuningFileMessage.Exported -> stringResource(R.string.file_exported_tunings, message.count)
        TuningFileMessage.NoCustomTunings -> stringResource(R.string.file_no_custom_tunings)
        is TuningFileMessage.Error -> message.text
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TuningChoiceButton(
    tuning: TuningDefinition,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val label = if (tuning.isBuiltIn) {
        tuning.name
    } else {
        stringResource(R.string.tuning_custom_suffix, tuning.name)
    }
    if (selected) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(label)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(label)
        }
    }
}

@Composable
private fun StartupModeButton(
    mode: StartupTuningMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    val contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            contentPadding = contentPadding,
        ) {
            Text(mode.label(), maxLines = 1)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            contentPadding = contentPadding,
        ) {
            Text(mode.label(), maxLines = 1)
        }
    }
}

@Composable
private fun ThemeModeButton(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    val contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            contentPadding = contentPadding,
        ) {
            Text(mode.label(), maxLines = 1)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            contentPadding = contentPadding,
        ) {
            Text(mode.label(), maxLines = 1)
        }
    }
}

@Composable
private fun TuningModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
        ) {
            Text(label, maxLines = 1)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
        ) {
            Text(label, maxLines = 1)
        }
    }
}

@Composable
private fun GuidedStringButton(
    string: GuitarString,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    val label = "${string.stringNumber} ${string.scientificPitch}"
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
        ) {
            Text(label, maxLines = 1)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
        ) {
            Text(label, maxLines = 1)
        }
    }
}

@Composable
private fun PegDirectionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
        ) {
            Text(label, maxLines = 1)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
        ) {
            Text(label, maxLines = 1)
        }
    }
}

private fun GuitarString.walkthroughLabel(): String =
    "$name ($scientificPitch)"

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
            text = target?.displayName ?: stringResource(R.string.target_waiting_for_string),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CentsMeter(state: TunerSessionState) {
    val cents = state.measurement.cents
    val boundedCents = (cents ?: 0.0).coerceIn(-50.0, 50.0)
    val accessibility = tuningMeterAccessibility(state.measurement)
    val trackColor = MaterialTheme.colorScheme.outline
    val centerColor = MaterialTheme.colorScheme.primary
    val markerColor = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .semantics {
                contentDescription = accessibility.contentDescription
                stateDescription = accessibility.stateDescription
                progressBarRangeInfo = ProgressBarRangeInfo(accessibility.progressCents, -50f..50f)
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
private fun FrequencyReadout(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
    pegTurnDirections: Map<Int, PegTurnDirection>,
) {
    val measurement = state.measurement
    val pitchResult = state.pitchResult
    val cents = pitchResult.centsOffset
    val frequency = pitchResult.frequencyHz
    val confidence = pitchResult.confidence
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = guidanceLabel(
                state = state,
                hasAudioPermission = hasAudioPermission,
                turnDirection = measurement.turnDirection(pegTurnDirections),
            ),
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
            text = if (frequency == null) {
                stringResource(R.string.frequency_placeholder)
            } else {
                stringResource(R.string.frequency_value, formatOneDecimal(frequency))
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = if (cents == null) {
                stringResource(R.string.cents_placeholder)
            } else {
                stringResource(R.string.cents_value, formatSignedOneDecimal(cents))
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = if (frequency == null) {
                stringResource(R.string.confidence_placeholder)
            } else {
                stringResource(R.string.confidence_value, formatPercent(confidence))
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun statusText(state: TunerSessionState, hasAudioPermission: Boolean): String = when {
    !hasAudioPermission -> stringResource(R.string.status_permission_required)
    state.errorMessage != null -> state.errorMessage
    !state.isListening -> stringResource(R.string.status_ready)
    state.isFrozen -> stringResource(R.string.status_frozen)
    state.measurement.status == TuningStatus.WaitingForSignal -> stringResource(R.string.status_listening)
    state.measurement.status == TuningStatus.SignalClipping -> stringResource(R.string.status_input_clipping)
    state.measurement.status == TuningStatus.HighNoise -> stringResource(R.string.status_high_noise)
    state.measurement.status == TuningStatus.NoStringDetected -> stringResource(R.string.status_no_string_detected)
    else -> stringResource(R.string.status_detected)
}

@Composable
private fun primaryActionLabel(state: TunerSessionState, hasAudioPermission: Boolean): String = when {
    !hasAudioPermission -> stringResource(R.string.action_allow_mic)
    state.isListening -> stringResource(R.string.action_pause)
    else -> stringResource(R.string.action_start)
}

@Composable
private fun guidanceLabel(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
    turnDirection: PegTurnDirection?,
): String {
    if (!hasAudioPermission) return stringResource(R.string.guidance_allow_mic)
    if (!state.isListening) return stringResource(R.string.guidance_start_listening)

    val status = state.measurement.status
    val base = when (status) {
        TuningStatus.WaitingForSignal -> stringResource(R.string.guidance_strum_open_string)
        TuningStatus.SignalClipping -> stringResource(R.string.guidance_play_softer)
        TuningStatus.HighNoise -> stringResource(R.string.guidance_reduce_noise)
        TuningStatus.NoStringDetected -> stringResource(R.string.guidance_try_one_string)
        TuningStatus.TuneUp -> stringResource(R.string.guidance_tune_up)
        TuningStatus.TuneDown -> stringResource(R.string.guidance_tune_down)
        TuningStatus.InTune -> stringResource(R.string.guidance_in_tune)
    }
    return if (
        turnDirection == null ||
        status != TuningStatus.TuneUp && status != TuningStatus.TuneDown
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

private fun formatOneDecimal(value: Double): String =
    String.format(Locale.US, "%.1f", value)

private fun formatSignedOneDecimal(value: Double): String =
    String.format(Locale.US, "%+.1f", value)

private fun formatWholeHz(value: Double): String =
    String.format(Locale.US, "%.0f Hz", value)

private fun formatWholeNumber(value: Double): String =
    String.format(Locale.US, "%.0f", value)

private fun formatThreeDecimals(value: Double): String =
    String.format(Locale.US, "%.3f", value)

private fun formatPercent(value: Double): String =
    String.format(Locale.US, "%.0f%%", value.coerceIn(0.0, 1.0) * 100.0)

@Composable
private fun StartupTuningMode.label(): String = when (this) {
    StartupTuningMode.StandardDefault -> stringResource(R.string.startup_standard)
    StartupTuningMode.LastUsed -> stringResource(R.string.startup_last)
    StartupTuningMode.Favorite -> stringResource(R.string.startup_favorite)
}

@Composable
private fun ThemeMode.label(): String = when (this) {
    ThemeMode.System -> stringResource(R.string.theme_system)
    ThemeMode.Dark -> stringResource(R.string.theme_dark)
    ThemeMode.Light -> stringResource(R.string.theme_light)
}

@Preview(name = "Phone portrait", widthDp = 360, heightDp = 740)
@Preview(name = "Phone landscape", widthDp = 840, heightDp = 360)
@Preview(name = "Split screen", widthDp = 320, heightDp = 600)
@Preview(name = "Tablet", widthDp = 900, heightDp = 1100)
@Preview(name = "Foldable", widthDp = 673, heightDp = 841)
@Composable
private fun TunerScreenPreview() {
    GuitarTunerTheme {
        TunerScreen(
            state = TunerSessionState(),
            hasAudioPermission = true,
            activeTuning = GuitarTunings.standard,
            tunings = GuitarTunings.builtIns,
            tuningMode = TuningMode.Guided,
            guidedStringNumber = 6,
            preferences = StoredTunerPreferences(),
            onPrimaryAction = {},
            onStop = {},
            onTuningModeSelected = {},
            onGuidedStringSelected = {},
            onStartupModeSelected = {},
            onSetFavoriteTuning = {},
            onThemeModeSelected = {},
            onFreezeAfterDecayChanged = {},
            onA4CalibrationChanged = {},
            onCentsToleranceChanged = {},
            onNoiseGateChanged = {},
            onPegTurnDirectionChanged = { _, _ -> },
            onTuningSelected = {},
            onImportTunings = {},
            onExportTunings = {},
            tuningFileMessage = null,
            onShowPrivacy = {},
        )
    }
}
