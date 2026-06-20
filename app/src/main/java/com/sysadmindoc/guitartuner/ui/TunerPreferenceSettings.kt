package com.sysadmindoc.guitartuner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sysadmindoc.guitartuner.R
import com.sysadmindoc.guitartuner.audio.InputDeviceInfo
import com.sysadmindoc.guitartuner.settings.MeterStyle
import com.sysadmindoc.guitartuner.settings.StartupTuningMode
import com.sysadmindoc.guitartuner.settings.StoredTunerPreferences
import com.sysadmindoc.guitartuner.settings.ThemeMode

@Composable
internal fun DefaultsSection(
    preferences: StoredTunerPreferences,
    onStartupModeSelected: (StartupTuningMode) -> Unit,
    onSetFavoriteTuning: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onFreezeAfterDecayChanged: (Boolean) -> Unit,
    onHapticEnabledChanged: (Boolean) -> Unit,
    onAutoAdvanceGuidedChanged: (Boolean) -> Unit,
    onSpokenFeedbackChanged: (Boolean) -> Unit,
    onMeterStyleSelected: (MeterStyle) -> Unit,
) {
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
        ToggleSettingRow(
            label = stringResource(R.string.setting_haptic),
            helper = stringResource(R.string.setting_haptic_helper),
            checked = preferences.hapticEnabled,
            onCheckedChange = onHapticEnabledChanged,
        )
        ToggleSettingRow(
            label = stringResource(R.string.setting_auto_advance),
            helper = stringResource(R.string.setting_auto_advance_helper),
            checked = preferences.autoAdvanceGuided,
            onCheckedChange = onAutoAdvanceGuidedChanged,
        )
        ToggleSettingRow(
            label = stringResource(R.string.setting_spoken_feedback),
            helper = stringResource(R.string.setting_spoken_feedback_helper),
            checked = preferences.spokenFeedback,
            onCheckedChange = onSpokenFeedbackChanged,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (style in MeterStyle.entries) {
                SelectableOptionButton(
                    label = when (style) {
                        MeterStyle.Normal -> stringResource(R.string.meter_style_normal)
                        MeterStyle.Strobe -> stringResource(R.string.meter_style_strobe)
                    },
                    selected = preferences.meterStyle == style,
                    onClick = { onMeterStyleSelected(style) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
internal fun PrecisionSection(
    preferences: StoredTunerPreferences,
    onMeasureA4: () -> Unit,
    onA4CalibrationChanged: (Double) -> Unit,
    onCentsToleranceChanged: (Double) -> Unit,
    onNoiseGateChanged: (Double) -> Unit,
) {
    SettingsSection(
        title = stringResource(R.string.section_precision),
        helper = stringResource(R.string.section_precision_helper),
    ) {
        NumericSettingRow(
            label = stringResource(R.string.setting_a4_calibration),
            value = formatA4Hz(preferences.a4Hz),
            decreaseLabel = "-0.5",
            increaseLabel = "+0.5",
            canDecrease = preferences.a4Hz > 400.0,
            canIncrease = preferences.a4Hz < 480.0,
            onDecrease = { onA4CalibrationChanged((preferences.a4Hz - 0.5).coerceAtLeast(400.0)) },
            onIncrease = { onA4CalibrationChanged((preferences.a4Hz + 0.5).coerceAtMost(480.0)) },
        )
        OutlinedButton(
            onClick = onMeasureA4,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = MinTouchTarget),
            shape = PanelShape,
            contentPadding = CompactButtonPadding,
        ) {
            Text(stringResource(R.string.action_measure_a4))
        }
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
}

@Composable
internal fun InputDeviceSection(
    inputDevices: List<InputDeviceInfo>,
    selectedDeviceId: Int?,
    onInputDeviceSelected: (Int?) -> Unit,
) {
    SettingsSection(
        title = stringResource(R.string.section_input_device),
        helper = stringResource(R.string.section_input_device_helper),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SelectableOptionButton(
                label = stringResource(R.string.input_device_auto),
                selected = selectedDeviceId == null,
                onClick = { onInputDeviceSelected(null) },
                modifier = Modifier.weight(1f),
            )
        }
        for (device in inputDevices) {
            SelectableOptionButton(
                label = device.label,
                selected = selectedDeviceId == device.id,
                onClick = { onInputDeviceSelected(device.id) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun FilesSection(
    hasCustomTunings: Boolean,
    onImportTunings: () -> Unit,
    onExportTunings: () -> Unit,
    tuningFileMessage: TuningFileMessage?,
) {
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
        TuningFileMessageBanner(tuningFileMessage)
    }
}

@Composable
private fun TuningFileMessageBanner(message: TuningFileMessage?) {
    if (message == null) return
    val text = when (message) {
        is TuningFileMessage.Imported -> stringResource(R.string.file_imported_tunings, message.count)
        is TuningFileMessage.Exported -> stringResource(R.string.file_exported_tunings, message.count)
        TuningFileMessage.NoCustomTunings -> stringResource(R.string.file_no_custom_tunings)
        is TuningFileMessage.Error -> message.text
        TuningFileMessage.FileTooLarge -> stringResource(R.string.file_error_too_large)
        TuningFileMessage.ReadError -> stringResource(R.string.file_error_read)
        TuningFileMessage.WriteError -> stringResource(R.string.file_error_write)
    }
    val isError = message is TuningFileMessage.Error ||
        message is TuningFileMessage.FileTooLarge ||
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
