package com.sysadmindoc.guitartuner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.semantics.selected
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

private val PanelShape = RoundedCornerShape(8.dp)
private val CompactButtonPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)

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
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            TunerMeterPanel(
                                state = state,
                                hasAudioPermission = hasAudioPermission,
                                activeTuning = activeTuning,
                                tuningMode = tuningMode,
                                guidedStringNumber = guidedStringNumber,
                                pegTurnDirections = preferences.pegTurnDirections,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            TunerActions(
                                state = state,
                                hasAudioPermission = hasAudioPermission,
                                onPrimaryAction = onPrimaryAction,
                                onStop = onStop,
                                onShowPrivacy = onShowPrivacy,
                            )
                        }
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
                        activeTuning = activeTuning,
                        tuningMode = tuningMode,
                        guidedStringNumber = guidedStringNumber,
                        pegTurnDirections = preferences.pegTurnDirections,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TunerActions(
                        state = state,
                        hasAudioPermission = hasAudioPermission,
                        onPrimaryAction = onPrimaryAction,
                        onStop = onStop,
                        onShowPrivacy = onShowPrivacy,
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
    activeTuning: TuningDefinition,
    tuningMode: TuningMode,
    guidedStringNumber: Int,
    pegTurnDirections: Map<Int, PegTurnDirection>,
    modifier: Modifier = Modifier,
) {
    val guidedTarget = if (tuningMode == TuningMode.Guided) {
        activeTuning.strings.firstOrNull { it.stringNumber == guidedStringNumber }
    } else {
        null
    }
    Surface(
        modifier = modifier,
        shape = PanelShape,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            TargetString(state = state, guidedTarget = guidedTarget)
            CentsMeter(state)
            FrequencyReadout(
                state = state,
                hasAudioPermission = hasAudioPermission,
                pegTurnDirections = pegTurnDirections,
                guidedTarget = guidedTarget,
            )
            Text(
                text = stringResource(R.string.privacy_inline_note),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
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
        shape = PanelShape,
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
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
                }
                if (tuningMode == TuningMode.Guided) {
                    val guidedStep = guidedTuningStep(activeTuning.strings, guidedStringNumber)
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
                    )
                }
            }

            SettingsSection(
                title = stringResource(R.string.section_defaults),
                helper = stringResource(R.string.section_defaults_helper),
            ) {
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
                    shape = PanelShape,
                    contentPadding = CompactButtonPadding,
                ) {
                    Text(stringResource(R.string.action_set_favorite))
                }
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
                ToggleSettingRow(
                    label = stringResource(R.string.setting_freeze_last_note),
                    helper = stringResource(R.string.setting_freeze_last_note_helper),
                    checked = preferences.freezeAfterDecay,
                    onCheckedChange = onFreezeAfterDecayChanged,
                )
            }

            SettingsSection(
                title = stringResource(R.string.section_precision),
                helper = stringResource(R.string.section_precision_helper),
            ) {
                NumericSettingRow(
                    label = stringResource(R.string.setting_a4_calibration),
                    value = formatWholeHz(preferences.a4Hz),
                    decreaseLabel = "-1",
                    increaseLabel = "+1",
                    canDecrease = preferences.a4Hz > 400.0,
                    canIncrease = preferences.a4Hz < 480.0,
                    onDecrease = { onA4CalibrationChanged((preferences.a4Hz - 1.0).coerceAtLeast(400.0)) },
                    onIncrease = { onA4CalibrationChanged((preferences.a4Hz + 1.0).coerceAtMost(480.0)) },
                )
                NumericSettingRow(
                    label = stringResource(R.string.setting_noise_gate),
                    value = formatFourDecimals(preferences.noiseGateRms),
                    decreaseLabel = "-",
                    increaseLabel = "+",
                    canDecrease = preferences.noiseGateRms > 0.001,
                    canIncrease = preferences.noiseGateRms < 0.030,
                    onDecrease = {
                        onNoiseGateChanged((preferences.noiseGateRms - 0.0005).coerceAtLeast(0.001))
                    },
                    onIncrease = {
                        onNoiseGateChanged((preferences.noiseGateRms + 0.0005).coerceAtMost(0.030))
                    },
                )
                NumericSettingRow(
                    label = stringResource(R.string.setting_cents_tolerance),
                    value = stringResource(
                        R.string.cents_tolerance_value,
                        formatWholeNumber(preferences.centsTolerance),
                    ),
                    decreaseLabel = "-1",
                    increaseLabel = "+1",
                    canDecrease = preferences.centsTolerance > 1.0,
                    canIncrease = preferences.centsTolerance < 25.0,
                    onDecrease = {
                        onCentsToleranceChanged((preferences.centsTolerance - 1.0).coerceAtLeast(1.0))
                    },
                    onIncrease = {
                        onCentsToleranceChanged((preferences.centsTolerance + 1.0).coerceAtMost(25.0))
                    },
                )
            }

            SettingsSection(
                title = stringResource(R.string.section_files),
                helper = stringResource(R.string.section_files_helper),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onImportTunings,
                        modifier = Modifier.weight(1f),
                        shape = PanelShape,
                        contentPadding = CompactButtonPadding,
                    ) {
                        Text(stringResource(R.string.action_import))
                    }
                    OutlinedButton(
                        onClick = onExportTunings,
                        enabled = hasCustomTunings,
                        modifier = Modifier.weight(1f),
                        shape = PanelShape,
                        contentPadding = CompactButtonPadding,
                    ) {
                        Text(stringResource(R.string.action_export))
                    }
                }
                TuningFileMessageText(tuningFileMessage)
            }
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (state.isListening) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onPrimaryAction,
                    modifier = Modifier.weight(1f),
                    shape = PanelShape,
                ) {
                    Text(primaryActionLabel(state, hasAudioPermission))
                }
                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    shape = PanelShape,
                ) {
                    Text(stringResource(R.string.action_stop))
                }
            }
        } else {
            Button(
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth(),
                shape = PanelShape,
            ) {
                Text(primaryActionLabel(state, hasAudioPermission))
            }
        }
        OutlinedButton(
            onClick = onShowPrivacy,
            modifier = Modifier.fillMaxWidth(),
            shape = PanelShape,
        ) {
            Text(stringResource(R.string.action_privacy))
        }
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
        TuningFileMessage.ReadError -> stringResource(R.string.file_error_read)
        TuningFileMessage.WriteError -> stringResource(R.string.file_error_write)
    }
    val isError = message is TuningFileMessage.Error ||
        message is TuningFileMessage.ReadError ||
        message is TuningFileMessage.WriteError
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PanelShape,
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        contentColor = if (isError) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        },
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    helper: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = helper,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun TuningChoiceGrid(
    tunings: List<TuningDefinition>,
    activeTuning: TuningDefinition,
    onTuningSelected: (TuningDefinition) -> Unit,
) {
    for (row in tunings.chunked(2)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (tuning in row) {
                TuningChoiceButton(
                    tuning = tuning,
                    selected = tuning.id == activeTuning.id,
                    onClick = { onTuningSelected(tuning) },
                    modifier = Modifier.weight(1f),
                )
            }
            if (row.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun GuidedStringGrid(
    strings: List<GuitarString>,
    guidedStringNumber: Int,
    onGuidedStringSelected: (Int) -> Unit,
) {
    for (row in strings.chunked(3)) {
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

@Composable
private fun ToggleSettingRow(
    label: String,
    helper: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val checkedState = if (checked) {
        stringResource(R.string.state_on)
    } else {
        stringResource(R.string.state_off)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = helper,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics {
                contentDescription = label
                stateDescription = checkedState
            },
        )
    }
}

@Composable
private fun NumericSettingRow(
    label: String,
    value: String,
    decreaseLabel: String,
    increaseLabel: String,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        OutlinedButton(
            onClick = onDecrease,
            enabled = canDecrease,
            shape = PanelShape,
            contentPadding = CompactButtonPadding,
        ) {
            Text(decreaseLabel)
        }
        Text(
            text = value,
            modifier = Modifier.widthIn(min = 72.dp),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        OutlinedButton(
            onClick = onIncrease,
            enabled = canIncrease,
            shape = PanelShape,
            contentPadding = CompactButtonPadding,
        ) {
            Text(increaseLabel)
        }
    }
}

@Composable
private fun SelectableOptionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = CompactButtonPadding,
) {
    val selectedState = if (selected) {
        stringResource(R.string.state_selected)
    } else {
        stringResource(R.string.state_not_selected)
    }
    val semanticsModifier = modifier.semantics {
        this.selected = selected
        stateDescription = selectedState
    }
    if (selected) {
        Button(
            onClick = onClick,
            modifier = semanticsModifier,
            shape = PanelShape,
            contentPadding = contentPadding,
        ) {
            Text(label, maxLines = 1, textAlign = TextAlign.Center)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = semanticsModifier,
            shape = PanelShape,
            contentPadding = contentPadding,
        ) {
            Text(label, maxLines = 1, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun TuningChoiceButton(
    tuning: TuningDefinition,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = if (tuning.isBuiltIn) {
        tuning.name
    } else {
        stringResource(R.string.tuning_custom_suffix, tuning.name)
    }
    SelectableOptionButton(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun StartupModeButton(
    mode: StartupTuningMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectableOptionButton(
        label = mode.label(),
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
    )
}

@Composable
private fun ThemeModeButton(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectableOptionButton(
        label = mode.label(),
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
    )
}

@Composable
private fun TuningModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectableOptionButton(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun GuidedStringButton(
    string: GuitarString,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = "${string.stringNumber} ${string.scientificPitch}"
    SelectableOptionButton(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
    )
}

@Composable
private fun PegDirectionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectableOptionButton(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    )
}

private fun GuitarString.walkthroughLabel(): String =
    "$name ($scientificPitch)"

@Composable
private fun TargetString(
    state: TunerSessionState,
    guidedTarget: GuitarString?,
) {
    val measurement = state.measurement
    val target = measurement.target ?: guidedTarget
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = target?.name ?: stringResource(R.string.target_auto_detect),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = when {
                measurement.target != null -> measurement.target.displayName
                guidedTarget != null -> stringResource(
                    R.string.target_guided_prompt,
                    guidedTarget.stringNumber,
                    guidedTarget.scientificPitch,
                )
                else -> stringResource(R.string.target_waiting_for_string)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.meter_flat),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.meter_in_tune),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.meter_sharp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FrequencyReadout(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = guidanceLabel(
                state = state,
                hasAudioPermission = hasAudioPermission,
                turnDirection = measurement.turnDirection(pegTurnDirections),
                guidedTarget = guidedTarget,
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
        Text(
            text = if (state.isListening) {
                stringResource(
                    R.string.input_level_value,
                    formatLevelPercent(inputLevel.rms),
                    formatLevelPercent(inputLevel.peak),
                )
            } else {
                stringResource(R.string.input_level_placeholder)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (state.isListening && inputLevel.sourceLabel != null && inputLevel.sampleRateHz != null) {
            Text(
                text = stringResource(
                    R.string.input_source_value,
                    inputLevel.sourceLabel,
                    inputLevel.sampleRateHz,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun statusText(state: TunerSessionState, hasAudioPermission: Boolean): String = when {
    !hasAudioPermission -> stringResource(R.string.status_permission_required)
    state.errorMessage != null -> state.errorMessage
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
    else -> stringResource(R.string.status_detected)
}

@Composable
private fun primaryActionLabel(state: TunerSessionState, hasAudioPermission: Boolean): String = when {
    !hasAudioPermission && state.permissionError -> stringResource(R.string.action_open_settings)
    !hasAudioPermission -> stringResource(R.string.action_allow_mic)
    state.isListening -> stringResource(R.string.action_pause)
    else -> stringResource(R.string.action_start)
}

@Composable
private fun guidanceLabel(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
    turnDirection: PegTurnDirection?,
    guidedTarget: GuitarString?,
): String {
    if (!hasAudioPermission && state.permissionError) return stringResource(R.string.guidance_open_settings)
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

private fun formatFourDecimals(value: Double): String =
    String.format(Locale.US, "%.4f", value)

private fun formatPercent(value: Double): String =
    String.format(Locale.US, "%.0f%%", value.coerceIn(0.0, 1.0) * 100.0)

private fun formatLevelPercent(value: Double): String =
    String.format(Locale.US, "%.1f%%", value.coerceIn(0.0, 1.0) * 100.0)

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
            tuningMode = TuningMode.Auto,
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
